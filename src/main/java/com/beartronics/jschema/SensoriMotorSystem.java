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


    public JSchema app;

    // Object planes
    public Plane plane0; // back plane
    public Plane plane1; // front plane

    final int WORLD_WIDTH = 4096;

    // Position of body in world
    float xpos = 0;
    float ypos = 0;

    // horizontal scroll speed for debugging
    float scrollspeed = 25.0f;

    ////////////////////////////////////////////////////////////////
    // Head and Eyes Controls

    // Computed from head gross and fine angles
    float gazeXpos = 400;
    float gazeYpos = 200;
    
    float headAzimuthAngle = 0;
    float headElevationAngle =0;

    // angles
    float gazeAzimuthAngle = 0;
    float gazeElevationAngle = 0;


    public SensoriMotorSystem(JSchema a) {
        this.app = a;
        System.out.println("SensoriMotorSystem constructor this.app = "+this.app);
    }

    PFont font;
    ArrayList<Plane> planes = new ArrayList<Plane>();
    Plane currentPlane;

    int marker_color;

    void setupDisplay() {
        // Initialize box2d physics and create the world
        plane0 = new Plane(app, app.color(255, 55, 55));
        plane1 = new Plane(app, app.color(0,0,0));

        marker_color = app.color(189,22,198,128);
        // Initialize box2d physics and create the world
        planes.add(plane0);
        planes.add(plane1);

        currentPlane = plane1;
        
        for (Plane plane: planes) {
            plane.setup();
        }

        app.smooth();

        font = app.createFont("Monospaced", 12);
        app.textFont(font);

        initialBoundaries(plane1);
        initialBoundaries(plane0);
        initialPhysobjs(plane1);
        plane1.initialGrippers();

        // TODO add grippers
        //plane2.addBox(500, 100, 100, 100, 4);
        plane0.addBox(800, 100, 100, 100, 4, app.color(255,40,30));
        //plane2.addBox(900, 100, 50, 50, 7);
        plane0.addBox(1000, 100, 50, 50, 8, app.color(87,191,22));

        worldState = new WorldState();

    }

    void setTranslation(float dx, float dy) {
        for (Plane plane: planes) {
            plane.setTranslation(dx,dy);
        }
    }



    void initialBoundaries(Plane p) {
        // Add a bunch of fixed boundaries
        p.addBoundary(WORLD_WIDTH/2, app.height-5, WORLD_WIDTH, 10f );
        p.addBoundary(5,           app.height-200, 10f, 1200f );
        p.addBoundary(WORLD_WIDTH-5, app.height-200, 10f, 1200f );
        p.addBoundary(400, app.height-50, 10f, 100f );
        p.addBoundary(800, app.height-80, 10f, 160f );
        p.addBoundary(1200, app.height-100, 10f, 200f );

        // add markers to help locate position

        for (int x = 200; x < WORLD_WIDTH; x+=200) {
            p.addBoundary(x, app.height-400, 20+x/100f, 20+x/50f, marker_color );
        }

    }

    void initialPhysobjs(Plane p) {
        int bottom = app.height;
        p.addBox(500, bottom -10, 64, 64, 1);
        p.addBox(500, bottom-10, 64, 64, 2);
        p.addBox(800, bottom-10, 32, 32, 2);
        p.addBox(1200, bottom-10, 64, 64, 2);
        p.addBox(1500, bottom-10, 64, 64, 1);
        p.addBox(2000, bottom-10, 64, 64, 10);
        p.addBox(300, bottom-200, 400, 5, 6);
        p.addBall(1000, bottom-100, 40);
        p.addBall(100, bottom-100, 40);
        p.addBall(200, bottom-100, 40);
        p.addBall(500, bottom-100, 30);
    }

    void draw() {

        app.rectMode(PConstants.CORNER);
        app.background(255);
        app.fill(0);

        app.text("alt-click to create box, click to grasp, ctrl-click to lift, left and right arrow to rotate, shift for transparent", 20,12);
        app.text("plane="+planes.indexOf(currentPlane), 20,22);
        app.text("xpos="+xpos,20,32);

        for (Plane plane: planes) {
            plane.draw();
        }


        // draw viewport and gaze location
        drawViewPort();

    }

    void drawViewPort() {
        float dx, dy;
        dx = gazeXpos;
        dy = gazeYpos;

        app.strokeWeight(1);
        app.stroke(app.color(128,128,128,200));
        app.line(xpos + dx - 50, ypos+dy-50,
                 xpos + dx + 50, ypos+dy+50);
        app.line(xpos + dx + 50, ypos+dy-50,
                 xpos + dx - 50, ypos+dy+50);
        
    }


    int downKeys[] = new int[1024];

    Plane nextPlane() {
        int idx = planes.indexOf(currentPlane);
        idx = (idx+1) % planes.size();
        return planes.get(idx);
    }

    Plane prevPlane() {
        int idx = planes.indexOf(currentPlane);
        idx = (idx-1);
        if (idx < 0) {
            idx = planes.size()-1;
        }
        return planes.get(idx);
    }


    public void keyPressed() {
        downKeys[app.keyCode] = 1;
        if (app.keyCode == PConstants.LEFT) {
            xpos = (float)Math.max(0,xpos-scrollspeed);
            setTranslation(xpos,ypos);
        } else if (app.keyCode == PConstants.RIGHT) {
            xpos = (float)Math.min(WORLD_WIDTH-app.width,xpos+scrollspeed);
            setTranslation(xpos,ypos);
        } else if (app.keyCode == PConstants.UP || app.keyCode == PConstants.DOWN) {
            // If we're grasping an object, move it to next plane
            Plane next = app.keyCode == PConstants.UP ? nextPlane() : prevPlane();
            if (currentPlane.grabbedThing != null) {
                Object2D obj = currentPlane.grabbedThing;
                currentPlane.dropObject();
                Object2D newObj = obj.moveToPlane(next);
                app.println("newObj = "+newObj+" next="+next+" ... grasping on next plane");
                next.graspObject(newObj);
            }
            currentPlane = next;
        }
        currentPlane.keyPressed();
        if (app.keyCode == PConstants.SHIFT) {
            for (Plane plane: planes) {
                plane.setTransparent(true);
            }
        }


    }
        
    public void keyReleased() {
        downKeys[app.keyCode] = 0;
        currentPlane.keyReleased();
        if (app.keyCode == PConstants.SHIFT) {
            for (Plane plane: planes) {
                plane.setTransparent(false);
            }
        }

    }
    
    boolean isKeyDown(int k) {
        return downKeys[k] == 1;
    }

    void mouseReleased() {
        currentPlane.mouseReleased();
    }

    void mousePressed() {
        currentPlane.mousePressed();
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


