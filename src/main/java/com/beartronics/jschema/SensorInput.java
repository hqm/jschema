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
    public String description;
    public String path;
    public int id; // a unique id for this input
    public boolean priorValue;
    public boolean value;

}
