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

  // Also known as the 'reifier' item
    Item syntheticItem = null;

    // reliability statistics
    int succeededWithActivation   = 0;
    int failedWithActivation      = 0; // number of times activation failed
    int succededWithoutActivation = 0;

    // Parent schema from which we were spun off
    Schema parent = null;
    // List of child schemas which we have spun off
    ArrayList<Schema> children = new ArrayList<Schema>();

    boolean applicable = false;
    float value = 0;
    // See pp. 55
    // correlation, reliability, duration, cost

    // Extended Context counters
    ExtendedCR xcontext = new ExtendedCR();

    // Extended Result counters
    ExtendedCR xresult = new ExtendedCR();

    // List of schemas who override this schema;
    TIntArrayList XOverride = new TIntArrayList();

    Action action = null;

    public Schema(int index, Action action) {
        this.id = index;
        this.action = action;
    }

    public void ensureCapacity(int n) {
        xcontext.ensureCapacity(n);
        xresult.ensureCapacity(n);
    }

    // Initialize this schema, for this stage
    public void initialize(Stage stage) {
        // create extended context, result arrays
    }

    public String toString() {
        return "[Schema %s::%~s/ action %s/ %s::~%s]".format(posContext.toString(), negContext.toString(), action, posResult.toString(), negResult.toString());
    }


}
