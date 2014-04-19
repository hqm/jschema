package com.beartronics.jschema;
// Basde on code examples from The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2011
// PBox2D example

// Basic example of falling rectangles

import java.util.*;
import processing.core.*;


public class SimpleSensoriMotorSystem extends BaseSensoriMotorSystem {

    int hand1_x,hand2_x;
    int hand1_y,hand2_y;

    public SimpleSensoriMotorSystem(JSchema a, WorldState w, PGraphics retina) {
	super(a, w, retina);
    }

    public void hand1Home() {
	hand1_x = 0;
	hand1_y = 0;
    }

    public void  hand1Up() {
	hand1_x = 0;
	hand1_y -= 1;
    }

    public void hand1Down() {
	hand1_x = 0;
	hand1_y += 1;
    }

    public void hand2Home() {
	hand2_x = 0;
	hand2_y = 0;
    }

    public void  hand2Up() {
	hand2_x = 0;
	hand2_y -= 1;
    }

    public void hand2Down() {
	hand2_x = 0;
	hand2_y += 1;
    }

    public void stepPhysicalWorld() {

    }

    public void processActions(WorldState w) {
        //HashMap<String,Action> outputList
        for (Action action : w.actions) {
            // CODE HERE To execute actions
            switch (action.type) {
              case HAND2_UP:
		  hand2Up();
                break;
              case HAND2_DOWN:
		  hand2Down();
                break;
              case HAND1_UP:
		  hand1Up();
                break;
              case HAND1_DOWN:
		  hand1Down();
                break;
              case HAND1_HOME:
		  hand1Home();
                break;
              case HAND2_HOME:
		  hand2Home();
                break;
              case NULL_ACTION:
                break;
              default:
                app.println("unknown Action type "+action.type);
            }
        }
    }
}

