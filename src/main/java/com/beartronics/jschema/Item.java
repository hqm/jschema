package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.*;


public class Item {
    String name;
    boolean value;
    boolean prevValue; // the value before the most recent action was taken

    /** Section 4.1.2 pp 73. We need to indicate if these value transitions were 'explained'
     * by a schema which was just activated and predicted they would occur.
     */


    long lastPosTransition =  -1000;
    long lastNegTransition =  -1000;


    /** Synthetic items may be in an unknown state */
    boolean knownState = true;
    boolean prevKnownState = true;

    float generality;
    float accessibility;
    float primitiveValue;
    float delegatedValue;

    // If non-null, we are the synthetic item for this host schema

    int    id;
    ItemType type;

    public static enum ItemType {
        PRIMITIVE, SYNTHETIC, CONTEXT_CONJUNCTION
    }

    Item( String name, int index, boolean value, ItemType type) {
        this.name = name;
        this.id = index;
        this.value = value;
        this.type = type;
    }

    Item(String name, int index, boolean value) {
        this.name = name;
        this.id = index;
        this.value = value;
        this.type = ItemType.PRIMITIVE;
    }

    public void setKnownState(boolean v) {
        knownState = v;
    }

    public void setValue(boolean v) {
        value = v;
    }



}
