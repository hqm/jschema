package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;


public class Item {
    String name;
    boolean  value;
    boolean prevValue;

    /** Synthetic items may be in an unknown state */
    boolean knownState;

    float generality;
    float accessibility;
    float primitiveValue;
    float delegatedValue;
    

    int    id;
    ItemType type;

    public static enum ItemType {
        PRIMITIVE, SYNTHETIC
    }

    Item(String name, int index, boolean value, ItemType type) {
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

    public void setValue(boolean v) {
        this.prevValue = this.value;
        this.value = v;
    }

    public String toString() {
        String val = "";
        if (type == ItemType.SYNTHETIC) {
            if (knownState) {
                val += value;
            } else {
                val += "unknown";
            }
        } else {
            val += value;
        }
        return "[Item"+id+" "+type+" "+name+": "+ val+"]";

    }

}
