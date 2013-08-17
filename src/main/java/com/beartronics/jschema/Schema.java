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
    boolean actionTaken;

    boolean succeeded = false;
    boolean applicable = false;

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
    public float duration = 120;
    public float cost = 0;
    long timeActivated = 0;
    long creationTime = 0;

    float correlation() {
        /* ratio of the probability that a transition to the result state happens
           when our action is taken, to the probability that the result state happens
           when our context is satisfied but the action is not taken (i.e., we are applicable but not activated)
        */
        return (succeededWithActivation / succeededWithoutActivation);
    }

    // Extended Context counters
    ExtendedCR xcontext = new ExtendedCR();

    // Extended Result counters
    ExtendedCR xresult = new ExtendedCR();

    // List of schemas who override this schema;
    // defer to these more specific schemas when they are also applicable
    TIntArrayList XOverride = new TIntArrayList();

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
    }


    // Perform designated action
    public void activate() {
        this.activations += 1;
        this.action.activate(true);
    }

    public void handleSuccessfulActivation() {
        if (posResult.size() == 0 && negResult.size() == 0) {
            // bare schema makes no predictions, has no synthetic item to maintain
            return;
        }
        if (syntheticItem != null) {
            syntheticItem.setValue(true);
        }
        timeActivated = stage.clock;
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
            item.predicteNegativeTransition = this;
        }
    }
    public void runMarginalAttribution() {
        // Did we recently perform our specified action?
        actionTaken = (stage.clock - action.lastActivatedAt) < ExtendedCR.eventTransitionMaxInterval;

        updateSyntheticItems();

        // Run the marginal attribution heuristics to decide whether to spin off
        // a new schema, with addtional item in the result set
        xresult.updateResultItems(stage, this, actionTaken);
        
        // run marginal attribution on extended context, looking for new context spinoffs
        //        xcontext.updateItems(stage, this, activated);
    }


    /**
     * Update our statistics on success and failure
     *
     * 
     */
    public void updateSyntheticItems() {
        if (syntheticItem != null) {

            // Absent evidence to the contrary, we deactivate this schema after it's duration has expired
            if (stage.clock > timeActivated + duration) {
                syntheticItem.setValue(false);
                syntheticItem.knownState = false;
            }

            // schemas succeeded if context was satisfied, action taken, and results obtained

            // If we have empty results list, then we only update results stats, and look for
            // potential spinoff condition (prob. that some item transitions more with action than without)

            applicable = true;
            succeeded = true;

            for (Item item: posContext) {
                applicable &= item.value;
            }
            for (Item item: negContext) {
                applicable &= !item.value;
            }

            if (applicable) {

                for (Item item: posResult) {
                    succeeded &= item.value;
                }
                for (Item item: negResult) {
                    succeeded &= !item.value;
                }

                // Did we just perform our specified action, within the valid time window?
                if (actionTaken && succeeded) {
                    // TODO [hqm 2013-07] Need to bias this statistic towards more recent activations
                    succeededWithActivation++;
                    handleSuccessfulActivation();
                    //syntheticItem.setValue(true);
                }

                if (actionTaken && !succeeded) {
                    // TODO [hqm 2013-07] Need to bias this statistic towards more recent activations
                    failedWithActivation++;
                    //                    syntheticItem.setValue(false);
                }

                if (!actionTaken && succeeded ) {
                    succeededWithoutActivation++;
                    //syntheticItem.setValue(false);
                }

            }
        }
    }

    static final boolean POSITIVE = true;
    static final boolean NEGATIVE = true;

    /*  Create a new spinoff schema, adding this item to the positive (or negative) result set

        We want to be careful not to spin off a result item that is our own synthetic item.
     */
    public void spinoffWithNewResultItem(Item item, boolean sense) {
        if (item == syntheticItem) return;
        xresult.ignoreItems.set(item.id);
        Schema schema = spinoffNewSchema();
        children.add(schema);
        if (sense == POSITIVE) {
            schema.posResult.add(item);
            xresult.clearPositiveItems(item.id);
        } else {
            schema.negResult.add(item);
            xresult.clearNegativeItems(item.id);
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
        xresult.ignoreItems.set(child.syntheticItem.id);
        child.xresult.ignoreItems.or(xresult.ignoreItems);

        stage.schemas.add(child);
        stage.ensureXCRcapacities();
        
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
        p.println("duration: " +duration);
        p.println("cost: " +cost);
        p.println("correlation(): " +correlation());
        /*        p.println("<b>xcontext</b>");
        p.println(xcontext.toHTML(stage, this));
        */
        p.println("<b>xresult</b>");
        p.println(xresult.toHTML(stage, this));
        return s.toString();
    }

    public String makeLink() {
        return String.format("<a href=\"/items/schema?id=%d\">Schema %d %s ~%s /%s/%s ~%s</a>",
                             id, id, posContext, negContext, action, posResult, negResult);
    }


    
}
