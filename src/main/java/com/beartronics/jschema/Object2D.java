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
import org.jbox2d.collision.*;
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
    public Fixture fixture;
    public int color;
    public JSchema app;
    public PBox2D box2d;
    public float w,h;
    public Plane plane;
    public MouseJoint mouseJoint;
    public  WeldJoint gripJoint;
    public DistanceJoint distanceJoint;
    public int index;
    static int counter = 0;

    Object2D(Plane p) {
        this.plane = p;
        this.app = plane.app;
        this.box2d = plane.box2d;
        this.index = Object2D.counter++;
    }

    public void lock(boolean v) {
        if (v) {
            body.setType(BodyType.STATIC);
        } else {
            body.setType(BodyType.DYNAMIC);
        }
    }

    public void lock(Body b, boolean v) {
        if (v) {
            b.setType(BodyType.STATIC);
        } else {
            b.setType(BodyType.DYNAMIC);
        }
    }


    boolean isHollow() {
        return false;
    }

    boolean isSolid() {
        return true;
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

    /** 
        @return list of objects that are touching our boundary
    */
    HashSet<Object2D> touchingObjects() {
        HashSet<Object2D> t = new HashSet<Object2D>();
        ContactEdge cedge = body.getContactList();
        while (cedge != null) {
            Contact c = cedge.contact;
            if (c.getManifold().pointCount > 0) {
                Fixture a = c.getFixtureA();
                Fixture b = c.getFixtureB();
                Body ba = a.getBody();
                Body bb = b.getBody();
                Object objA = ba.getUserData();
                Object objB = bb.getUserData();
                if (objA != null && objA instanceof Object2D && objA != this) {
                    t.add((Object2D)objA);
                }
                if (objB != null && objB instanceof Object2D && objB != this) {
                        t.add((Object2D)objB);
                }
            }
            cedge = cedge.next;
        }
        return t;
    }

    // This function removes the particle from the box2d world
    void killBody() {
        box2d.destroyBody(body);
    }

    /** Moves this object to a different plane.
     * The way this now works is
     * + create a copy of our body, on the target plane.
     * + kill the body on this plane
     * + destroy any existing joints (? is this needed if we call killBody?)
     * + remove us from the physobjs list of the old and add to new plane
     * + update our plane and box2d fields

     */
    void moveToPlane(Plane newPlane) {
        plane.removeObject(this);
        newPlane.addObject(this);
        destroyMouseJoint();
        destroyWeldJoint();
    }

    // Forces a move to this position, may cause non-physical behavior.
    void moveTo(float xpos, float ypos) {
        Vec2 worldPoint = box2d.coordPixelsToWorld(xpos, ypos);
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
        if (mouseJoint != null) {
            destroyMouseJoint();
        }
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

    /** Welds two objects together with a WeldJoint
     * param obj1
     * param obj2
     */
    void weld(Object2D obj1, Object2D obj2) {
        app.println("weld "+obj1+" to "+obj2);
        WeldJointDef wd = new WeldJointDef();
        //wd.collideConnected = false;
        wd.initialize(obj1.body, obj2.body, obj1.body.getWorldCenter());
        gripJoint = (WeldJoint) box2d.world.createJoint(wd);
    }


    /** Remove the WeldJoint we use for gripping. Used primarily by the Hand class.
     */
    void destroyWeldJoint() {
        if (gripJoint != null) {

            box2d.world.destroyJoint(gripJoint);
            gripJoint = null;
        }
    }

    /** Returns list of Object2Ds which are joined via a WeldJoint */
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
                //app.print(String.format("getWeldedObjects bA=%s bB=%s", bA,bB));
                if (other != null) {
                    //app.println(String.format("... obj=%s", other));
                    objs.add(other);
                } else {
                    //app.println(" ... null obj");
                }
            }
        }
        return objs;
    }

    /** Remove all weld joints from this body
     * @return true if an object was unwelded
     */
    public boolean removeWeldJoints() {
        return removeWeldJoints(null);
    }

    /** Remove all weld joints except to the body IGNORE
     * @param ignore do not destroy a joint if it goes to object IGNORE
     * @return true if an object was unwelded
     */
    public boolean removeWeldJoints(Object2D ignore) {
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

    /** Adds WeldJoint between thing and any bodies it is contacting
     * @return list of objects it welded to
     */
    ArrayList<Object2D> weldContacts() {
        return weldContacts(null);
    }


    /** Adds WeldJoint between thing and any bodies it is contacting
     * @param ignore do not weld to this object, if supplied
     * @return list of objects it welded to
     */
    ArrayList<Object2D> weldContacts(Object2D ignore) {
        ArrayList<Object2D> objs = new ArrayList<Object2D>();
        app.println("********\nGrasp Contacts for "+this);
        ContactEdge cedge = body.getContactList();
        while (cedge != null) {
            Contact c = cedge.contact;
            if (c.getManifold().pointCount > 0) {
                Object2D other = (Object2D) cedge.other.getUserData();
                if (other != ignore && other != null) {
                    app.println("welding "+this+" to obj "+other);
                    if (!objs.contains(other)) {
                        objs.add(other);
                        weld(this, other);
                    }
                }
            }
            cedge = cedge.next;
        }
        return objs;
    }

    /**
     * Destroys every joint in the body's contact list.
     */
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

    public Vec2 getJointForce() {
        Vec2 fv = new Vec2();
        if (mouseJoint != null) {
            mouseJoint.getReactionForce(1.0f, fv);
        }
        return fv;
    }

    public float getJointTorque() {
        float t = 0;
        if (mouseJoint != null) {
            t = mouseJoint.getReactionTorque(1.0f);
        }
        return t;
    }


}


