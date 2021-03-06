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
    int color;

    Ball(Plane plane, float x, float y, float r_, int color) {
        super(plane);
        this.color = color;
        r = r_;
        // This function puts the particle in the Box2d world
        makeBody(box2d, x,y,r);
    }


    // 
    public void display(PGraphics pg) {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        float a = body.getAngle();
        pg.pushStyle();
        pg.pushMatrix();
        pg.translate(pos.x,pos.y);
        pg.rotate(-a);
        pg.fill(color, plane.alpha);
        pg.stroke(plane.borderColor);
        pg.strokeWeight(2);
        pg.ellipse(0,0,r*2,r*2);
        // Let's add a line so we can see the rotation
        pg.line(0,0,r,0);

        // Draw id number
        pg.fill(0);
        pg.text(index, 0,0);

        pg.popMatrix();
        pg.popStyle();
    }

    public void moveToPlane(Plane newPlane) {
        super.moveToPlane(newPlane);
        float a = getAngle();
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // delete us from this plane box2d world
        removeAllJoints();
        killBody();
        // create new body in target plane
        // Switch our box2d pointers to the new plane
        box2d = newPlane.box2d;
        plane = newPlane;
     
        makeBody(box2d, pos.x, pos.y, r);
        setAngle(a);
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
        fd.density = 2f;
        fd.friction = 0.02f;
        fd.restitution = 0.1f;
    
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

