package com.beartronics.jschema;
// Basde on code examples from The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2011
// PBox2D example

// Basic example of falling rectangles

import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import java.util.*;


public class SensoriMotorSystem {

    // A reference to our box2d world
    public PBox2D box2d;
    public JSchema app;

    public SensoriMotorSystem(JSchema app) {
        this.app = app;
    }

    // A list we'll use to track fixed objects
    ArrayList<Boundary> boundaries;
    // A list for all of our rectangles
    ArrayList<Box> boxes;

    void setupDisplay() {
        // Initialize box2d physics and create the world
        box2d = app.createBox2D();
        // Initialize box2d physics and create the world
        box2d.createWorld();
        // We are setting a custom gravity
        box2d.setGravity(0, -10);

        app.smooth();
        // Create ArrayLists  
        boxes = new ArrayList<Box>();
        boundaries = new ArrayList<Boundary>();

        // Add a bunch of fixed boundaries
        boundaries.add(new Boundary(app, app.width/4,app.height-5,app.width/2-50,10));
        boundaries.add(new Boundary(app, 3*app.width/4,app.height-50,app.width/2-50,10));
    }

    void draw() {
        app.background(255);

        // We must always step through time!
        box2d.step();

        // Boxes fall from the top every so often
        if (app.random(1) < 0.2) {
            Box p = new Box(app, app.width/2,30);
            boxes.add(p);
        }

        // Display all the boundaries
        for (Boundary wall: boundaries) {
            wall.display();
        }

        // Display all the boxes
        for (Box b: boxes) {
            b.display();
        }

        // Boxes that leave the screen, we delete them
        // (note they have to be deleted from both the box2d world and our list
        for (int i = boxes.size()-1; i >= 0; i--) {
            Box b = boxes.get(i);
            if (b.done()) {
                boxes.remove(i);
            }
        }
    }

}


