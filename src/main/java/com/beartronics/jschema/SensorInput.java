package com.beartronics.jschema;

import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import java.util.*;

import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.Shape;

import processing.core.*;


public class SensorInput {
    public String path;
    public int id; // a unique id for this input

    public long lastPosTransition =  Long.MIN_VALUE;
    public long lastNegTransition =  Long.MIN_VALUE;
    public boolean value;
    public boolean prevValue;

    SensorInput(String path, int id, boolean val) {
        _construct(path, id, val);
    }

    void _construct(String path, int id, boolean val) {
        this.path = path;
        this.id = id;
        this.value = val;
    }

    public String toString() {
        return String.format("%s: %s #%d", path, value, id);
    }

}
