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
class Box extends Object2D {

    // We need to keep track of a Body and a width and height
    float w;
    float h;
    float alpha = 255;
    final static int MAX_DENSITY = 10;

    // Constructor
    Box(Plane p, float x, float y, float w, float h, float density) {
        super(p);
        _Box(p, x,y,w,h,density);
    }


    Box(Plane p, float x, float y, float w, float h, float density, int color) {
        super(p);
        _Box(p, x,y,w,h,density);
        this.color = color;
    }

    void _Box(Plane p, float x, float y, float w, float h, float density) {
        this.color = app.color(0,0,0);
        this.density = density;
        this.w = w;
        this.h = h;

        this.alpha = app.map(density, 0, MAX_DENSITY, 0, 255);

        // Add the box to the box2d world
        makeBody(new Vec2(x, y), w, h, density);
    }
    


    // Drawing the box
    void display() {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        float a = body.getAngle();

        app.pushStyle();
        app.rectMode(PConstants.CENTER);
        app.pushMatrix();
        app.translate(pos.x, pos.y);
        app.rotate(-a);
        app.fill(app.red(color), app.green(color), app.blue(color), (float)(plane.alpha/255.0) * alpha);
        app.stroke(0);
        app.rect(0, 0, w, h);
        app.popMatrix();
        app.popStyle();
    }

    // This function adds the rectangle to the box2d world
    void makeBody(Vec2 center, float w_, float h_, float density) {

        // Define a polygon (this is what we use for a rectangle)
        PolygonShape sd = new PolygonShape();
        float box2dW = box2d.scalarPixelsToWorld(w_/2);
        float box2dH = box2d.scalarPixelsToWorld(h_/2);
        sd.setAsBox(box2dW, box2dH);

        // Define a fixture
        FixtureDef fd = new FixtureDef();
        fd.shape = sd;
        // Parameters that affect physics
        //fd.density = density;
        fd.density = 1;
        fd.friction = 0.6f;
        fd.restitution = 0.3f;

        // Define the body and make it from the shape
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(box2d.coordPixelsToWorld(center));

        body = box2d.createBody(bd);
        fixture = body.createFixture(fd);

        // Give it some initial random velocity
        //body.setLinearVelocity(new Vec2(app.random(-5, 5), app.random(2, 5)));
        //body.setAngularVelocity(app.random(-5, 5));
    }

    public String toString() {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        float a = body.getAngle();
        float alpha = app.map(density, 0, MAX_DENSITY, 0, 255);
        return String.format("{BOX x,y=(%f, %f) w,h=(%f, %f) rot=%f density=%f color=%x, alpha=%f}",pos.x,pos.y,w,h,a,density,color,alpha);
    }


}


