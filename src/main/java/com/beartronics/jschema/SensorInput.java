package com.beartronics.jschema;
import org.json.simple.*;

import java.io.Serializable;
import java.util.*;


public class SensorInput implements JSONAware, Serializable {
    public String name;
    public boolean value;
    public long clock;

    public int id; // a unique id for this input

    public long lastPosTransition =  -1000;
    public long lastNegTransition =  -1000;
    public boolean value;
    public boolean prevValue;

    SensorInput(String name, boolean val) {
        this.name = name;
        this.value = val;
    }
    
    public void setValue(boolean val, long time) {
        this.value = val;
        this.clock = time;
    }
    
    public String toString() {
        return String.format("sensorinput-%s: %s", name, value);
    }

    // for serializing to send to browser for visualizing
    public String toJSONString() {
        Map obj = new LinkedHashMap();
        obj.put("name", name);
        obj.put("state", value);
        obj.put("clock", clock);
        return JSONValue.toJSONString(obj);
    }

}
