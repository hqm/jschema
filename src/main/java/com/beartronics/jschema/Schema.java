package com.beartronics.jschema;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;


public class Schema {
    static Logger logger = Logger.getLogger(Schema.class);

    // Numerical id of this schema
    public int id = 0;
    public boolean bare = true;

    // The items in this schema's context list
    public HashSet<Item> posContext = new HashSet<Item>();
    public HashSet<Item> negContext = new HashSet<Item>();
    // The items in this schema's result list
    public HashSet<Item> posResult  = new HashSet<Item>();
    public HashSet<Item> negResult  = new HashSet<Item>();

    // The synthetic item which is controlled by this schema's successful activation.
    // Also known as the 'reifier' item
    public Item syntheticItem = null;

    // Was this action taken recently (within the last time window)
    public boolean actionTaken;

    public Item conjunctItem = null;

    public boolean succeeded = false;
    public boolean applicable = false;

    // reliability statistics
    /** How many times did this schema achieve it's predicted result when activated? */
    public float succeededWithActivation   = 0;

    /** How many times did this schema achieve it's predicted result when not activated? */
    public float succeededWithoutActivation = 0;

    /** How many times did this schema fail to achieve it's predicted results when activated? */
    public float failedWithActivation      = 0; // number of times activation failed

    /** How many times was this schema activated? */
    public int activations = 0;

    // Parent schema from which we were spun off
    public Schema parent = null;
    // List of child schemas which we have spun off
    public ArrayList<Schema> children = new ArrayList<Schema>();

    public float value = 0;
    // See pp. 55
    // correlation, reliability, duration, cost
    /** How long this schema typically remains applicable. Used to maintain the default on time of the synthetic item. */
    public float duration = 30;
    public float cost = 0;
    public long lastTimeActivated = -1000;
    public long lastTimeSucceeded = -1000;
    long creationTime = 0;

    float correlation() {
        /* ratio of the probability that a transition to the result state happens
           when our action is taken, to the probability that the result state happens
           when our context is satisfied but the action is not taken (i.e., we are applicable but not activated)
        */
        return (succeededWithActivation / succeededWithoutActivation);
    }

    // Extended Context counters
    public ExtendedContext xcontext = new ExtendedContext();

    // Extended Result counters
    public ExtendedResult xresult = new ExtendedResult();

    // List of schemas who override this schema;
    // defer to these more specific schemas when they are also applicable
    public TIntArrayList XOverride = new TIntArrayList();

    public Action action = null;
    Stage stage = null;

    public Schema(Stage stage, int index, Action action) {
        this.stage = stage;
        this.id = index;
        this.action = action;
        action.schemas.add(this);

        growArrays(stage.items.size());
        creationTime = stage.clock;
    }

    public void makeSyntheticItem() {
        this.syntheticItem = stage.makeSyntheticItem(this);
        this.syntheticItem.setKnownState(false);
    }

    void clearPredictedTransitions() {
        for (Item item: posResult) {
            item.predictedPositiveTransition = null;
        }
        for (Item item: negResult) {
            item.predictedNegativeTransition = null;
        }
    }

    // Perform designated action
    public void activate() {
        activations += 1;
        action.activate(true);
        actionTaken = true;
        lastTimeActivated = stage.clock;
    }

    // Schema's action was taken, and it's predicted results set was satisfied
    public void handleSuccessfulActivation() {
        if (syntheticItem != null) {
            syntheticItem.setValue(true);
        }
        lastTimeSucceeded = stage.clock;
        /* Section 4.1.2 pp 73
           We need to 'publish' our 'prediction' of items that we assert will transition
           due to our activation.

           This is so that the marginal attribution algorithm can
           allow idle schemas to not have to tally up "explained" transitions in their
           extended results "action not taken" counters. The schema need only bring about the
           result item transition more often than the results other "unexplained" occurences.

         */
        for (Item item: posResult) {
            item.predictedPositiveTransition = this;
        }
        for (Item item: negResult) {
            item.predictedNegativeTransition = this;
        }
        
    }

    /**
       @param item the item for which we're updating statistics
       @param lastTimeActivityTime the time that any action was taken (not necessarily our action)
     */
    public void updateResultsCounters(Item item, long lastActivityTime) {
        // Run the marginal attribution heuristics to decide whether to spin off
        // a new schema with new results
        xresult.updateResultItem(stage, this, item, actionTaken, lastActivityTime);
    }


    /**
       Checks if the context is satisfied
     */
    public void updateApplicableFlag() {
        applicable = true;
        for (Item item: posContext) {
            if (!item.knownState) {
                applicable = false;
            } else {
                applicable &= item.value;
            }
        }
        for (Item item: negContext) {
            if (!item.knownState) {
                applicable = false;
            } else {
                applicable &= !item.value;
            }
        }
    }

