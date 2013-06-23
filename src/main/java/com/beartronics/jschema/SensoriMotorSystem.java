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
    public Plane plane1; //front
    public Plane plane2; //back
    public JSchema app;

    public SensoriMotorSystem(JSchema a) {
        this.app = a;
        System.out.println("SensoriMotorSystem constructor this.app = "+this.app);
    }

    PFont font;
    ArrayList<Plane> planes = new ArrayList<Plane>();

    void setupDisplay() {
        // Initialize box2d physics and create the world
        plane1 = new Plane(app);
        //        plane2 = new Plane(app);

        // Initialize box2d physics and create the world
        planes.add(plane1);
        //        planes.add(plane2);
        
        for (Plane plane: planes) {
            plane.setup();
        }

        app.smooth();

        font = app.createFont("Monospaced", 12);
        app.textFont(font);

        plane1.initialBoundaries();
        plane1.initialPhysobjs();
        plane1.initialGrippers();

        worldState = new WorldState();

    }

    int downKeys[] = new int[1024];

    public void keyPressed() {
        downKeys[app.keyCode] = 1;
        plane1.keyPressed();
    }
        
    public void keyReleased() {
        downKeys[app.keyCode] = 0;
        plane1.keyReleased();
    }
    
    boolean isKeyDown(int k) {
        return downKeys[k] == 1;
    }

    void mouseReleased() {
        plane1.mouseReleased();
    }

    void mousePressed() {
        plane1.mousePressed();
    }

    void draw() {
        app.rectMode(PConstants.CORNER);
        app.background(255);
        app.fill(0);

        app.text("alt-click to create box, click to grasp, ctrl-click to lift, left and right arrow to rotate", 10,12);

        plane1.draw();
        for (Plane plane: planes) {
            //            plane.draw();
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


