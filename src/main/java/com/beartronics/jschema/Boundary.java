package com.beartronics.jschema;
// The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2012
// PBox2D example
import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;

import processing.core.*;
import processing.core.PConstants; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 


// A fixed boundary class

class Boundary {

    // A boundary is a simple rectangle with x,y,width,and height
    float x;
    float y;
    float w;
    float h;
  
    PBox2D box2d;
    // But we also have to make a body for box2d to know about it
    Body b;
    JSchema app;

    Boundary(JSchema app_, PBox2D box2d, float x_,float y_, float w_, float h_) {
        app = app_;
        box2d = box2d;
        x = x_;
        y = y_;
        w = w_;
        h = h_;

        // Define the polygon
        PolygonShape sd = new PolygonShape();
        // Figure out the box2d coordinates
        float box2dW = box2d.scalarPixelsToWorld(w/2);
        float box2dH = box2d.scalarPixelsToWorld(h/2);
        // We're just a box
        sd.setAsBox(box2dW, box2dH);

        // Create the body
        BodyDef bd = new BodyDef();
        bd.type = BodyType.STATIC;
        bd.position.set(box2d.coordPixelsToWorld(x,y));
        b = box2d.createBody(bd);
    
        // Attached the shape to the body using a Fixture
        b.createFixture(sd,1);
    }

    // Draw the boundary, if it were at an angle we'd have to do something fancier
    void display() {
        app.fill(0);
        app.stroke(0);
        app.rectMode(PConstants.CENTER);
        app.rect(x,y,w,h);
    }

}