    /** Assumes 'applicable' has already been calculated. If 'applicable == true' it means the
        context must be satisfied, i.e., the conjunction of items in the context must be true.
        There is a 'conjunctItem' that we maintain, and we need to update it's pos and neg transition info.
        We do that here, by computing if its prior value differs from the current value.

    */
    public void updateConjunctItem(long lastActivityTime) {
        // Update the conjunct-item value transitions, if there is one, which is tracking our context's value
        // If 'applicable' is true, that means the value of the context must be true. I'll refer to this as 'newval'
        boolean newval = this.applicable; 
        boolean oldval = conjunctItem.value;
        if (!oldval && newval) {
            conjunctItem.lastPosTransition = lastActivityTime;
        } else if (oldval && !newval) {
            conjunctItem.lastNegTransition = lastActivityTime;
        }
        conjunctItem.value = newval;
        conjunctItem.prevValue = oldval;
    }

    /**
     * Update our statistics on success and failure
     *
     * 
     */
    public void handleActivation() {
        // Absent evidence to the contrary, we deactivate this schema after it's duration has expired
        if (stage.clock > (lastTimeActivated + duration)) {
            syntheticItem.setValue(false);
            syntheticItem.setKnownState(false);
            logger.info("handleActivation "+this+" timed out synthetic item "+syntheticItem);
        }

        // schemas succeeded if context was satisfied, action taken, and results obtained

        // If we have empty results list, then we only update results stats, and look for
        // potential spinoff condition (prob. that some item transitions more with action than without)


        succeeded = true;

        if (applicable) {

            for (Item item: posResult) {
                if (!item.knownState) {
                    succeeded = false;
                } else {
                    succeeded &= item.value;
                }
            }
            for (Item item: negResult) {
                if (!item.knownState) {
                    succeeded = false;
                } else {
                    succeeded &= !item.value;
                }
            }

            xcontext.updateContextItems(stage, this, succeeded, lastTimeActivated);

            logger.info("handleActivation "+this+" applicable=true, succeeded="+succeeded);


            // Did we just perform our specified action, within the valid time window?
            if (actionTaken && succeeded) {
                // TODO [hqm 2013-07] Need to bias this statistic towards more recent activations
                succeededWithActivation++;
                handleSuccessfulActivation();
            }

            if (actionTaken && !succeeded) {
                // TODO [hqm 2013-07] Need to bias this statistic towards more recent activations
                failedWithActivation++;
                if (syntheticItem != null) { syntheticItem.setValue(false); }
            }

            // TODO [hqm 2013-08] ?? What should we do with synthetic item in this case?
            // TODO case currently will never happen because handleActivation is only called when actionTaken=true
            // Do we need to call handleActivation on all schemas whenever any action is taken?? 
            if (!actionTaken && succeeded ) {
                succeededWithoutActivation++;
            }

        } else {
            logger.info("handleActivation "+this+" applicable=false");
        }


    }

    static final boolean POSITIVE = true;
    static final boolean NEGATIVE = true;

    /*  Create a new spinoff schema, adding this item to the positive (or negative) result set

        We want to be careful not to spin off a result item that is our own synthetic item.
     */
    public void spinoffWithNewResultItem(Item item, boolean sense) {
        if (sense) {
            xresult.ignoreItemsPos.set(item.id);
        } else {
            xresult.ignoreItemsNeg.set(item.id);
        }
        Schema schema = spinoffNewSchema();
        schema.bare = false;
        children.add(schema);
        if (sense == POSITIVE) {
            schema.posResult.add(item);
            xresult.clearPositiveItems(item.id);
        } else {
            schema.negResult.add(item);
            xresult.clearNegativeItems(item.id);
        }

    }

    public void spinoffWithNewContextItem(Item item, boolean sense) {
        logger.info("spinoffWithNewContextItem: "+this+ "sense="+sense+ ":= "+xcontext.describeContextItem(item));


        if (sense == true) { // positive value
            xcontext.ignoreItemsOn.set(item.id);
        } else {
            xcontext.ignoreItemsOff.set(item.id);
        }
        Schema child = spinoffNewSchema();

        xcontext.clearAllCounters();

        child.bare = false;
        children.add(child);
        if (sense == POSITIVE) {
            child.posContext.add(item);
            logger.info("clearOnItems "+child+" item "+item);
        } else {
            child.negContext.add(item);
            logger.info("clearOffItems "+child+" item "+item);
        }

        // If more than one item in the context, create a pseudo-item for the conjunction of the items,
        // so that schemas can consider using it as the result for a new spin-off schema.
        if ((child.posContext.size() + child.negContext.size()) > 1) {
            // Create a new conjunct item for our context items. This will be
            // a candidate for inclusion in result of a new schema.
            int nitems = stage.items.size();
            String name = String.format("%s-%s-~%s", Integer.toString(nitems), child.posContext, child.negContext);
            Item citem = new Item(stage, name, nitems, false, Item.ItemType.CONTEXT_CONJUNCTION);
            child.conjunctItem = citem;
            stage.items.add(citem);
            stage.conjunctItems.add(citem);
            stage.ensureXCRcapacities();
        }
    }

