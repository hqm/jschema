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

import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.Shape;

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


    // The Spring that will attach to the box from the mouse
    Spring spring;

    Spring gripperSpring1;
    Spring gripperSpring2;

    // The hands. Can be lifted 'out of the plane' by setting the body sensor flag on their fixture
    // Can sense collisions while lifted, equivalent to dragging hand over a surface.

    Box gripper1;
    Box gripper2;

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

        systems = new ArrayList<ParticleSystem>();
        font = app.createFont("Monospaced", 12);
        app.textFont(font);

        // Make the spring (it doesn't really get initialized until the mouse is clicked)
        spring = new Spring(app);

        initialBoundaries();
        initialBoxes();
        initialGrippers();

        worldState = new WorldState();

    }

    void initialBoundaries() {
        // Add a bunch of fixed boundaries
        boundaries.add(new Boundary(app, app.width/2, app.height-5, app.width, 10f ));

        boundaries.add(new Boundary(app, 5,           app.height-200, 10f, 400f ));
        boundaries.add(new Boundary(app, app.width-5, app.height-200, 10f, 400f ));


        boundaries.add(new Boundary(app, app.width/4, app.height-50, 10f, 100f ));
    }

    void initialGrippers() {
        int bottom = app.height;
        gripper1 = new Box(app, 500, bottom -200, 32, 32, 5, app.color(0,255,0));
        boxes.add(gripper1);
        gripper2 = new Box(app, 800, bottom -200, 32, 32, 5, app.color(255,0,0));
        boxes.add(gripper2);

    }

    void initialBoxes() {
        Box p;
        int bottom = app.height;
        boxes.add(new Box(app, 500, bottom -10, 64, 64, 1));
        boxes.add(new Box(app, 500, bottom-10, 64, 64, 2));
        boxes.add(new Box(app, 500, bottom-10, 32, 32, 2));
        boxes.add(new Box(app, 500, bottom-10, 64, 64, 2));
        boxes.add(new Box(app, 500, bottom-10, 32, 32, 2));
        boxes.add(new Box(app, 500, bottom-10, 64, 64, 1));
        boxes.add(new Box(app, 500, bottom-10, 64, 64, 4));
        boxes.add(new Box(app, 500, bottom-10, 64, 64, 8));

        boxes.add(new Box(app, 900, bottom-40, 400, 5, 6));
        boxes.add(new Box(app, 1000, bottom-10, 20, 64, 8));
        boxes.add(new Box(app, 1000, bottom-10, 20, 50, 8));
        boxes.add(new Box(app, 1000, bottom-10, 20, 100, 8));
        
    }


    int downKeys[] = new int[1024];

    public void keyPressed() {
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
            grabbedBox.setSensor(false);
            grabbedBox = null;

        }

        spring.destroy();
        
    }

    // return first box found which contains point (x, y)
    Box findBoxAt(ArrayList<Box> boxlist, float x, float y) {
        for (Box b: boxlist) {
            if (b.contains(x, y)) {
                return b;
            }
        }
        return null;
    }


    void mousePressed() {
        // Add a new Particle System whenever the mouse is clicked
        //        if (app.keyCode == 'l') {
        //            systems.add(new ParticleSystem(app, 0, new PVector(app.mouseX,app.mouseY)));

        // create a box when control-clicked
        if (isKeyDown(PConstants.ALT)) {
            float w = app.random(16, 256);
            float h = app.random(16, 64);
            float density = app.random(1, 10);
            app.println("new box "+ app.mouseX + ", "+ app.mouseY +", "+ w +"," + h +" density="+density);
            Box p = new Box(app, app.mouseX,app.mouseY, w, h, density);
            boxes.add(p);
        }

        Box touching = findBoxAt(boxes, app.mouseX, app.mouseY);
        if (app.mousePressed && (touching != null)) {
            app.println("grabbed "+touching);
            grabbedBox = touching;
            // And if so, bind the mouse location to the box with a spring
            spring.bind(app.mouseX,app.mouseY,grabbedBox);
            if (isKeyDown(PConstants.CONTROL)) {
                grabbedBox.setSensor(true);
            }
        }

    }


    void draw() {
        app.rectMode(PConstants.CORNER);
        app.background(255);
        app.fill(0);


        // Always alert the spring to the new mouse location
        spring.update(app.mouseX,app.mouseY);


        app.text("alt-click to create box, click to grasp, ctrl-click to lift, left and right arrow to rotate", 10,12);

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

        // Draw the spring (it only appears when active)
        spring.display();

         // Run all the particle systems
        for (ParticleSystem system: systems) {
            system.run();

            int n = (int) app.random(0,2);
            system.addParticles(n);
        }
    }


    

    WorldState worldState;

    public WorldState getWorldState() {
        return worldState;
    }

    /// Fills in the sensory input values
    public WorldState computeWorldState() {
        computeTouchSensors();
        computeVisionSensor();
        computeAudioSensors();
        
        return worldState;
    }

    void computeAudioSensors() {
    }

    void computeVisionSensor() {
    }

    // Includes proprioceptive sensors
    void computeTouchSensors() {

        // update joint position sensors

        // update joint force sensors

        // update gripper touch and force sensors

        

    }


}


