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

    int WORLD_WIDTH = 4096;
    int WORLD_HEIGHT = 800;

    // Position of body in world
    public float xpos = 300;
    public float ypos = 300;
    float bodyWidth = 600;
    float bodyHeight = 600;

    // horizontal scroll speed for debugging
    float scrollspeed = 25.0f;

    ////////////////////////////////////////////////////////////////
    // Head and Eyes Controls

    // Computed from head gross and fine angles
    float gazeXpos = 0;
    float gazeYpos = 0;
    
    float headAzimuthAngle = 0;
    float headElevationAngle =0;

    // angles
    float gazeAzimuthAngle = 0;
    float gazeElevationAngle = 0;

    public Hand hand1;
    public Hand hand2;

    // max number of gross and fine motor steps that arms can take
    int reachX = 10;
    int reachY = 10;
    // arm motor step size
    int dGross = 100;
    int dFine = 10;

    public SensoriMotorSystem(JSchema a) {
        this.app = a;
        System.out.println("SensoriMotorSystem constructor this.app = "+this.app);
    }

    Object2D findObj(int index) {
        for (Plane plane: planes) {
            Object2D o = plane.findObj(index);
            if (o != null) { return o; }
        }
        return null;
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

        // hands start out in plane1
        hand1 = plane1.addHand( 500, 200, 32, 32, 5, app.color(0,255,0));
        hand2 = plane1.addHand( 800, 200, 32, 32, 5, app.color(255,0,0));
        hand1.alpha = 255;
        hand2.alpha = 255;
        hand1.setFixedRotation(true);
        hand2.setFixedRotation(true);


        plane0.addBox(800, 100, 100, 100, 4, app.color(255,40,30));
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
        // floor
        p.addBoundary(WORLD_WIDTH/2, app.height-5, WORLD_WIDTH, 10f );
        // left wall
        p.addBoundary(5,           app.height-200, 10f, 1200f );
        // right wall
        p.addBoundary(WORLD_WIDTH-5, app.height-200, 10f, 1200f );


        p.addBoundary(100, app.height-50, 10f, 100f );
        p.addBoundary(160, app.height-50, 10f, 100f );
        p.addBoundary(900, app.height-80, 10f, 160f );
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
        p.addBox(300, bottom-200, 200, 5, 6);
        p.addBox(300, bottom-200, 400, 5, 6);
        p.addBall(1000, bottom-100, 40);
        p.addBall(200, bottom-100, 40);
        p.addBall(250, bottom-100, 40);
        p.addBall(500, bottom-100, 30);
        p.addCustomShape1(50,bottom-100,app.color(123,201,122));
    }

    String showHandForces() {
        Vec2 f1 = hand1.getNetForce();
        Vec2 f2 = hand2.getNetForce();
        String hforces = String.format("hand1 Fx=%f Fy=%f, hand2 Fx=%f, Fy=%f,", f1.x, f1.y, f2.x, f2.y);
        return hforces;
    }


    void draw() {

        app.rectMode(PConstants.CORNER);
        app.background(255);
        app.fill(0);

        app.text("alt-click to create box, click to grasp, ctrl-click to lift, left and right arrow to rotate, shift for transparent", 20,12);
        app.text("plane="+planes.indexOf(currentPlane), 20,22);
        app.text("xpos="+xpos+ "   ypos="+ypos,20,32);
        app.text(showHandForces(),20,42);

        hand1.updatePosition(xpos,ypos);
        hand2.updatePosition(xpos,ypos);

        for (Plane plane: planes) {
            plane.draw();
        }


        // draw viewport and gaze location
        drawViewPort();

    }

    void drawViewPort() {
        // draw where the gaze is centered
        float dx, dy;
        dx = gazeXpos;
        dy = gazeYpos;
        float cx = app.width/2;
        float cy = app.height/2;

        app.strokeWeight(1);
        app.stroke(app.color(128,128,128,200));
        app.line(cx + dx - 50, cy-dy-50,
                 cx + dx + 50, cy-dy+50);
        app.line(cx + dx + 50, cy-dy-50,
                 cx + dx - 50, cy-dy+50);
        

        // draw the max range the hands can move
        


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


    void moveBody(float dx, float dy) {
        xpos += dx;
        xpos = (float)Math.max(0,xpos);
        xpos = (float)Math.min(WORLD_WIDTH-bodyWidth/2,xpos);

        ypos += dy;
        ypos = (float)Math.max(WORLD_HEIGHT-bodyHeight/2,ypos);
        ypos = (float)Math.min(0+bodyHeight/2,ypos);
    }

    public void keyPressed() {
        downKeys[app.keyCode] = 1;
        if (app.keyCode == PConstants.LEFT) {
            moveBody(-scrollspeed,0);
            setTranslation(xpos,ypos);
        } else if (app.keyCode == PConstants.RIGHT) {
            moveBody(scrollspeed,0);
            setTranslation(xpos,ypos);
        } else if (app.keyCode == PConstants.UP || app.keyCode == PConstants.DOWN) {
            // If we're interactively grasping an object with the mouse, move it to next plane
            Plane next = app.keyCode == PConstants.UP ? nextPlane() : prevPlane();
            if (currentPlane.pickedThing != null) {
                Object2D obj = currentPlane.pickedThing;
                currentPlane.mouseDropObject();
                Object2D newObj = obj.moveToPlane(next);
                app.println("newObj = "+newObj+" next="+next+" ... grasping on next plane");
                next.mouseGraspObject(newObj);
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


