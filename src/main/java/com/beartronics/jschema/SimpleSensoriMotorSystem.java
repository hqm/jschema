package com.beartronics.jschema;
// Basde on code examples from The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2011
// PBox2D example

// Basic example of falling rectangles

import java.util.*;
import processing.core.*;
import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;



public class SimpleSensoriMotorSystem extends BaseSensoriMotorSystem {

    Vec2 hand1pos = new Vec2();
    Vec2 hand2pos = new Vec2();

    public SimpleSensoriMotorSystem(JSchema a, WorldState w, PGraphics retina) {
        super(a, w, retina);
    }

    public void hand1Home() {
        hand1pos.x = 0;
        hand1pos.y = 0;
    }

    public void  hand1Up() {
        hand1pos.x = 0;
        hand1pos.y -= 1;
    }

    public void hand1Down() {
        hand1pos.x = 0;
        hand1pos.y += 1;
    }

    public void hand2Home() {
        hand2pos.x = 0;
        hand2pos.y = 0;
    }

    public void  hand2Up() {
        hand2pos.x = 0;
        hand2pos.y -= 1;
    }

    public void hand2Down() {
        hand2pos.x = 0;
        hand2pos.y += 1;
    }

    public void stepPhysicalWorld() {
        computeWorldState();
    }

    /// Fills in the sensory input values
    public WorldState computeWorldState() {
        computeTouchSensors();

        return worldState;
    }

    public static int reachX = 3;
    public static int reachY = 3;

    public void computeTouchSensors() {
        // update joint position sensors
        int sensorID = 0;
        for (int i = -reachX; i <= reachX; i++) {
            for (int j = -reachY; j <= reachY; j++) {
                worldState.setSensorItem("hand1@("+i+","+j+")",sensorID++, ((int)(hand1pos.x) == i) && ((int)(hand1pos.y) == j), clock);
                worldState.setSensorItem("hand2@("+i+","+j+")",sensorID++, ((int)(hand2pos.x) == i) && ((int)(hand2pos.y) == j), clock);

            }
        }
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

