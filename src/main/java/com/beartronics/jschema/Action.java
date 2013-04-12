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

    public Action(String name, int index) {
        this.name = name;
        this.index = index;
        this.value = value;
        this.type = ActionType.PRIMITIVE;
    }

    public Action(String name, int index, Float value, ActionType type) {
        this.name = name;
        this.index = index;
        this.value = value;
        this.type = type;
    }

    public String toString() {
        return "[Action"+index+" "+name+"]";
    }

}
