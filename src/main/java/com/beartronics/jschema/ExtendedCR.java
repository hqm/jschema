package com.beartronics.jschema;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

// Holds the extended context or result arrays
public class ExtendedCR {

    static Logger logger = Logger.getLogger(ExtendedCR.class);

    TIntArrayList offToOnActionTaken = new TIntArrayList();
    TIntArrayList offToOnActionNotTaken = new TIntArrayList();

    TIntArrayList onToOffActionTaken = new TIntArrayList();
    TIntArrayList onToOffActionNotTaken = new TIntArrayList();

    TIntArrayList remainedOnActionTaken = new TIntArrayList();
    TIntArrayList remainedOnActionNotTaken = new TIntArrayList();

    TIntArrayList remainedOffActionTaken = new TIntArrayList();
    TIntArrayList remainedOffActionNotTaken = new TIntArrayList();

    void ensureCapacity(int n) {
        offToOnActionTaken.ensureCapacity(n);
        offToOnActionNotTaken.ensureCapacity(n);
        onToOffActionTaken.ensureCapacity(n);
        onToOffActionNotTaken.ensureCapacity(n);
        remainedOnActionTaken.ensureCapacity(n);
        remainedOnActionNotTaken.ensureCapacity(n);
        remainedOffActionTaken.ensureCapacity(n);
        remainedOffActionNotTaken.ensureCapacity(n);
    }

}
