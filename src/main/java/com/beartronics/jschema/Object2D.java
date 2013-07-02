package com.beartronics.jschema;

// The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2010
// PBox2D example

import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.dynamics.contacts.*;
import java.util.*;

import processing.core.PApplet;
import processing.core.*;
import processing.core.PConstants.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 


// A displayable 2D object
abstract class Object2D {

    // We need to keep track of a Body and a width and height
    public Body body;
    float density;
    Fixture fixture;
    int color;
    JSchema app;
    PBox2D box2d;
    float w,h;
    Plane plane;
    MouseJoint mouseJoint;
    WeldJoint weldJoint;
    DistanceJoint distanceJoint;
    int index;
    static int counter = 0;

    Object2D(Plane p) {
        this.plane = p;
        this.app = plane.app;
        this.box2d = plane.box2d;
        this.index = Object2D.counter++;
    }

    // Call this after creating body
    void makeBody() {
        body.setUserData(this);
    }
        
    boolean contains(float x, float y) {
        Vec2 worldPoint = box2d.coordPixelsToWorld(x, y);
        Fixture f = body.getFixtureList();
        boolean inside = f.testPoint(worldPoint);
        return inside;
    }


    // This function removes the particle from the box2d world
    void killBody() {
        box2d.destroyBody(body);
    }

    // Creates a new object on another plane, and returns it
    Object2D moveToPlane(Plane newPlane) {
        return this;
    }

    // Forces a move to this position, may cause non-physical behavior.
    void moveTo(float x, float y) {
        Vec2 worldPoint = box2d.coordPixelsToWorld(x, y);
        body.setAngularVelocity(0);
        body.setTransform(worldPoint, body.getAngle());

    }

    void applyTorque(float t) {
        body.applyTorque(t);
    }
    void applyAngularImpulse(float t) {
        body.applyAngularImpulse(t);
    }

    void setFixedRotation(boolean v) {
        body.setFixedRotation(v);
    }

    Vec2 getPosition() {
        return box2d.getBodyPixelCoord(body);
    }

    void rotate(float a) {
        float r = body.getAngle();
        r += Math.toRadians(a);
        Vec2 pos = body.getPosition();
        body.setTransform(pos, r);
    }


    float getAngle() {
        return (float)Math.toDegrees(body.getAngle());
    }

    void setAngle(float a) {
        double r = Math.toRadians(a);
        Vec2 pos = body.getPosition();
        body.setTransform(pos, (float)r);
    }

    void setSensor(boolean v) {
        app.println(this + "setSensor fixture = "+fixture+" values = "+v);
        fixture.setSensor(v);
    }

    void setActive(boolean a) {
        body.setActive(a);
    }

    // Is the particle ready for deletion?
    boolean done() {
        // Let's find the screen position of the particle
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Is it off the bottom of the screen?
        if (pos.y > app.height+w*h) {
            killBody();
            return true;
        }
        return false;
    }

    // Drawing the box
    abstract void display();


    ////////////////////////////////////////////////////////////////
    // MouseJoint methods, for interactively attaching mouse to an object

    // This is the key function where
    // we attach the spring to an x,y location
    // and the Box object's location
    void bindMouseJoint(float x, float y) {
        // Define the joint
        MouseJointDef md = new MouseJointDef();
        // Body A is just a fake ground body for simplicity (there isn't anything at the mouse)
        md.bodyA = box2d.getGroundBody();
        // Body 2 is the obj's objy
        md.bodyB = body;
        // Get the mouse location in world coordinates
        Vec2 mp = box2d.coordPixelsToWorld(x,y);
        // And that's the target
        md.target.set(mp);
        // Some stuff about how strong and bouncy the spring should be
        md.maxForce = 1000.0f * body.m_mass;
        md.frequencyHz = 5.0f;
        md.dampingRatio = 0.9f;

        // Make the joint!
        mouseJoint = (MouseJoint) box2d.world.createJoint(md);
    }

    void updateMouseJointPos(float x, float y) {
        if (mouseJoint != null) {
            // Always convert to world coordinates!
            Vec2 mouseWorld = box2d.coordPixelsToWorld(x,y);
            mouseJoint.setTarget(mouseWorld);
        }
    }

