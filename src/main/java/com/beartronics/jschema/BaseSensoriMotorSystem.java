package com.beartronics.jschema;
// Basde on code examples from The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2011
// PBox2D example

// Basic example of falling rectangles

import java.util.*;
import processing.core.*;


abstract class BaseSensoriMotorSystem {
    public JSchema app;
    public Stage stage;
    public WorldState worldState;

    public BaseSensoriMotorSystem(JSchema a, WorldState w, PGraphics retina) {
        this.app = a;
	this.worldState = w;
    }

    public WorldState getWorldState() {
        return worldState;
    }

    abstract public void processActions(WorldState w);

    abstract void stepPhysicalWorld();

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


