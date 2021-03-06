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
import org.jbox2d.collision.ManifoldPoint;


import processing.core.*;


// A Plane holds a surface of 2d objects
public class Plane implements ContactListener {

    // A reference to our box2d world
    public PBox2D box2d;
    public JSchema app;
    public SensoriMotorSystem sms;

    int borderColor = 0;

    // For debugging, the user can grab an object with the mouse interactively
    Object2D pickedThing = null;

    public Plane(JSchema a, int color) {
        this.app = a;
        this.sms = a.sms;
        this.borderColor = color;
        System.out.println("Plane constructor this.app = "+this.app);
    }

    boolean transparent = false;
    int alpha = 255;

    // A list for all of our rectangles
    public ArrayList<Object2D> physobjs;

    PFont font;

    Object2D findObj(int index) {
        for (Object2D obj: physobjs) {
            if (obj.index == index) {
                return obj;
            }
        }
        return null;
    }


    void setup() {
        box2d = app.createBox2D();
        box2d.createWorld();
        box2d.world.setContactListener(this);

        // We are setting a custom gravity
        box2d.setGravity(0, app.stage.gravity);

        // Create ArrayLists  
        physobjs = new ArrayList<Object2D>();

        // Make the spring (it doesn't really get initialized until the mouse is clicked)
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


    Boundary addBoundary(float x, float y,float w,float h) {
        return addBoundary(x, y, w, h, sms.getNextObjectColor() );
    }

    private void addPhysobj(Object2D obj, int color) {
        physobjs.add(obj);
        sms.addObjectColorMapping(obj,color);
    }


    Boundary addBoundary(float x, float y,float w,float h, int color) {
        Boundary b = new Boundary(this, x, y, w, h, color );
        addPhysobj(b, color);
        return b;
    }


    Hand addHand(float x, float y,float w,float h, float density, int color) {
        Hand hand = new Hand(this, x,  y, w, h, density, color);
        addPhysobj(hand, color);
        return hand;
    }

    Box addBox(float x, float y,float w,float h, float density, int color) {
        Box box = new Box(this, x, y, w, h, density, color);
        addPhysobj(box, color);
        return box;
    }

    Box addBox(float x, float y,float w,float h, float density) {
        int color = sms.getNextObjectColor();
        Box box = new Box(this, x, y, w, h, density, color);
        app.logger.info("adding box color "+color +" " +box);
        addPhysobj(box, color);
        return box;
    }

    
    Ball addBall(float x, float y,float r) {
        int color = sms.getNextObjectColor();
        Ball b = new Ball(this, x, y, r, color);
        addPhysobj(b, color);
        return b;
    }

    int downKeys[] = new int[1024];

    public void keyPressed() {
        //app.println("key "+app.keyCode);
        downKeys[app.keyCode] = 1;
        if (pickedThing != null) {
            showContacts(pickedThing);
            if (app.key == 'g') {
                pickedThing.weldContacts();
            } else if (app.key == 'd') {
                pickedThing.removeWeldJoints();
            }
            
        }
    }

    void showContacts(Object2D thing) {
        app.println("********\nContacts for "+thing);
        ContactEdge cedge = thing.body.getContactList();
        while (cedge != null) {
            Contact c = cedge.contact;
            Object2D other = (Object2D) cedge.other.getUserData();
            app.println(" obj "+other);
            cedge = cedge.next;
        }
    }



        
    public void keyReleased() {
        downKeys[app.keyCode] = 0;
    }
    
    boolean isKeyDown(int k) {
        return downKeys[k] == 1;
    }

    void mouseReleased() {
        mouseDropObject();
    }

    void mouseGraspObject(Object2D thing) {
        thing.bindMouseJoint(mouseX(),mouseY());
        pickedThing = thing;
    }

    // Drop what the mouse is holding. But if it's a Hand, don't destroy it's mouseJoint.
    public void mouseDropObject() {
        if (pickedThing != null) {
            pickedThing.destroyMouseJoint();
            if (pickedThing instanceof Hand) {
                // set the hand position to this location
                ((Hand) pickedThing).setupMouseJoint();
            }
            pickedThing.setSensor(false);
            pickedThing = null;
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

    public Object2D findObjAt(float x, float y) {
        return findObjAt(physobjs, x, y);
    }


    ArrayList<Object2D> findObjectsAt(float x, float y) {
        ArrayList<Object2D> items = new ArrayList<Object2D>();
        for (Object2D b: physobjs) {
            if (b.contains(x, y)) {
                items.add(b);
            }
        }
        return items;
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
            Box p = addBox( mouseX(),mouseY(), w, h, density);
            physobjs.add(p);
        }

        Object2D touching = findObjAt(physobjs, mouseX(), mouseY());
        app.println("plane mousePressed touching="+touching);
        if (app.mousePressed && (touching != null)) {
            app.println("grabbed "+touching);
            mouseGraspObject(touching);
            if (isKeyDown(PConstants.CONTROL)) {
                pickedThing.setSensor(true);
            }
        }

    }

    float mouseX() {
        float v = app.mouseX + translateX - (app.width/2);
        return v;
    }

    float mouseY() {
        float v = app.mouseY;
        return v;
    }

    float translateX;
    float translateY;

    void setTranslation(float x, float y) {
        translateX = x;
        translateY = y;
    }


    /* move the all the cells in the visual field to current head location */
    void updateHeadPosition(float x, float y) {
    }

    float ROTATIONAL_IMPULSE = 1000f;
    float ROTATION_INCR = 0.5f;
    int L_KEY = 76;
    int R_KEY = 82;
    void step() {

        // rotates the grasped object with arrow keys
        if (pickedThing != null) {
            if (isKeyDown(L_KEY)) {
                pickedThing.body.setAngularVelocity(0);
                pickedThing.body.applyAngularImpulse (ROTATIONAL_IMPULSE);
                pickedThing.rotate(ROTATION_INCR);
            } else if (isKeyDown(R_KEY)) {
                pickedThing.body.setAngularVelocity(0);
                pickedThing.body.applyAngularImpulse (-ROTATIONAL_IMPULSE);
                pickedThing.rotate(-ROTATION_INCR);
            } 
        }




        app.pushMatrix();
        app.translate(-translateX+(app.width/2), 0);


        app.pushStyle();
        if (transparent) {
            app.fill(0,100);
        }
        // Always alert the spring to the new mouse location
        if (pickedThing != null) {
            pickedThing.updateMouseJointPos(mouseX(),mouseY());
        }

        box2d.step();

        // Display all the physobjs
        for (Object2D b: physobjs) {
            b.display(app.g);
        }

        // Physobjs that leave the screen, we delete them
        // (note they have to be deleted from both the box2d world and our list
        for (int i = physobjs.size()-1; i >= 0; i--) {
            Object2D b = physobjs.get(i);
            if (b.done()) {
                physobjs.remove(i);
            }
        }

        app.popStyle();

        app.popMatrix();


    }

    /**
       Render the objects onto the retina canvas, centered around the current
       gaze position.
     */
    void drawRetina(PGraphics pg) {
        pg.pushMatrix();
        pg.translate(-translateX-sms.gazeXpos+ (pg.width/2), -translateY - sms.gazeYpos + pg.height/2);
        pg.pushStyle();

        // Display all the physobjs
        for (Object2D b: physobjs) {
            b.display(pg);
        }

        pg.popStyle();
        pg.popMatrix();
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
        if (f1.isSensor() || f2.isSensor()) {
            Body b1 = f1.getBody();
            Body b2 = f2.getBody();

            // Get our objects that reference these bodies
            Object2D o1 = (Object2D) b1.getUserData();
            Object2D o2 = (Object2D) b2.getUserData();

            if (o1.getClass() == VisualBox.class) {
                ((VisualBox) o1).addSensed(o2);
            } else if (o2.getClass() == VisualBox.class) {
                ((VisualBox) o2).addSensed(o1);
            }
        }
    }

    // Objects stop touching each other
    public void endContact(Contact cp) {
        // Get both fixtures
        Fixture f1 = cp.getFixtureA();
        Fixture f2 = cp.getFixtureB();
        // Get both bodies
        if (f1.isSensor() || f2.isSensor()) {
            Body b1 = f1.getBody();
            Body b2 = f2.getBody();

            // Get our objects that reference these bodies
            Object2D o1 = (Object2D) b1.getUserData();
            Object2D o2 = (Object2D) b2.getUserData();

            if (o1.getClass() == VisualBox.class) {
                ((VisualBox) o1).removeSensed(o2);
            } else if (o2.getClass() == VisualBox.class) {
                ((VisualBox) o2).removeSensed(o1);
            }
        }
    }


    // Gives us a chance to find the normal forces on contacting objects
    public void postSolve(Contact cp, ContactImpulse ci) {
    }

    public void preSolve(Contact cp, Manifold m) {

    }


}


