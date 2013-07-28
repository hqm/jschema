package com.beartronics.jschema;

import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import java.util.*;

import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.Shape;

import processing.core.*;

public class MotorAction {

    public enum Type {
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

    public String description;
    public int id; // unique id for this output
    public boolean value;
}

