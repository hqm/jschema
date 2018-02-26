package com.beartronics.jschema;
import org.apache.log4j.Logger;
import java.util.concurrent.*;
import org.json.simple.*;

import java.io.Serializable;
import java.util.*;

public class SensorState  implements JSONAware, Serializable {
    static Logger logger = Logger.getLogger(SensorState.class);

    public ConcurrentHashMap<String,SensorInput> items = new ConcurrentHashMap<String,SensorInput>();
    public List<String> actions = new ArrayList<>();
    public List debugInfo = new LinkedList();
    public long clock = 0 ;
    
    public SensorState() {        
    }

    public void setClock(long c) {
        clock = c;
    }
    public long getClock() {
        return clock;
    }

    public void setActions(List<String> a) {
        actions = a;
    }

    public void setDebugInfo(List d) {
        debugInfo = d;
    }

    public boolean getSensorValue(String name) {
        SensorInput s = items.get(name);
        if (s == null) {
            s = new SensorInput(name, false);
        }
        return s.value;
    }

    // Sensor items are only distinguished as being proprioceptive or visual by the name

    public SensorInput getSensorItem(String name) {
        SensorInput s = items.get(name);
        if (s == null) {
            s = new SensorInput(name, false);
        }
        return s;
    }

    /**
       Item list, sorted by item name
     */
    public List<SensorInput> sortedItems() {
        ArrayList<SensorInput> sorted = new ArrayList<>();
        for (Map.Entry<String, SensorInput> entry : items.entrySet())
        {
            sorted.add(entry.getValue());
        }

        Collections.sort(sorted,
                ((s1, s2) -> s1.name.compareTo(s2.name)));


        return sorted;

    }

    static long id = 1;

    public SensorInput setSensorValue(String name,  boolean val, long clock) {
        SensorInput s = items.get(name);
        if (s == null) {
            s = new SensorInput(name, val, _id++);
            items.put(name, s);
        } else {
            s.setValue(val, clock);
        }
        return s;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry<String, SensorInput> entry : items.entrySet())
        {
            list.add(entry.getValue().toString());
        }
        Collections.sort(list);
        for (String s: list) {
            out.append(s+"\n");
        }

        return out.toString();
    }


    public Map toMap() {
        HashMap<String, Object> obj = new HashMap<>();
        Map itemSet = new LinkedHashMap();
        for (Map.Entry<String, SensorInput> entry : items.entrySet())
        {
            SensorInput item = entry.getValue();
            itemSet.put(entry.getKey(), item.value);
        }
        obj.put("items", itemSet);
        obj.put("actions", actions);
        obj.put("clock", clock);
        obj.put("debuginfo", debugInfo);
        return obj;
    }


    public String toJSONString() {
        Map obj = new LinkedHashMap();
        Map itemSet = new LinkedHashMap();
        for (Map.Entry<String, SensorInput> entry : items.entrySet())
        {
            SensorInput item = entry.getValue();
            itemSet.put(entry.getKey(), item.value);
        }
        obj.put("items", itemSet);
        obj.put("actions", actions);
        obj.put("clock", clock);
        return JSONValue.toJSONString(obj);
    }



}
