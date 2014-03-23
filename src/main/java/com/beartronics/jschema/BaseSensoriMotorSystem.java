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

    public BaseSensoriMotorSystem(JSchema a, PGraphics retina) {
        this.app = a;
    }

    public WorldState getWorldState() {
        return worldState;
    }

    abstract public void processActions(WorldState w);

    abstract void stepPhysicalWorld();


}


