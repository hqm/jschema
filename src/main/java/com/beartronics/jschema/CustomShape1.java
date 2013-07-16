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

class CustomShape1 extends Object2D {

    // Constructor
    CustomShape1(Plane p, float x, float y, int color) {
        super(p);
        // Add the box to the box2d world
        makeBody(box2d, new Vec2(x, y));
        this.color = color;
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
        if (pos.y > app.height) {
            killBody();
            return true;
        }
        return false;
    }

    // Drawing the box
    void display(PGraphics pg) {
        // We look at each body and get its screen position
        Vec2 pos = box2d.getBodyPixelCoord(body);
        // Get its angle of rotation
        float a = body.getAngle();

        Fixture f = body.getFixtureList();
        PolygonShape ps = (PolygonShape) f.getShape();


        pg.rectMode(PConstants.CENTER);
        pg.pushMatrix();
        pg.translate(pos.x, pos.y);
        pg.rotate(-a);
        pg.fill(color);
        pg.stroke(0);
        pg.beginShape();
        //println(vertices.length);
        // For every vertex, convert to pixel vector
        for (int i = 0; i < ps.getVertexCount(); i++) {
            Vec2 v = box2d.vectorWorldToPixels(ps.getVertex(i));
            pg.vertex(v.x, v.y);
        }
        pg.endShape(PConstants.CLOSE);
        pg.popMatrix();
    }

    // This function adds the rectangle to the box2d world
    void makeBody(PBox2D world, Vec2 center) {

        // Define a polygon (this is what we use for a rectangle)
        PolygonShape sd = new PolygonShape();

        Vec2[] vertices = {
            box2d.vectorPixelsToWorld(new Vec2(0,0)),
            box2d.vectorPixelsToWorld(new Vec2(0,50)),
            box2d.vectorPixelsToWorld(new Vec2(50,50)),
            box2d.vectorPixelsToWorld(new Vec2(50,40)),
            box2d.vectorPixelsToWorld(new Vec2(10,40)),
            box2d.vectorPixelsToWorld(new Vec2(10,0))
        };
            
        sd.set(vertices, vertices.length);

        // Define the body and make it from the shape
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(box2d.coordPixelsToWorld(center));
        body = box2d.createBody(bd);

        fixture = body.createFixture(sd, 1.0f);



        // Give it some initial app.random velocity
        body.setLinearVelocity(new Vec2(app.random(-5, 5), app.random(2, 5)));
        body.setAngularVelocity(app.random(-5, 5));
        super.makeBody();

    }
}
