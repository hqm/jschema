package com.beartronics.jschema;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

public class Action {

    public enum Type {
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
        handl, handr, handf, handb,
        HAND2_LEFT, HAND2_RIGHT, HAND2_UP, HAND2_DOWN,
        HAND1_FINE_LEFT, HAND1_FINE_RIGHT, HAND1_FINE_UP, HAND1_FINE_DOWN,
        HAND2_FINE_LEFT, HAND2_FINE_RIGHT, HAND2_FINE_UP, HAND2_FINE_DOWN,
        grasp, ungrasp,
        HAND2_GRASP, HAND2_UNGRASP,
        HAND1_WELD, HAND2_WELD,
        HAND1_UNWELD, HAND2_UNWELD,
        HAND1_HOME, HAND2_HOME
    }

    public static List<String> usableActionNames() {
        Action.Type capabilities[] = {
            Type.NULL_ACTION,
            Type.MOVE_LEFT, Type.MOVE_RIGHT, Type.MOVE_UP, Type.MOVE_DOWN,
            Type.GAZE_LEFT, Type.GAZE_RIGHT, Type.GAZE_UP, Type.GAZE_DOWN,
            Type.CENTER_GAZE,
            Type.FOVEATE_NEXT_OBJECT_LEFT,
            Type.FOVEATE_NEXT_OBJECT_RIGHT,
            Type.FOVEATE_NEXT_OBJECT_UP,
            Type.FOVEATE_NEXT_OBJECT_DOWN, 
            Type.FOVEATE_NEXT_MOTION,
            Type.handl, Type.handr, Type.handf, Type.handb,
            Type.HAND2_LEFT, Type.HAND2_RIGHT, Type.HAND2_UP, Type.HAND2_DOWN,
            Type.HAND1_FINE_LEFT, Type.HAND1_FINE_RIGHT, Type.HAND1_FINE_UP, Type.HAND1_FINE_DOWN,
            Type.HAND2_FINE_LEFT, Type.HAND2_FINE_RIGHT, Type.HAND2_FINE_UP, Type.HAND2_FINE_DOWN,
            Type.grasp, Type.ungrasp,
            Type.HAND2_GRASP, Type.HAND2_UNGRASP,
            Type.HAND1_WELD, Type.HAND2_WELD,
            Type.HAND1_UNWELD, Type.HAND2_UNWELD,
            Type.HAND1_HOME, Type.HAND2_HOME
        };

        List<String> actions = Arrays.asList(capabilities).stream().map(x -> x.name()).collect(Collectors.toList());

        return actions;
    }


    String    name;
    int       index;
    boolean   activated;
    long      lastActivatedAt = -1000;

    Type  type;


    public void activate(boolean val) {
    }



    public Action(String name, Type type, int index) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    public String toString() {
        return String.format("[Action-%d %s]",index, name);
    }

}
