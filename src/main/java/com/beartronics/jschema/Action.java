package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

public class Action {

    public enum Type {
        // a synthetic action
        COMPOSITE, 
            // list of possible primitive actions
            NULL_ACTION,
        MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN,
        GAZE_LEFT, GAZE_RIGHT, GAZE_UP, GAZE_DOWN,
            CENTER_GAZE,
            FOVEATE_NEXT_OBJECT_LEFT,
            FOVEATE_NEXT_OBJECT_RIGHT,
            FOVEATE_NEXT_OBJECT_UP,
            FOVEATE_NEXT_OBJECT_DOWN, 
            FOVEATE_NEXT_MOTION,
            HAND1_LEFT, HAND1_RIGHT, HAND1_UP, HAND1_DOWN,
            HAND2_LEFT, HAND2_RIGHT, HAND2_UP, HAND2_DOWN,
            HAND1_FINE_LEFT, HAND1_FINE_RIGHT, HAND1_FINE_UP, HAND1_FINE_DOWN,
            HAND2_FINE_LEFT, HAND2_FINE_RIGHT, HAND2_FINE_UP, HAND2_FINE_DOWN,
            HAND1_GRASP, HAND1_UNGRASP,
            HAND2_GRASP, HAND2_UNGRASP,
            HAND1_WELD, HAND2_WELD,
	HAND1_UNWELD, HAND2_UNWELD,
	HAND1_HOME
    }


    String    name;
    int       index;
    boolean   activated;
    long      lastActivatedAt = -1000;

    Type  type;

    ActionController controller;
    Stage stage;

    public ArrayList<Schema> schemas = new ArrayList<Schema>();

    public void activate(boolean val) {
        if (val == true) {
            this.lastActivatedAt = stage.clock;
        }
    }

    public Action(Stage stage, String name, int index) {
        this.stage = stage;
        this.name = name;
        this.index = index;
        this.type = Type.HAND1_GRASP;
        this.controller = new ActionController(this);
    }

    public Action(Stage stage, String name, Type type, int index) {
        this.stage = stage;
        this.name = name;
        this.index = index;
        this.type = type;
        this.controller = new ActionController(this);
    }

    public String toHTML() {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        p.println("<h1>Action "+index+": "+name+ " "+type+"</h1>");
        p.println("<pre>");
        p.println("activated: "+activated);
        p.println("Schemas containing this action:");
        for (Schema schema: stage.schemas) {
            if (schema.action == this) {
                p.println(schema.makeLink());
            }
        }

        return s.toString();
    }

    public String makeLink() {
        return "<a href=\"/items/action?id="+index+"\">Action-"+index+ " " +name + "</a>";
    }

    public String toString() {
        return String.format("[Action-%d %s]",index, name);
    }

}
