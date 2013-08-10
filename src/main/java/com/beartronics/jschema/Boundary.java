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

class Boundary extends Object2D {

    // A boundary is a simple rectangle with x,y,width,and height
    float x;
    float y;
  
    PBox2D box2d;
    // But we also have to make a body for box2d to know about it
    JSchema app;

    void setColor(int color) {
        this.color = color;
    }

    Boundary(Plane p, float x_,float y_, float w_, float h_) {
        super(p);
        _Boundary(p,x_,y_,w_,h_,0);
    }

    Boundary(Plane p, float x_,float y_, float w_, float h_, int color) {
        super(p);
        _Boundary(p,x_,y_,w_,h_,color);
    }


    void _Boundary(Plane p, float x_,float y_, float w_, float h_, int color) {
        app = p.app;
        box2d = p.box2d;
        x = x_;
        y = y_;
        w = w_;
        h = h_;
        this.color = color;

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
        body = box2d.createBody(bd);
    
        // Attached the shape to the body using a Fixture
        fixture = body.createFixture(sd,1);
        super.makeBody();
    }

    // Draw the boundary, if it were at an angle we'd have to do something fancier
    void display(PGraphics pg) {
        pg.fill(color);
        pg.stroke(0);
        pg.rectMode(PConstants.CENTER);
        pg.rect(x,y,w,h);
    }


    public String toString() {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        return String.format("{Boundary %d x,y=(%f, %f) }",index, pos.x,pos.y);
    }
}


