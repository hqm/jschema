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


import processing.core.*;
import processing.core.PConstants.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

// The Nature of Code
// Daniel Shiffman
// http://natureofcode.com

// Box2D Particle System

// A class to describe a group of Particles
// An ArrayList is used to manage the list of Particles 

class ParticleSystem  {

    JSchema app;
    PBox2D box2d;


    ArrayList<Particle> particles;    // An ArrayList for all the particles
    PVector origin;         // An origin point for where particles are birthed

    ParticleSystem(JSchema app, int num, PVector v) {
        this.app = app;
        this.box2d = app.box2d;

        particles = new ArrayList<Particle>();             // Initialize the ArrayList
        origin = v.get();                        // Store the origin point

        for (int i = 0; i < num; i++) {
            particles.add(new Particle(app, origin.x,origin.y));    // Add "num" amount of particles to the ArrayList
        }
    }

    void run() {
        // Display all the particles
        for (Particle p: particles) {
            p.display();
        }

        // Particles that leave the screen, we delete them
        // (note they have to be deleted from both the box2d world and our list
        for (int i = particles.size()-1; i >= 0; i--) {
            Particle p = particles.get(i);
            if (p.done()) {
                particles.remove(i);
            }
        }
    }

    void addParticles(int n) {
        for (int i = 0; i < n; i++) {
            particles.add(new Particle(app, origin.x,origin.y));
        }
    }

    // A method to test if the particle system still has particles
    boolean dead() {
        if (particles.isEmpty()) {
            return true;
        } 
        else {
            return false;
        }
    }

}
