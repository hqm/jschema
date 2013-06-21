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

import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.Shape;



import processing.core.*;
import processing.core.PConstants.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 


// Class to describe the spring joint (displayed as a line)

class Spring {

    // This is the box2d object we need to create
    MouseJoint mouseJoint;

    JSchema app;
    PBox2D box2d;
    // Constructor

    Spring(JSchema app) {
        this.app = app;
        this.box2d = app.box2d;
        // At first it doesn't exist
        mouseJoint = null;
    }

    // If it exists we set its target to the mouse location 
    void update(float x, float y) {
        if (mouseJoint != null) {
            // Always convert to world coordinates!
            Vec2 mouseWorld = box2d.coordPixelsToWorld(x,y);
            mouseJoint.setTarget(mouseWorld);
        }
    }

    void display() {
        if (mouseJoint != null) {
            // We can get the two anchor points
            Vec2 v1 = new Vec2(0,0);
            mouseJoint.getAnchorA(v1);
            Vec2 v2 = new Vec2(0,0);
            mouseJoint.getAnchorB(v2);
            // Convert them to screen coordinates
            v1 = box2d.coordWorldToPixels(v1);
            v2 = box2d.coordWorldToPixels(v2);
            // And just draw a line
            app.stroke(0);
            app.strokeWeight(1);
            app.line(v1.x,v1.y,v2.x,v2.y);
        }
    }


    // This is the key function where
    // we attach the spring to an x,y location
    // and the Box object's location
    void bind(float x, float y, Box box) {
        // Define the joint
        MouseJointDef md = new MouseJointDef();
        // Body A is just a fake ground body for simplicity (there isn't anything at the mouse)
        md.bodyA = box2d.getGroundBody();
        // Body 2 is the box's boxy
        md.bodyB = box.body;
        // Get the mouse location in world coordinates
        Vec2 mp = box2d.coordPixelsToWorld(x,y);
        // And that's the target
        md.target.set(mp);
        // Some stuff about how strong and bouncy the spring should be
        md.maxForce = 1000.0f * box.body.m_mass;
        md.frequencyHz = 5.0f;
        md.dampingRatio = 0.9f;

        // Make the joint!
        mouseJoint = (MouseJoint) box2d.world.createJoint(md);
    }

    void destroy() {
        // We can get rid of the joint when the mouse is released
        if (mouseJoint != null) {
            box2d.world.destroyJoint(mouseJoint);
            mouseJoint = null;
        }
    }

}

