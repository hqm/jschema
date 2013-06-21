package com.beartronics.jschema;

// The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2010
// PBox2D example

import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;


import processing.core.*;
import processing.core.PConstants.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 


// The Nature of Code
// Daniel Shiffman
// http://natureofcode.com

// A Particle

class Particle {

    // We need to keep track of a Body
    Body body;
    JSchema app;
    PBox2D box2d;


    PVector[] trail;

    // Constructor
    Particle(JSchema app, float x_, float y_) {
        this.app = app;
        this.box2d = app.box2d;

        float x = x_;
        float y = y_;
        trail = new PVector[6];
        for (int i = 0; i < trail.length; i++) {
            trail[i] = new PVector(x, y);
        }

        // Add the box to the box2d world
        // Here's a little trick, let's make a tiny tiny radius
        // This way we have collisions, but they don't overwhelm the system
        makeBody(new Vec2(x, y), 0.2f);
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
        if (pos.y > app.height+20) {
            killBody();
            return true;
        }
        return false;
    }

    // Drawing the box
    void display() {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);

        // Keep track of a history of screen positions in an array
        for (int i = 0; i < trail.length-1; i++) {
            trail[i] = trail[i+1];
        }
        trail[trail.length-1] = new PVector(pos.x, pos.y);

        // Draw particle as a trail
        app.beginShape();
        app.noFill();
        app.strokeWeight(2);
        app.stroke(0, 150);
        for (int i = 0; i < trail.length; i++) {
            app.vertex(trail[i].x, trail[i].y);
        }
        app.endShape();
    }

    // This function adds the rectangle to the box2d world
    void makeBody(Vec2 center, float r) {
        // Define and create the body
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;

        bd.position.set(box2d.coordPixelsToWorld(center));
        body = box2d.createBody(bd);

        // Give it some initial app.random velocity
        body.setLinearVelocity(new Vec2(app.random(-1, 1), app.random(-1, 1)));

        // Make the body's shape a circle
        CircleShape cs = new CircleShape();
        cs.m_radius = box2d.scalarPixelsToWorld(r);

        FixtureDef fd = new FixtureDef();
        fd.shape = cs;

        fd.density = 1;
        fd.friction = 0;  // Slippery when wet!
        fd.restitution = 0.5f;

        // We could use this if we want to turn collisions off
        //cd.filter.groupIndex = -10;

        // Attach fixture to body
        body.createFixture(fd);
    }
}
