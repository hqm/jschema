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
import org.jbox2d.dynamics.contacts.*;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;


import processing.core.*;


// A Plane holds a surface of 2d objects
public class Plane implements ContactListener {

    // A reference to our box2d world
    public PBox2D box2d;
    public JSchema app;

    float xpos = 0;
    float ypos = 0;

    int borderColor = 0;


    public Plane(JSchema a, int color) {
        this.app = a;
        this.borderColor = color;

        System.out.println("Plane constructor this.app = "+this.app);
    }

    boolean transparent = false;
    int alpha = 255;

    // A list we'll use to track fixed objects
    public ArrayList<Boundary> boundaries;
    // A list for all of our rectangles
    public ArrayList<Object2D> physobjs;


    // The Spring that will attach to the box from the mouse
    Spring spring;
    Spring gripperSpring1;
    Spring gripperSpring2;

    // The hands. Can be lifted 'out of the plane' by setting the body sensor flag on their fixture
    // Can sense collisions while lifted, equivalent to dragging hand over a surface.

    Object2D gripper1;
    Object2D gripper2;

    PFont font;

    void setup() {
        box2d = app.createBox2D();

        box2d.createWorld();

        box2d.world.setContactListener(this);


        // We are setting a custom gravity
        box2d.setGravity(0, -10);

        // Create ArrayLists  
        physobjs = new ArrayList<Object2D>();
        boundaries = new ArrayList<Boundary>();

        // Make the spring (it doesn't really get initialized until the mouse is clicked)
        spring = new Spring(this);
        worldState = new WorldState();
    }


    void setTransparent(boolean v) {
        app.println("setTransparent "+v);
        this.transparent = v;
        if (v) {
            this.alpha = 75;
        } else {
            this.alpha = 255;
        }
                
    }

    void removeObject(Object2D obj) {
        physobjs.remove(obj);
    }

    void addObject(Object2D obj) {
        physobjs.add(obj);
    }

    void initialGrippers() {
        int bottom = app.height;
        gripper1 = new Box(this, 500, bottom -200, 32, 32, 5, app.color(0,255,0));
        physobjs.add(gripper1);
        gripper2 = new Box(this, 800, bottom -200, 32, 32, 5, app.color(255,0,0));
        physobjs.add(gripper2);

    }

    Boundary addBoundary(float x, float y,float w,float h) {
        return addBoundary(x, y, w, h, 0 );
    }


    Boundary addBoundary(float x, float y,float w,float h, int color) {
        Boundary b = new Boundary(this, x, y, w, h, color );
        boundaries.add(b);
        return b;
    }

    Box addBox(float x, float y,float w,float h, float density) {
        Box box = new Box(this, x,  y, w, h, density);
        physobjs.add(box);
        return box;
    }

    Box addBox(float x, float y,float w,float h, float density, int color) {
        Box box = new Box(this, x, y, w, h, density, color);
        physobjs.add(box);
        return box;
    }
    
    Ball addBall(float x, float y,float r) {
        Ball b = new Ball(this, x, y, r);
        physobjs.add(b);
        return b;
    }

    int downKeys[] = new int[1024];

    public void keyPressed() {
        downKeys[app.keyCode] = 1;
        // rotates the grasped object with arrow keys
        if (grabbedThing != null) {
            if (app.key == 'l') {
                grabbedThing.rotate(10);
            } else if (app.key == 'r') {
                grabbedThing.rotate(-10);
            } 
        }
    }
        
    public void keyReleased() {
        downKeys[app.keyCode] = 0;
    }
    
    boolean isKeyDown(int k) {
        return downKeys[k] == 1;
    }

    Object2D grabbedThing = null;

    void mouseReleased() {
        dropObject();
    }

    void dropObject() {
        if (grabbedThing != null) {
            grabbedThing.setSensor(false);
            grabbedThing = null;

        }
        if (spring != null) {
            spring.destroy();
            spring = null;
        }
    }

    // return first box found which contains point (x, y)
    Object2D findObjAt(ArrayList<Object2D> objlist, float x, float y) {
        for (Object2D b: objlist) {
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

        // create a box when alt-clicked
        if (isKeyDown(PConstants.ALT)) {
            float w = app.random(16, 256);
            float h = app.random(16, 64);
            float density = app.random(1, 10);
            app.println("new box "+ mouseX() + ", "+ mouseY() +", "+ w +"," + h +" density="+density);
            Box p = new Box(this, mouseX(),mouseY(), w, h, density);
            physobjs.add(p);
        }

        Object2D touching = findObjAt(physobjs, mouseX(), mouseY());
        app.println("plane mousePressed touching="+touching);
        if (app.mousePressed && (touching != null)) {
            app.println("grabbed "+touching);
            graspObject(touching);
            if (isKeyDown(PConstants.CONTROL)) {
                grabbedThing.setSensor(true);
            }
        }

    }

    float mouseX() {
        return app.mouseX + xpos;
    }

    float mouseY() {
        return app.mouseY + ypos;
    }

    void graspObject(Object2D thing) {
        if (spring == null) {
            spring = new Spring(this);
        }
        spring.bind(mouseX(),mouseY(),thing);
        grabbedThing = thing;
    }

    void setTranslation(float dx, float dy) {
        xpos = dx;
        ypos = dy;
    }

    void step() {
        box2d.step();
    }

    void draw() {
        app.pushMatrix();
        app.translate(-xpos,0);


        app.pushStyle();
        if (transparent) {
            app.fill(0,100);
        }
        // Always alert the spring to the new mouse location
        if (spring != null) {
            spring.update(mouseX(),mouseY());
        }

        box2d.step();

        // Display all the boundaries
        for (Boundary wall: boundaries) {
            wall.display();
        }

        // Display all the physobjs
        for (Object2D b: physobjs) {
            b.display();
        }

        // Physobjs that leave the screen, we delete them
        // (note they have to be deleted from both the box2d world and our list
        for (int i = physobjs.size()-1; i >= 0; i--) {
            Object2D b = physobjs.get(i);
            if (b.done()) {
                physobjs.remove(i);
            }
        }

        // Draw the spring (it only appears when active)
        if (spring != null) {
            spring.display();
        }

        app.popStyle();

        app.popMatrix();


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


    // Collision event functions!
    public void beginContact(Contact cp) {
        // Get both fixtures
        Fixture f1 = cp.getFixtureA();
        Fixture f2 = cp.getFixtureB();
        // Get both bodies
        Body b1 = f1.getBody();
        Body b2 = f2.getBody();

        // Get our objects that reference these bodies
        Object2D o1 = (Object2D) b1.getUserData();
        Object2D o2 = (Object2D) b2.getUserData();

        app.println("beginContact "+cp +" o1="+o1+" o2="+o2);

        if (o1.getClass() == Box.class && o2.getClass() == Box.class) {
            Box p1 = (Box) o1;
            //            p1.setColor(app.color(0,0,255));
            Box p2 = (Box) o2;
            //            p2.setColor(app.color(0,0,255));
        }
    }

    // Objects stop touching each other
    public void endContact(Contact cp) {
    }

    public void postSolve(Contact c, ContactImpulse ci) {
    }


    public void preSolve(Contact c, Manifold m) {
    }


}


