package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;

public class Action {

    public enum Type {
        // a synthetic action
        COMPOUND, 
            // list of possible primitive actions
        MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN,
        GAZE_LEFT, GAZE_RIGHT, GAZE_UP, GAZE_DOWN,
            FOVEATE_NEXT_OBJECT_LEFT,
            FOVEATE_NEXT_OBJECT_RIGHT,
            FOVEATE_NEXT_OBJECT_UP,
            FOVEATE_NEXT_OBJECT_DOWN, 
            HAND1_LEFT, HAND1_RIGHT, HAND1_UP, HAND1_DOWN,
            HAND2_LEFT, HAND2_RIGHT, HAND2_UP, HAND2_DOWN,
            HAND1_GRASP, HAND1_UNGRASP,
            HAND2_GRASP, HAND2_UNGRASP,
            HAND1_WELD, HAND2_WELD,
            HAND1_UNWELD, HAND2_UNWELD
    }


    String      name;
    int         index;
    boolean     activated;
    Type  type;

    ActionController controller;
    Stage stage;


    public Action(Stage stage, String name, int index) {
        this.stage = stage;
        this.name = name;
        this.index = index;
        this.type = Type.HAND1_GRASP;
        this.controller = new ActionController(this);
    }

    public Action(Stage stage, String name, Type type, int index, boolean active) {
        this.stage = stage;
        this.name = name;
        this.index = index;
        this.activated = active;
        this.type = type;
        this.controller = new ActionController(this);
    }

    public String toHTML() {
        StringBuilder s = new StringBuilder();
        s.append("<h1>Action "+index+": "+name+ " "+type+"</h1>");
        s.append("activated: "+activated);
        return s.toString();
    }

    public String makeLink() {
        return "<a href=\"/items/action?id="+index+"\">Action "+index+ " " +type + "</a>";
    }

    public String toString() {
        return String.format("[Action %d %s %s %s]",index, name, type, activated);
    }

}
