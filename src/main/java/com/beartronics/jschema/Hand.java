package com.beartronics.jschema;

// The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2010
// PBox2D example

import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import java.util.*;
import org.jbox2d.dynamics.contacts.*;
import org.jbox2d.dynamics.joints.*;


import processing.core.PApplet;
import processing.core.*;
import processing.core.PConstants.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 


// A rectangular box
public class Hand extends Box {

    // Vector of (gross,fine) body-relative hand position
    public float grossX   = 0;
    public float fineX    = 0;
    public float grossY   = 0;
    public float fineY    = 0;
    public float handForceX = 0;
    public float handForceY = 0;

    float GROSS_DIST = 100;
    float FINE_DIST = 10;

    // TODO we need to set up a distance joint on the hands so they can't get further than the 'reach' dist
    // from the body.

    // public final void applyLinearImpulse(Vec2 impulse, Vec2 point)

    // Constructor
    Hand(Plane p, float x, float y, float w, float h, float density, int color) {
        super(p,x,y,w,h,density,color);
        setupMouseJoint();
    }

    /** Creates the spring joints that sets the hand at fixed position relative to body.
     */
    public void setupMouseJoint() {
        Vec2 pos = box2d.getBodyPixelCoord(body);
        bindMouseJoint(pos.x, pos.y);
    }

    /** Releases motor control of the hand (by destroying its mouse joint)
       Call setupMouseJoint to restore control of the hand to motor system.
    */
    public void relaxMouseJoint() {
        destroyMouseJoint();
    }

    /** Find all objects we are grasping, and welds them to anything they are touching (except us).
       returns true if any objects were welded.
     */
    public boolean weldGraspedObjects () {
        boolean anyAttached = false;
        for (Object2D obj: getWeldedObjects()) {
            anyAttached = true;
            obj.weldContacts(this);
        }
        return anyAttached;
    }

    /**
     * Welds all contacting objects to this hand.
     */
    public ArrayList<Object2D> grasp() {
        return weldContacts();
    }

    public void ungrasp() {
        removeWeldJoints();
    }

    /**
       Finds any objects we're grasping, and deletes any weld joints they have, except the weld to the hand.
     */
    public boolean unWeldGraspedObjects () {
        boolean anyAttached = false;
        for (Object2D obj: getWeldedObjects()) {
            anyAttached = true;
            obj.removeWeldJoints(this);
        }
        return anyAttached;
    }

    /** Takes a gross and fine delta motion, enforce max reach limit
        @param dgx gross motion delta
        @param dfx fine motion delta
    */
    public void hjog(float dgx, float dfx) {
        float reachX = app.sms.reachX;
        float dGross = app.sms.dGross;
        float dFine  = app.sms.dFine;

        grossX += dgx;
        fineX += dfx;
        if (grossX > reachX/2)   { grossX = reachX/2; }
        if (grossX < -reachX/2)  { grossX = -reachX/2; }
        if (fineX > reachX/2) { fineX = reachX/2; }
        if (fineX < -reachX/2) { fineX = -reachX/2; }

        updatePosition(app.sms.xpos, app.sms.ypos);
    }

    /** Takes a gross and fine delta motion, enforce max reach limit
        @param dgx gross motion delta
        @param dfx fine motion delta
     */
     public void vjog(float dgy, float dfy) {
        float reachY = app.sms.reachY;
        float dGross = app.sms.dGross;
        float dFine  = app.sms.dFine;

        grossY += dgy;
        fineY += dfy;
        if (grossY > reachY/2)   { grossY = reachY/2; }
        if (grossY < -reachY/2)  { grossY = -reachY/2; }
        if (fineY > reachY/2) { fineY = reachY/2; }
        if (fineY < -reachY/2) { fineY = -reachY/2; }


        updatePosition(app.sms.xpos, app.sms.ypos);
    }


    /** Sum of all forces of contacting objects  on the hand*/
    public Vec2 getNetForce() {
        return getNormalForces(this);
    }

    /**
       Update absolute pixel coord of the 'mousejoint' which hold the hand in position.
       @param  x body xpos
       @param  y body ypos
     */
    void updatePosition(float absx, float absy) {
        float x,y;

        // compute hand position from gross+fine positioning
        x = absx + grossX * GROSS_DIST + fineX * FINE_DIST;
        y = absy + grossY * GROSS_DIST + fineY * FINE_DIST;
        
        updateMouseJointPos(x,y);
        Vec2 pos = body.getPosition();
        // Is there a bug with apply the force here? We're applying
        // the force at the current position of the hand, not the
        // position it will finally be at when the mousejoint spring
        // forces are resolved.
        body.applyLinearImpulse(new Vec2(handForceX, handForceY), pos);
    }
    
    void setColor(int c) {
        this.color = c;
    }

    // Drawing the box
    void display() {
        super.display();
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        float a = body.getAngle();

        // Draw an interior border
        app.pushStyle();
        app.rectMode(PConstants.CENTER);
        app.pushMatrix();
        app.translate(pos.x, pos.y);
        app.rotate(-a);
        app.fill(app.red(color), app.green(color), app.blue(color), (float)(plane.alpha/255.0) * alpha);
        app.strokeWeight(4);
        app.stroke(app.color(94,224,88));
        app.rect(0, 0, w-6, h-6);
        app.popMatrix();
        app.popStyle();
    }

    public String toString() {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        float a = body.getAngle();
        float alpha = app.map(density, 0, MAX_DENSITY, 0, 255);
        StringBuilder grasps = new StringBuilder();
        for (Object2D obj: getWeldedObjects()) {
            grasps.append(obj.toString() +", ");
        }
        return String.format("{HAND %d x,y=(%f, %f) w,h=(%f, %f) rot=%f density=%f color=%x, alpha=%f grasping[%s]}",index, pos.x,pos.y,w,h,a,density,color,alpha, grasps);
    }


}


