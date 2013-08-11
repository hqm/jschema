package com.beartronics.jschema;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class Schema {
    // Numerical id of this schema
    public int id = 0;
    
    // The items in this schema's context list
    public ArrayList<Item> posContext = new ArrayList<Item>();
    public ArrayList<Item> negContext = new ArrayList<Item>();
    // The items in this schema's result list
    public ArrayList<Item> posResult  = new ArrayList<Item>();
    public ArrayList<Item> negResult  = new ArrayList<Item>();

    // The synthetic item which is controlled by this schema's successful activation.
    // Also known as the 'reifier' item
    public Item syntheticItem = null;

    // reliability statistics
    public float succeededWithActivation   = 0;
    public float succeededWithoutActivation = 0;
    public float failedWithActivation      = 0; // number of times activation failed

    // Parent schema from which we were spun off
    public Schema parent = null;
    // List of child schemas which we have spun off
    public ArrayList<Schema> children = new ArrayList<Schema>();

    public boolean applicable = false;
    public float value = 0;
    // See pp. 55
    // correlation, reliability, duration, cost
    public float duration = 3600;
    public float cost = 0;
    long timeActivated = 0;

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
        syntheticItem = stage.makeSyntheticItem(this);
        growArrays(stage.items.size());
    }

    // Perform designated action
    // turn on our synthetic item, and start the clock so we can turn it off after our duration time
    public void activate() {
        syntheticItem.setValue(true);
        this.action.activate(true);
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
            item.predictedPositiveTransition = true;
        }
        for (Item item: negResult) {
            item.predicteNegativeTransition = true;
        }
    }

    /**
     * Update all our statistics
     *
     * 
     */
    public void runMarginalAttribution(Stage s) {
        if (stage.clock > timeActivated + duration) {
            syntheticItem.setValue(false);
        }
        boolean actionTaken = action.activated;
        boolean succeeded = true;
        boolean applicable = true;


        // schemas succeeded if context was satisfied, action taken, and results obtained

        // If we have empty results list, then we only update results stats, and look for
        // potential spinoff condition (prob. that some item transitions more with action than without)

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

            if (actionTaken && succeeded) {
            // TODO [hqm 2013-07] Need to bias this statistic towards more recent activations
                succeededWithActivation++;
                syntheticItem.setValue(true);
            }

            if (actionTaken && !succeeded) {
                // TODO [hqm 2013-07] Need to bias this statistic towards more recent activations
                failedWithActivation++;
                syntheticItem.setValue(false);
            }

            if (!actionTaken && succeeded ) {
                succeededWithoutActivation++;
                syntheticItem.setValue(false);
            }

        }

        // Run the marginal attribution heuristics to decide whether to spin off
        // a new schema
        xresult.updateResultItems(stage, this, actionTaken);
        


        // Now the heavy lifting; update the extended context and result
        //        xcontext.updateItems(stage, this, activated);

    }

    
    //  Create a new spinoff schema, adding this item to the positive (or negative) result set
    void spinoffWithNewResultItem(Item item, boolean positive) {
        Schema schema = stage.spinoffNewSchema(this);
        if (positive) {
            schema.posResult.add(item);
        } else {
            schema.negResult.add(item);
        }
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
        return String.format("[Schema %d %s::~%s/ action %s/ %s::~%s]",id, posContext, negContext, action, posResult, negResult);
    }

    public String toHTML() {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        p.println("<h1>Schema "+id+"</h1>");
        p.println("Action: "+action.makeLink());
        p.println("<pre>");
        p.println("parent: "+parent);
        p.println("posContext: "+posContext);
        p.println("negContext: "+negContext);
        p.println("posResult: "+posResult);
        p.println("negResult: "+negResult);
        p.println("Synthetic Item: "+syntheticItem.makeLink());
        p.println("succeededWithActivation = "+succeededWithActivation);
        p.println("succeededWithoutActivation = "+succeededWithoutActivation);
        p.println("failedWithActivation = "+failedWithActivation);
        p.println("applicable: "+applicable);
        p.println("value: " +value);
        p.println("duration: " +duration);
        p.println("cost: " +cost);
        p.println("correlation(): " +correlation());
        p.println("<b>xcontext</b>");
        p.println(xcontext.toHTML(stage, this));
        p.println("<b>xresult</b>");
        p.println(xresult.toHTML(stage, this));
        return s.toString();
    }

    public String makeLink() {
        return String.format("<a href=\"/items/schema?id=%d\">Schema %d %s :: ~ %s / %s / %s ::~ %s</a>",
                             id, id, posContext, negContext, action, posResult, negResult);
    }


}
