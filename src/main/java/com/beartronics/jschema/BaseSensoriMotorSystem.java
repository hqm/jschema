package com.beartronics.jschema;
// Basde on code examples from The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2011
// PBox2D example

// Basic example of falling rectangles

import java.util.*;
import processing.core.*;


abstract class BaseSensoriMotorSystem {
    public SensorState  sensorState;
    JSchema app;

    public BaseSensoriMotorSystem(JSchema a, PGraphics retina) {
        this.app = a;
        this.sensorState = new SensorState();
    }

    long clock = 0;

    public SensorState getSensorState() {
        return sensorState;
    }

    abstract void stepPhysicalWorld(List<String> actions);

    public void keyReleased() {
    }

    public void mousePressed() {
    }

    public void mouseReleased() {
    }

    // debugging methods

    public void moveHand2(int x, int y) {
    }

    public void moveHand1(int x, int y) {
    }

}


