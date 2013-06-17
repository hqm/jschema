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

import processing.core.*;


public class SensoriMotorSystem {

    // A reference to our box2d world
    public PBox2D box2d;
    public JSchema app;

    public SensoriMotorSystem(JSchema a) {
        this.app = a;
        System.out.println("SensoriMotorSystem constructor this.app = "+this.app);
    }

    // A list we'll use to track fixed objects
    public ArrayList<Boundary> boundaries;
    // A list for all of our rectangles
    public ArrayList<Box> boxes;

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
        boundaries.add(new Boundary(app, app.width/4-50, app.height-5, app.width/2-50, 10f ));
        boundaries.add(new Boundary(app, app.width*(3/4f)+50, app.height-5, app.width/2-50, 10f ));

        boundaries.add(new Boundary(app, app.width/4, app.height-100, 10f, 200f ));
        boundaries.add(new Boundary(app, app.width*(3f/4f), app.height-100f, 10f, 200f ));

    }

    void draw() {
        app.background(255);

        // We must always step through time!
        box2d.step();

        // Boxes fall from the top every so often
        if ((app.random(1) < 0.2) && (app.mousePressed && (app.mouseButton == PConstants.LEFT)) ) {
            Box p = new Box(app, app.mouseX,app.mouseY);
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


