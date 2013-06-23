package com.beartronics.jschema;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class Schema {
    // Numerical id of this schema
    int id = 0;
    
    // The items in this schema's context list
    ArrayList<Item> posContext = new ArrayList<Item>();
    ArrayList<Item> negContext = new ArrayList<Item>();
    // The items in this schema's result list
    ArrayList<Item> posResult  = new ArrayList<Item>();
    ArrayList<Item> negResult  = new ArrayList<Item>();

    // The synthetic item which is controlled by this schema's successful activation.
    // Also known as the 'reifier' item
    Item syntheticItem = null;

    // reliability statistics
    float succeededWithActivation   = 0;
    float succededWithoutActivation = 0;
    float failedWithActivation      = 0; // number of times activation failed

    // Parent schema from which we were spun off
    Schema parent = null;
    // List of child schemas which we have spun off
    ArrayList<Schema> children = new ArrayList<Schema>();

    boolean applicable = false;
    float value = 0;
    // See pp. 55
    // correlation, reliability, duration, cost
    float duration = Float.POSITIVE_INFINITY;
    float cost = 0;

    float correlation() {
        /* ratio of the probability that a transition to the result state happens
           when our action is taken, to the probability that the result state happens
           when our context is satisfied but the action is not taken (i.e., we are applicable but not activated)
        */
        return (succeededWithActivation / succededWithoutActivation);
    }

    // Extended Context counters
    ExtendedCR xcontext = new ExtendedCR();

    // Extended Result counters
    ExtendedCR xresult = new ExtendedCR();

    // List of schemas who override this schema;
    // defer to these more specific schemas when they are also applicable
    TIntArrayList XOverride = new TIntArrayList();

    Action action = null;
    Stage stage = null;

    public Schema(Stage stage, int index, Action action) {
        this.stage = stage;
        this.id = index;
        this.action = action;
        syntheticItem = stage.makeSyntheticItem(this);
    }

    public void ensureCapacity(int n) {
        xcontext.ensureCapacity(n);
        xresult.ensureCapacity(n);
    }

    // Initialize this schema, for this stage
    public void initialize() {
        // create extended context, result arrays
    }

    public String toString() {
        return "[Schema %s::%~s/ action %s/ %s::~%s]".format(posContext.toString(), negContext.toString(), action, posResult.toString(), negResult.toString());
    }


}
