package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.io.*;


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
    Stage stage;
    

    int    id;
    ItemType type;

    public static enum ItemType {
        PRIMITIVE, SYNTHETIC
    }

    Item(Stage stage, String name, int index, boolean value, ItemType type) {
        this.stage = stage;
        this.name = name;
        this.id = index;
        this.value = value;
        this.type = type;
    }

    Item(Stage stage, String name, int index, boolean value) {
        this.stage = stage;
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

    public String toHTML() {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        p.println("<h1>Item #"+id+" "+name+" "+type+"</h1>");
        p.println("<pre>");
        p.println("value: "+value);
        p.println("previous value: "+prevValue);

        p.println("in posContext of schemas:");
        // find all schemas which include us in their context
        for (Schema schema: stage.schemas) {
            if (schema.posContext.contains(this)) {
                p.println(schema.makeLink());
            }
        }
        p.println("in negContext of schemas:");
        // find all schemas which include us in their context
        for (Schema schema: stage.schemas) {
            if (schema.negContext.contains(this)) {
                p.println(schema.makeLink());
            }
        }
        p.println("in posResult of schemas:");
        // find all schemas which include us in their result
        for (Schema schema: stage.schemas) {
            if (schema.posResult.contains(this)) {
                p.println(schema.makeLink());
            }
        }
        p.println("in negResult of schemas:");
        // find all schemas which include us in their result
        for (Schema schema: stage.schemas) {
            if (schema.negResult.contains(this)) {
                p.println(schema.makeLink());
            }
        }
        return s.toString();
    }

    public String makeLink() {
        return String.format("<a href=\"/items/item?id=%d\">Item %d %s %s %s %s </a>",
                             id, id, name, type, value, prevValue);
    }


}