    void destroyMouseJoint() {
        if (mouseJoint != null) {
            box2d.world.destroyJoint(mouseJoint);
            mouseJoint = null;
        }
    }

    ////////////////////////////////////////////////////////////////
    // Weld Joints

    void bindWeldJoint(Object2D obj1, Object2D obj2) {
        WeldJointDef wd = new WeldJointDef();
        wd.collideConnected = false;
        wd.initialize(obj1.body, obj2.body, obj1.body.getWorldCenter());
        weldJoint = (WeldJoint) box2d.world.createJoint(wd);
    }

    void destroyWeldJoint() {
        if (weldJoint != null) {
            box2d.world.destroyJoint(weldJoint);
            weldJoint = null;
        }
    }

    /* Get list of contacting Object2Ds */
    public ArrayList<Object2D> getWeldedObjects() {
        JointEdge jedge = body.getJointList();
        ArrayList<Joint> jlist = new ArrayList<Joint>();
        while (jedge != null) {
            jlist.add(jedge.joint);
            jedge = jedge.next;
        }

        ArrayList<Object2D> objs = new  ArrayList<Object2D>();
        for (Joint j: jlist) {
            if (j instanceof WeldJoint) {
                Body bA = j.getBodyB();
                Body bB = j.getBodyA();
                Body b = (bA == body ? bB : bA);
                Object2D other = (Object2D) b.getUserData();
                app.print(String.format("getWeldedObjects bA=%s bB=%s", bA,bB));
                if (other != null) {
                    app.println(String.format("... obj=%s", other));
                    objs.add(other);
                } else {
                    app.println(" ... null obj");
                }
            }
        }
        return objs;
    }

    
    boolean removeWeldJoints() {
        return removeWeldJoints(null);
    }

    /** Remove all weld joints except to the body IGNORE
        @param ignore do not destroy a joint if it goes to object IGNORE
     @return true if an object was unwelded
*/
    boolean removeWeldJoints(Object2D ignore) {
        boolean removed = false;
        JointEdge jedge = body.getJointList();
        ArrayList<Joint> jlist = new ArrayList<Joint>();
        while (jedge != null) {
            jlist.add(jedge.joint);
            jedge = jedge.next;
        }
        for (Joint j: jlist) {
            if (j instanceof WeldJoint) {
                Body bA = j.getBodyB();
                Body bB = j.getBodyA();
                Object2D oA = null;
                Object2D oB = null;
                if (bA != null) {
                    oA = (Object2D) bA.getUserData();
                }
                if (bB != null) {
                    oB = (Object2D) bB.getUserData();
                }
                if (oA != ignore && oB != ignore) {
                    removed = true;
                    box2d.world.destroyJoint(j);
                }
            }
        }
        return removed;
    }



    ////////////////////////////////////////////////////////////////
    // 
    void bind(Object2D objA, Object2D objB, float len) {
        Vec2 p1, p2, d;
        // Define the joint
        DistanceJointDef djd = new DistanceJointDef();
        djd.bodyA = objA.body;
        // Body 2 is the obj's objy
        djd.bodyB = objB.body;
        djd.length = box2d.scalarPixelsToWorld(len);

        djd.frequencyHz = 5.0f;
        djd.dampingRatio = 0.9f;

        // Make the joint!
        distanceJoint = (DistanceJoint) box2d.world.createJoint(djd);
    }

    void weldContacts() {
        weldContacts(null);
    }


    // adds WeldJoint between thing and any bodies it touches
    void weldContacts(Object2D ignore) {
        app.println("********\nGrasp Contacts for "+this);
        ContactEdge cedge = body.getContactList();
        while (cedge != null) {
            Contact c = cedge.contact;
            Object2D other = (Object2D) cedge.other.getUserData();
            if (other != ignore && other != null) {
                app.println("welding "+this+" to obj "+other);

                bindWeldJoint(this, other);
            }
            cedge = cedge.next;
        }
    }



    void removeAllJoints() {
        JointEdge jedge = body.getJointList();
        ArrayList<Joint> jlist = new ArrayList<Joint>();
        while (jedge != null) {
            jlist.add(jedge.joint);
            jedge = jedge.next;
        }
        for (Joint j: jlist) {
            box2d.world.destroyJoint(j);
        }
    }





}


