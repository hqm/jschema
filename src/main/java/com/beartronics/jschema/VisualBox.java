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

import processing.core.PApplet;
import processing.core.*;
import processing.core.PConstants.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 


// A vision quadrant cell. Senses what objects intersect with it.
public class VisualBox extends Object2D {

    // We need to keep track of a Body and a width and height
    public float w;
    public float h;
    public int px,py;
    public float dx,dy;
    float alpha = 255;
    public HashSet<Object2D> sensed = new HashSet<Object2D>();

    void addSensed(Object2D obj) {
        sensed.add(obj);
    }

    void removeSensed(Object2D obj) {
        sensed.remove(obj);
    }

    // Constructor
    VisualBox(Plane p, float x, float y, float w, float h) {
        super(p);
        _VisualBox(p, x,y,w,h);
    }

    void _VisualBox(Plane p, float x, float y, float w, float h) {
        this.color = app.color(0,0,0);
        this.density = 1f;
        this.w = w;
        this.h = h;
        this.dx = x;
        this.dy = y;

        // Add the box to the box2d world
        makeBody(box2d, new Vec2(dx, dy), w, h);
    }
    
    // Drawing the box
    @Override
    void display(PGraphics pg) {
        Vec2 pos = box2d.getBodyPixelCoord(body);
        pg.pushStyle();
        pg.rectMode(PConstants.CENTER);
        pg.pushMatrix();
        pg.translate(pos.x, pos.y);
        //pg.noFill();
        pg.fill(127,127,127,10);
        pg.strokeWeight(1);
        pg.stroke(0,0,0,10);
        pg.rect(0, 0, w, h);
        pg.popMatrix();
        pg.popStyle();
    }

    // This function adds the rectangle to the box2d world
    void makeBody(PBox2D world, Vec2 center, float w_, float h_) {

        // Define a polygon (this is what we use for a rectangle)
        PolygonShape sd = new PolygonShape();
        float box2dW = world.scalarPixelsToWorld(w_/2);
        float box2dH = world.scalarPixelsToWorld(h_/2);
        sd.setAsBox(box2dW, box2dH);

        // Define a fixture
        FixtureDef fd = new FixtureDef();
        fd.shape = sd;
        // Parameters that affect physics
        //fd.density = density;
        fd.density = density;
        fd.friction = 0f;
        fd.restitution = 1f;
        fd.isSensor = true;

        // Define the body and make it from the shape
        BodyDef bd = new BodyDef();
        bd.type = BodyType.STATIC;
        bd.position.set(world.coordPixelsToWorld(center));

        body = world.createBody(bd);
        fixture = body.createFixture(fd);
        super.makeBody();
    }

    public String toString() {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        float a = body.getAngle();

        StringBuilder touching = new StringBuilder();
        for (Object2D obj: sensed) {
            touching.append(" "+obj.index);
        }

        return String.format("{SENSORBOX %d x,y=(%.0f, %.0f) w,h=(%.0f, %.0f) contains %s }",index, pos.x,pos.y,w,h, touching);
    }


    public void updatePosition(float cx, float cy) {
        moveTo(cx+dx, cy+dy);
    }


}


