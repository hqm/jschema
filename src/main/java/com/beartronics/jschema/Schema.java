package com.beartronics.jschema;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

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
    public float duration = Float.POSITIVE_INFINITY;
    public float cost = 0;

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
    }

    /**
       Update all our statistics
     */
    public void runMarginalAttribution(Stage s) {
        boolean activated = action.activated;
        boolean succeeded = true;
        boolean applicable = true;
        // schemas succeeded if context was satisfied, action taken, and results obtained

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

            if (activated && succeeded) {
            // TODO [hqm 2013-07] Need to bias this statistic towards more recent activations
                succeededWithActivation++;
                syntheticItem.setValue(true);
            }

            if (activated && !succeeded) {
                // TODO [hqm 2013-07] Need to bias this statistic towards more recent activations
                failedWithActivation++;
                syntheticItem.setValue(false);
            }

            if (!activated && succeeded ) {
                succeededWithoutActivation++;
                syntheticItem.setValue(false);
            }

        }

        // Now the heavy lifting; update the extended context and result

    }

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


}
