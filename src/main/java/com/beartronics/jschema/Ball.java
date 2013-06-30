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




// The Nature of Code
// Daniel Shiffman
// http://natureofcode.com

// A circular particle

public class Ball extends Object2D {

    // We need to keep track of a Body and a radius
    float r;
    final static int MAX_DENSITY = 10;

    Ball(Plane plane, float x, float y, float r_) {
        super(plane);
        r = r_;
        // This function puts the particle in the Box2d world
        makeBody(box2d, x,y,r);
    }


    // 
    void display() {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        float a = body.getAngle();
        app.pushStyle();
        app.pushMatrix();
        app.translate(pos.x,pos.y);
        app.rotate(-a);
        app.fill(127, plane.alpha);
        app.stroke(plane.borderColor);
        app.strokeWeight(2);
        app.ellipse(0,0,r*2,r*2);
        // Let's add a line so we can see the rotation
        app.line(0,0,r,0);
        app.popMatrix();
        app.popStyle();
    }

    Ball moveToPlane(Plane newPlane) {
        plane.removeObject(this);
        float a = getAngle();
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // delete us from this plane box2d world
        killBody();
        // create new body in target plane
        this.plane = newPlane;
        Ball b = newPlane.addBall(pos.x, pos.y, r);
        b.setAngle(a);
        return b;
    }


    // Here's our function that adds the particle to the Box2D world
    void makeBody(PBox2D box2d, float x, float y, float r) {
        // Define a body
        BodyDef bd = new BodyDef();
        // Set its position
        bd.position = box2d.coordPixelsToWorld(x,y);
        bd.type = BodyType.DYNAMIC;
        body = box2d.world.createBody(bd);

        // Make the body's shape a circle
        CircleShape cs = new CircleShape();
        cs.m_radius = box2d.scalarPixelsToWorld(r);
    
        FixtureDef fd = new FixtureDef();
        fd.shape = cs;
        // Parameters that affect physics
        fd.density = 1f;
        fd.friction = 0.01f;
        fd.restitution = 0.3f;
    
        // Attach fixture to body
        fixture = body.createFixture(fd);
        
        // Give it a random initial velocity (and angular velocity)
        body.setLinearVelocity(new Vec2(app.random(-10f,10f),app.random(5f,10f)));
        body.setAngularVelocity(app.random(-10,10));
        super.makeBody();
    }


    public String toString() {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        float a = body.getAngle();
        float alpha = app.map(density, 0, MAX_DENSITY, 0, 255);
        return String.format("{Ball %d x,y=(%f, %f) r=(%f) rot=%f density=%f color=%x, alpha=%f}",index, pos.x,pos.y,r,a,density,color,alpha);
    }


}

