package com.beartronics.jschema;

// The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2010
// PBox2D example

import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;

import processing.core.PApplet;
import processing.core.*;
import processing.core.PConstants.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 


// A rectangular box
public class Hand extends Box {

    // Vector of (gross,fine) body-relative hand position
    public int grossX   = 0;
    public int fineX    = 0;
    public int grossY   = 0;
    public int fineY    = 0;
    public float handForceX = 0;
    public float handForceY = 0;

    float GROSS_DIST = 10;

    int reachX;
    int reachY;

    // TODO we need to set up a distance joint on the hands so they can't get further than the 'reach' dist
    // from the body.

    // public final void applyLinearImpulse(Vec2 impulse, Vec2 point)

    // Constructor
    Hand(Plane p, float x, float y, float w, float h, float density, int color) {
        super(p,x,y,w,h,density,color);
        setupMouseJoint();
        reachX = app.sms.reachX;
        reachY = app.sms.reachY;
    }

    // Creates the spring joints that hold the hands at fixed positions.
    void setupMouseJoint() {
        Vec2 pos = box2d.getBodyPixelCoord(body);
        bindMouseJoint(pos.x, pos.y);
    }

    /**
       Update absolute pixel coord of the 'mousejoint' which hold the hand in position.
       @param  x body xpos
       @param  y body ypos
     */
    void updatePosition(float absx, float absy) {
        float x,y;

        // compute hand position from gross+fine positioning
        x = absx + grossX * GROSS_DIST + fineX;
        y = absy + grossY * GROSS_DIST + fineY;
        
        updateMouseJointPos(x,y);
        Vec2 pos = box2d.getBodyPixelCoord(body);
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
        return String.format("{BOX %d x,y=(%f, %f) w,h=(%f, %f) rot=%f density=%f color=%x, alpha=%f}",index, pos.x,pos.y,w,h,a,density,color,alpha);
    }


}


