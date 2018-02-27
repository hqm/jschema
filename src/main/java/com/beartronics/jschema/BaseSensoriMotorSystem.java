package com.beartronics.jschema;
// Basde on code examples from The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2011
// PBox2D example

// Basic example of falling rectangles

import java.util.*;
import processing.core.*;


abstract class BaseSensoriMotorSystem {
    JSchema app;

    public BaseSensoriMotorSystem(JSchema a, PGraphics retina) {
        this.app = a;
    }

    long clock = 0;

    abstract public SensorState getSensorState();

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


