package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.io.*;


public class Item {
    String name;
    boolean value;

    /** Section 4.1.2 pp 73. We need to indicate if these value transitions were 'explained'
     * by a schema which was just activated and predicted they would occur.
     */
    Schema predictedPositiveTransition = null;
    Schema predictedNegativeTransition = null;

    long lastPosTransition =  -1000;
    long lastNegTransition =  -1000;


    /** Synthetic items may be in an unknown state */
    boolean knownState = true;

    float generality;
    float accessibility;
    float primitiveValue;
    float delegatedValue;
    Stage stage;

    // If non-null, we are the synthetic item for this host schema
    Schema hostSchema;

    int    id;
    ItemType type;

    public static enum ItemType {
        PRIMITIVE, SYNTHETIC, CONTEXT_CONJUNCTION
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
        this.value = v;
    }

    public String toHTML() {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        p.println("<h1>Item #"+id+" "+name+" "+type+"</h1>");
        p.println("<pre>");
        if (hostSchema != null) {
            p.println("Host Schema: "+hostSchema.makeLink());
        }

        p.println("predictedPositiveTransition: "+predictedPositiveTransition);
        p.println("predictedNegativeTransition: "+predictedNegativeTransition);

        p.println("&Delta; lastPosTransition: "+ (lastPosTransition < 0 ? "never" : (stage.clock - lastPosTransition)));
        p.println("&Delta; lastNegTransition: "+ (lastNegTransition < 0 ? "never" : (stage.clock - lastNegTransition)));


        p.println("value: "+value);
        p.println("knownState: "+knownState);

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


    public String toString() {
        String val = "";
        String lname = name;
        if (type == ItemType.SYNTHETIC) {
            if (hostSchema != null) {
                lname = "S-" + hostSchema.id + "_" + hostSchema.action.type.toString();
            }
            if (knownState) {
                val += value;
            } else {
                val += "unknown";
            }
        } else {
            val += value;
        }
        return "Item-"+id+" "+type+" "+lname+" := "+val;

    }
    public String makeLink() {
        String lname = name;
        if (hostSchema != null) {
            lname = "S-" + hostSchema.id + "_" + hostSchema.action.type.toString();
        }

        return String.format("<a href=\"/items/item?id=%d\">Item-%d %s %s</a>",
                             id, id, lname, type);
    }


}