    // Search all children to find one which has this item in its result
    public Schema findSchemaWithResult(Item item, boolean positive) {
        for (Schema child: children) {
            if (positive && child.posResult.contains(item)) {
                    return child;
            }
            if (!positive && child.negResult.contains(item)) {
                    return child;
            }

            Schema found = child.findSchemaWithResult(item, positive);
            if (found != null) {
                return found;
            }
        }
        return null;
    }


    /**
       copies the context, action, and result lists
     */
    Schema spinoffNewSchema() {
        Schema child = new Schema(stage, stage.schemas.size(), action);
        child.parent = this;
        // Copy the context and result into the new child schema
        child.posContext.addAll(posContext);
        child.negContext.addAll(negContext);
        child.posResult.addAll(posResult);
        child.negResult.addAll(negResult);

        // TODO verify if we need to do this or something like it
        //child.xcontext.ignoreItems.or(xcontext.ignoreItems);

        child.makeSyntheticItem();
        // ignore child's synthetic item
        xresult.ignoreItemsPos.set(child.syntheticItem.id);
        xresult.ignoreItemsNeg.set(child.syntheticItem.id);
        child.xresult.ignoreItemsPos.or(xresult.ignoreItemsPos);
        child.xresult.ignoreItemsNeg.or(xresult.ignoreItemsNeg);

        stage.schemas.add(child);
        stage.ensureXCRcapacities();
        
        logger.debug("spun off new schema "+child);
        return child;
    }


    // Call this when we add a new synthetic item, to grow everyone's extended context and result lists
    public void growArrays(int n) {
        xcontext.growArrays(n);
        xresult.growArrays(n);
    }

    // Initialize this schema, for this stage
    public void initialize() {
        // create extended context, result arrays
    }

    public String toString() {
        return String.format("[Schema %d %s:~%s/%s/%s:~%s]",id, posContext, negContext, action, posResult, negResult);
    }

    String itemLinks(HashSet<Item> l) {
        StringBuilder b = new StringBuilder();
        boolean prev = false;
        b.append("[");
        for (Item i: l) {
            b.append((prev ? "," : "") + i.makeLink());
            prev = true;
        }
        b.append("]");
        return b.toString();
    }


    public String toHTML() {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        p.println("<h1>Schema "+id+"</h1>");
        p.println("<pre>");
        p.println(this.toString());
        p.println("Action: "+action.makeLink());
        p.println("creationTime: "+creationTime);
        p.println("parent: "+(parent != null ? parent.makeLink() : null));
        p.print("children: ");
        if (children.size() > 0) {
            for (Schema c: children) {
                p.println(c.makeLink());
            }
        } else {
            p.println("none");
        }
        p.println("posContext: " + itemLinks(posContext));
        p.println("negContext: "+ itemLinks(negContext));
        p.println("posResult: "+ itemLinks(posResult));
        p.println("negResult: "+ itemLinks(negResult));
        p.println("Synthetic Item: "+ (syntheticItem == null ? "null" : syntheticItem.makeLink()));
        p.println("activations = "+activations);
        p.println("succeededWithActivation = "+succeededWithActivation);
        p.println("succeededWithoutActivation = "+succeededWithoutActivation);
        p.println("failedWithActivation = "+failedWithActivation);
        p.println("applicable: "+applicable);
        p.println("value: " +value);
        p.println("lastTimeActivated: "+lastTimeActivated);
        p.println("lastTimeSucceeded: "+lastTimeSucceeded);
        p.println("duration: " +duration);
        p.println("cost: " +cost);
        p.println("correlation(): " +correlation());
        p.println("bare: " +bare);
        /*        p.println("<b>xcontext</b>");
        p.println(xcontext.toHTML(stage, this));
        */
        p.println("<table rows=2 border=1>");
        p.println("<tr><th>Extended Context</th><th>Extended Result</th></tr>");
        p.println("<tr><td align=left><pre>");
        p.println(xcontext.toHTML(stage, this));
        p.println("</td>");
        p.println("<td align=left><pre>");
        p.println(xresult.toHTML(stage, this));
        p.println("</td></tr>");
        p.println("</table>");
        return s.toString();
    }

    public String makeLink() {
        return String.format("<a href=\"/items/schema?id=%d\">Schema %d <font color=green>%s</font> <font color=red>~%s</font> /%s/<font color=green>%s</font> <font color=red>~%s</font></a>",
                             id, id, posContext, negContext, action, posResult, negResult);
    }


    
}
