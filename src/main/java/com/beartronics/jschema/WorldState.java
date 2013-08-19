package com.beartronics.jschema;

import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import java.util.*;

import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.Shape;

import processing.core.*;
import org.apache.log4j.Logger;

public class WorldState {
    static Logger logger = Logger.getLogger(WorldState.class);

    public long clock = 0;
    public HashMap<String,SensorInput> inputs = new HashMap<String,SensorInput>();
    
    // Actions which are to be performed on this clock step
    public ArrayList<Action> actions = new ArrayList<Action>();

    public WorldState() {        
    }

    public void setClock(long c) {
        clock = c;
    }

    SensorInput setSensorInput(String path, int id,  boolean val) {
        SensorInput s = inputs.get(path);
        if (s == null) {
            s = new SensorInput(path, id, val);
            inputs.put(path, s);
        }

        s.prevValue = s.value;
        s.value = val;
        if (!s.prevValue && val) {
            s.lastPosTransition = clock;
        } else if (s.prevValue && !val) {
            s.lastNegTransition = clock;
        }

        /*
          if (s.id == 127 || s.id==126) {
            logger.info("setSensorInput "+clock+" "+s+" lastPos="+s.lastPosTransition+" lastNeg="+s.lastNegTransition);
        }
        */
        return s;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, SensorInput> entry : inputs.entrySet())
        {
            list.add(entry.getValue().toString());
        }
        Collections.sort(list);
        for (String s: list) {
            out.append(s+"\n");
        }

        return out.toString();
    }

}
