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


// A Plane holds a surface of 2d objects
public class Plane {

    // A reference to our box2d world
    public PBox2D box2d;
    public JSchema app;

    public Plane(JSchema a) {
        this.app = a;

        System.out.println("Plane constructor this.app = "+this.app);
    }

    boolean transparent = false;
    int alpha = 255;

    // A list we'll use to track fixed objects
    public ArrayList<Boundary> boundaries;
    // A list for all of our rectangles
    public ArrayList<Object2D> physobjs;

    // A list for all particle systems
    ArrayList<ParticleSystem> systems;


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
        // We are setting a custom gravity
        box2d.setGravity(0, -10);

        // Create ArrayLists  
        physobjs = new ArrayList<Object2D>();
        boundaries = new ArrayList<Boundary>();

        systems = new ArrayList<ParticleSystem>();
        // Make the spring (it doesn't really get initialized until the mouse is clicked)
        spring = new Spring(this);
        worldState = new WorldState();
    }

    void initialBoundaries() {
        // Add a bunch of fixed boundaries
        boundaries.add(new Boundary(this, app.width/2, app.height-5, app.width, 10f ));

        boundaries.add(new Boundary(this, 5,           app.height-200, 10f, 400f ));
        boundaries.add(new Boundary(this, app.width-5, app.height-200, 10f, 400f ));


        boundaries.add(new Boundary(this, app.width/4, app.height-50, 10f, 100f ));
    }

    void initialBoundaries2() {
        // Add a bunch of fixed boundaries
        // bottom
        boundaries.add(new Boundary(this, app.width/2, app.height-5, app.width, 10f ));

        //left
        boundaries.add(new Boundary(this, 5,           app.height-200, 10f, 400f ));
        //right
        boundaries.add(new Boundary(this, app.width-5, app.height-200, 10f, 400f ));


        // obstacle
        //boundaries.add(new Boundary(this, app.width/4, app.height-50, 10f, 100f ));
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


    void initialGrippers() {
        int bottom = app.height;
        gripper1 = new Box(this, 500, bottom -200, 32, 32, 5, app.color(0,255,0));
        physobjs.add(gripper1);
        gripper2 = new Box(this, 800, bottom -200, 32, 32, 5, app.color(255,0,0));
        physobjs.add(gripper2);

    }

    void addBoundary(float x, float y,float w,float h) {
        boundaries.add(new Boundary(this, x, y, w, h ));
    }

    void addBox(float x, float y,float w,float h, float density) {
        int bottom = app.height;
        physobjs.add(new Box(this, x, bottom - y, w, h, density));
    }
    void addBox(float x, float y,float w,float h, float density, int color) {
        int bottom = app.height;
        physobjs.add(new Box(this, x, bottom - y, w, h, density, color));
    }



    void addBall(float x, float y,float r) {
        int bottom = app.height;
        physobjs.add(new Ball(this, x, bottom - y, r));
    }

        
    void initialPhysobjs() {
        int bottom = app.height;
        physobjs.add(new Box(this, 500, bottom -10, 64, 64, 1));
        physobjs.add(new Box(this, 500, bottom-10, 64, 64, 2));
        physobjs.add(new Box(this, 500, bottom-10, 32, 32, 2));
        physobjs.add(new Box(this, 500, bottom-10, 64, 64, 2));
        //physobjs.add(new Box(this, 500, bottom-10, 32, 32, 2));
        physobjs.add(new Box(this, 500, bottom-10, 64, 64, 1));
        //        physobjs.add(new Box(this, 500, bottom-10, 64, 64, 10));
        physobjs.add(new Box(this, 500, bottom-10, 64, 64, 10));

        physobjs.add(new Box(this, 900, bottom-40, 400, 5, 6));
        //physobjs.add(new Box(this, 1000, bottom-10, 20, 64, 8));
        //        physobjs.add(new Box(this, 1000, bottom-10, 20, 50, 8));
        //physobjs.add(new Box(this, 1000, bottom-10, 20, 100, 8));


        physobjs.add(new Ball(this, 1000, bottom-100, 40));
        physobjs.add(new Ball(this, 800, bottom-100, 40));
        physobjs.add(new Ball(this, 800, bottom-100, 40));
        //        physobjs.add(new Ball(this, 800, bottom-200, 20));
        //physobjs.add(new Ball(this, 800, bottom-200, 20));
        //physobjs.add(new Ball(this, 800, bottom-200, 20));
        
    }


    int downKeys[] = new int[1024];

    public void keyPressed() {
        downKeys[app.keyCode] = 1;
        // rotates the grasped object with arrow keys
        if (grabbedThing != null) {
            if (app.keyCode == PConstants.LEFT) {
                grabbedThing.rotate(10);
            } else if (app.keyCode == PConstants.RIGHT) {
                grabbedThing.rotate(-10);
            } 
        }
        if (app.keyCode == PConstants.SHIFT) {
            setTransparent(true);
        }

    }
        
    public void keyReleased() {
        downKeys[app.keyCode] = 0;
        if (app.keyCode == PConstants.SHIFT) {
            setTransparent(false);
        }
    }
    
    boolean isKeyDown(int k) {
        return downKeys[k] == 1;
    }

    Object2D grabbedThing = null;

    void mouseReleased() {
        if (grabbedThing != null) {
            grabbedThing.setSensor(false);
            grabbedThing = null;

        }

        spring.destroy();
        
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

        // create a box when control-clicked
        if (isKeyDown(PConstants.ALT)) {
            float w = app.random(16, 256);
            float h = app.random(16, 64);
            float density = app.random(1, 10);
            app.println("new box "+ app.mouseX + ", "+ app.mouseY +", "+ w +"," + h +" density="+density);
            Box p = new Box(this, app.mouseX,app.mouseY, w, h, density);
            physobjs.add(p);
        }

        Object2D touching = findObjAt(physobjs, app.mouseX, app.mouseY);
        app.println("plane mousePressed touching="+touching);
        if (app.mousePressed && (touching != null)) {
            app.println("grabbed "+touching);
            grabbedThing = touching;
            // And if so, bind the mouse location to the box with a spring
            spring.bind(app.mouseX,app.mouseY,grabbedThing);
            if (isKeyDown(PConstants.CONTROL)) {
                grabbedThing.setSensor(true);
            }
        }

    }


    void draw() {
        app.pushStyle();
        if (transparent) {
            app.fill(0,100);
        }
        // Always alert the spring to the new mouse location
        spring.update(app.mouseX,app.mouseY);

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
        spring.display();

        // Run all the particle systems
        for (ParticleSystem system: systems) {
            system.run();

            int n = (int) app.random(0,2);
            system.addParticles(n);
        }
        app.popStyle();
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


