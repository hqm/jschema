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


// A displayable 2D object
abstract class Object2D {

    // We need to keep track of a Body and a width and height
    Body body;
    float density;
    Fixture fixture;
    int color;
    JSchema app;
    PBox2D box2d;
    float w,h;
    Plane plane;

    Object2D(Plane p) {
        this.plane = p;
        this.app = plane.app;
        this.box2d = plane.box2d;
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

}


