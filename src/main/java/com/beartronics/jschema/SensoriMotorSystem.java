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

    // A list for all particle systems
    ArrayList<ParticleSystem> systems;

    PFont font;

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
        boundaries.add(new Boundary(app, app.width/2, app.height-5, app.width, 10f ));
        boundaries.add(new Boundary(app, 5, app.height-100, 10f, 200f ));
        boundaries.add(new Boundary(app, app.width-5, app.height-100, 10f, 200f ));


        boundaries.add(new Boundary(app, app.width/4, app.height-50, 10f, 100f ));

        systems = new ArrayList<ParticleSystem>();
        font = app.createFont("Monospaced", 12);
        app.textFont(font);

    }

    int downKeys[] = new int[1024];

    public void keyPressed() {
        app.println(new Integer(app.keyCode));
        downKeys[app.keyCode] = 1;

        // rotates the grasped object with arrow keys
        if (grabbedBox != null) {
            if (app.keyCode == PConstants.LEFT) {
                grabbedBox.rotate(10);
            } else if (app.keyCode == PConstants.RIGHT) {
                grabbedBox.rotate(-10);
            } 
        }
    }
        
    public void keyReleased() {
        downKeys[app.keyCode] = 0;
    }
    
    boolean isKeyDown(int k) {
        return downKeys[k] == 1;
    }

    Box grabbedBox = null;

    void mouseReleased() {
        if (grabbedBox != null) {
            grabbedBox.setActive(true);
            grabbedBox = null;
        }
    }

    // return first box found which contains point (x, y)
    Box findBox(ArrayList<Box> boxlist, float x, float y) {
        for (Box b: boxlist) {
            if (b.contains(x, y)) {
                return b;
            }
        }
        return null;
    }


    void mousePressed() {
        // Add a new Particle System whenever the mouse is clicked
        System.out.println("key = "+app.keyCode);
        //        if (app.keyCode == 'l') {
        //            systems.add(new ParticleSystem(app, 0, new PVector(app.mouseX,app.mouseY)));

        // create a box when control-clicked
        if (isKeyDown(PConstants.CONTROL)) {
            float w = app.random(16, 256);
            float h = app.random(16, 64);
            float density = app.random(1, 4);
            app.println("new box "+ app.mouseX + ", "+ app.mouseY +", "+ w +"," + h +" density="+density);
            Box p = new Box(app, app.mouseX,app.mouseY, w, h, density);
            boxes.add(p);
        }

        Box touching = findBox(boxes, app.mouseX, app.mouseY);
        if (app.mousePressed && (touching != null)) {
            touching.moveTo(app.mouseX, app.mouseY);
            touching.setActive(false);
            grabbedBox = touching;
        }


    }

    void draw() {
        app.rectMode(PConstants.CORNER);
        app.background(255);
        app.fill(0);

        app.text("Ctrl-click to create box, click to grasp, left and right arrow to rotate", 10,12);
        if (app.mousePressed && (grabbedBox != null)) {
            grabbedBox.moveTo(app.mouseX, app.mouseY);
        }

        // We must always step through time!
        box2d.step();

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

         // Run all the particle systems
        for (ParticleSystem system: systems) {
            system.run();

            int n = (int) app.random(0,2);
            system.addParticles(n);
        }
    }

}


