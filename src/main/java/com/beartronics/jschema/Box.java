package com.beartronics.jschema;

// The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2010
// PBox2D example

import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;

// A rectangular box
class Box {

    // We need to keep track of a Body and a width and height
    Body body;
    float w;
    float h;

    PBox2D box2d;
    // Constructor
    Box(PBox2D box2d, float x, float y) {
        this.box2d = box2d;
        w = random(4, 16);
        h = random(4, 16);
        // Add the box to the box2d world
        makeBody(new Vec2(x, y), w, h);
    }

    // This function removes the particle from the box2d world
    void killBody() {
        box2d.destroyBody(body);
    }

    // Is the particle ready for deletion?
    boolean done() {
        // Let's find the screen position of the particle
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Is it off the bottom of the screen?
        if (pos.y > height+w*h) {
            killBody();
            return true;
        }
        return false;
    }

    // Drawing the box
    void display() {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        float a = body.getAngle();

        rectMode(CENTER);
        pushMatrix();
        translate(pos.x, pos.y);
        rotate(-a);
        fill(175);
        stroke(0);
        rect(0, 0, w, h);
        popMatrix();
    }

    // This function adds the rectangle to the box2d world
    void makeBody(Vec2 center, float w_, float h_) {

        // Define a polygon (this is what we use for a rectangle)
        PolygonShape sd = new PolygonShape();
        float box2dW = box2d.scalarPixelsToWorld(w_/2);
        float box2dH = box2d.scalarPixelsToWorld(h_/2);
        sd.setAsBox(box2dW, box2dH);

        // Define a fixture
        FixtureDef fd = new FixtureDef();
        fd.shape = sd;
        // Parameters that affect physics
        fd.density = 1;
        fd.friction = 0.3;
        fd.restitution = 0.5;

        // Define the body and make it from the shape
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(box2d.coordPixelsToWorld(center));

        body = box2d.createBody(bd);
        body.createFixture(fd);

        // Give it some initial random velocity
        body.setLinearVelocity(new Vec2(random(-5, 5), random(2, 5)));
        body.setAngularVelocity(random(-5, 5));
    }
}


