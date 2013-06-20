package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;

public class Action {

    public static enum ActionType {
        PRIMITIVE,
        COMPOUND
    }

    String      name;
    int         index;
    float       value;
    ActionType  type;

    ActionController controller;
    Stage stage;


    public Action(Stage stage, String name, int index) {
        this.stage = stage;
        this.name = name;
        this.index = index;
        this.value = value;
        this.type = ActionType.PRIMITIVE;
        this.controller = new ActionController(this);
    }

    public Action(String name, int index, Float value, ActionType type) {
        this.stage = stage;
        this.name = name;
        this.index = index;
        this.value = value;
        this.type = type;
        this.controller = new ActionController(this);
    }

    public String toString() {
        return "[Action"+index+" "+name+"]";
    }

}
