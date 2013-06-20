package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;


class SchemaProximityValue implements Comparable<SchemaProximityValue> {
    Schema schema;
    float proximity = 0;

    SchemaProximityValue(Schema s, float p) {
        this.schema = s;
        this.proximity = p;
    }

    public int compareTo(SchemaProximityValue o) {
        if (this.proximity == o.proximity) {
            return 0;
        } else if ((this.proximity - o.proximity) < 0) {
            return -1;
        } else {
            return 1;
        }
    }
}

public class ActionController {

    Action action;
    // need to make a list which can hold proximity values for each schema and be sorted by proximity.
    ArrayList<SchemaProximityValue> schemas = new ArrayList<SchemaProximityValue>();

    public ActionController(Action a) {
        this.action = a;
    }

    void sortSchemas() {
        Collections.sort(schemas);
    }

    void computeProximities() {
        // 

        // See Sec 3.3 of Made-Up Minds for implementation of action controller
        //
        // We need to compute, for each schema, what it's proximity to our action's goal state is.
        // This is the inverse of the time expected to reach the goal state, derived from the activation times
        // of the schemas in the chain. Also factored in are a value proportional reliability of the schema and inverse of it's cost. 
    }


    public String toString() {
        return "[ActionController "+action+"]";
    }

}
