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
    public boolean priorValue;
    public boolean value;
    public float realValue;

    SensorInput(String path, int id, boolean val) {
        _construct(path, id, val, 0f);
    }

    SensorInput(String path, int id, boolean val, float realValue) {
        _construct(path, id, val, realValue);
    }

    void _construct(String path, int id, boolean val, float realValue) {
        this.path = path;
        this.id = id;
        this.value = val;
        this.priorValue = val;
        this.realValue = realValue;
    }

    public String toString() {
        return String.format("<<Input %s %s [prior %s] [real %f] #%d>>", path, value, priorValue, realValue, id);
    }

}
