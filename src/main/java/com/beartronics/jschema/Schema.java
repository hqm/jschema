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
    TIntArrayList posContext = new TIntArrayList();
    TIntArrayList negContext = new TIntArrayList();
    // The items in this schema's result list
    TIntArrayList posResult  = new TIntArrayList();
    TIntArrayList negResult  = new TIntArrayList();

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
    TIntArrayList XCposTransitionWithAction = new TIntArrayList();
    TIntArrayList XCposTransitionWithoutAction = new TIntArrayList();

    TIntArrayList XCnegTransitionWithAction = new TIntArrayList();
    TIntArrayList XCnegTranstitionWithoutAction = new TIntArrayList();

    TIntArrayList XCremainPosWithAction = new TIntArrayList();
    TIntArrayList XCremainPosWithoutAction = new TIntArrayList();

    TIntArrayList XCremainNegWithAction = new TIntArrayList();
    TIntArrayList XCremainNegWithoutAction = new TIntArrayList();

    // Extended Result counters
    TIntArrayList XRposTransitionWithAction = new TIntArrayList();
    TIntArrayList XRposTransitionWithoutAction = new TIntArrayList();

    TIntArrayList XRnegTransitionWithAction = new TIntArrayList();
    TIntArrayList XRnegTranstitionWithoutAction = new TIntArrayList();

    TIntArrayList XRremainPosWithAction = new TIntArrayList();
    TIntArrayList XRremainPosWithoutAction = new TIntArrayList();

    TIntArrayList XRremainNegWithAction = new TIntArrayList();
    TIntArrayList XRremainNegWithoutAction = new TIntArrayList();

    // List of schemas who override this schema;
    TIntArrayList XOverride = new TIntArrayList();

    Action action = null;

    public Schema(int index, Action action) {
        this.id = index;
        this.action = action;
    }

    // Initialize this schema, for this stage
    public void initialize(Stage stage) {
        // create extended context, result arrays
        
    }

    public String toString() {
        return "[Schema %s::%~s/ action %s/ %s::~%s]".format(posContext.toString(), negContext.toString(), action, posResult.toString(), negResult.toString());
    }


}
