package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;


public class ActionController {

    Action action;
    // need to make a list which can hold proximity values for each schema and be sorted by proximity.
    ArrayList<Schema> schemas = new ArrayList<Schema>();

    public ActionController(Action a) {
        this.action = a;
    }

    public String toString() {
        return "[ActionController "+action+"]";
    }

}
