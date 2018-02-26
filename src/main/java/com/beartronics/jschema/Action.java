package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
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
        HAND1_LEFT, HAND1_RIGHT, HAND1_UP, HAND1_DOWN,
        HAND2_LEFT, HAND2_RIGHT, HAND2_UP, HAND2_DOWN,
        HAND1_FINE_LEFT, HAND1_FINE_RIGHT, HAND1_FINE_UP, HAND1_FINE_DOWN,
        HAND2_FINE_LEFT, HAND2_FINE_RIGHT, HAND2_FINE_UP, HAND2_FINE_DOWN,
        HAND1_GRASP, HAND1_UNGRASP,
        HAND2_GRASP, HAND2_UNGRASP,
        HAND1_WELD, HAND2_WELD,
        HAND1_UNWELD, HAND2_UNWELD,
        HAND1_HOME, HAND2_HOME
    }

    public static List<String> usableActionNames() {
        Action capabilities[] = {
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
            HAND1_HOME, HAND2_HOME
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

    public Action(String name, int index) {
        this.name = name;
        this.index = index;
        this.type = Type.HAND1_GRASP;
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
