package com.beartronics.jschema;

import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import java.util.*;

import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.Shape;

import processing.core.*;


public class WorldState {

    int sensorID = 0;

    void setSensorInput(String path, int id,  boolean val) {
        setSensorInput(path, id, val, 0);
    }

    void setSensorInput(String path, int id,  boolean val, float realVal) {
        SensorInput s = inputList.get(path);
        if (s == null) {
            s = new SensorInput(path, id, val, realVal);
            inputList.put(path, s);
        } else {
            s.value = val;
            s.realValue = realVal;
        }
    }

    HashMap<String,SensorInput> inputList = new HashMap<String,SensorInput>();
    
    HashMap<String,MotorAction> outputList = new HashMap<String,MotorAction>();

    public WorldState() {        
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<String, SensorInput> entry : inputList.entrySet())
        {
            s.append(entry.getKey() + "/" + entry.getValue()+"\n");
        }

        return s.toString();
    }

}
