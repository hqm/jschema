package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;


public class Item {
    String name;
    boolean  value;
    boolean prevValue;
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

    public String toString() {
        return "[Item"+id+" "+name+": "+value+"]";
    }

}
