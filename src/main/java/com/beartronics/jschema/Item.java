package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;

public class Item {
    String name;
    float  value;
    int    index;
    ItemType type;

    public static enum ItemType {
        PRIMITIVE, SYNTHETIC
    }


    Item(String name, int index, float value, ItemType type) {
        this.name = name;
        this.index = index;
        this.value = value;
        this.type = type;
    }

    Item(String name, int index, float value) {
        this.name = name;
        this.index = index;
        this.value = value;
        this.type = ItemType.PRIMITIVE;
    }

    public String toString() {
        return "[Item"+index+" "+name+": "+value+"]";
    }

}
