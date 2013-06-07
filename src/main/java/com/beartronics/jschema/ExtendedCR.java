package com.beartronics.jschema;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendedCR {

    final Logger logger =
        LoggerFactory.getLogger(ExtendedCR.class);
 

    TIntArrayList offToOnActionTaken = new TIntArrayList();
    TIntArrayList offToOnActionNotTaken = new TIntArrayList();

    TIntArrayList onToOffActionTaken = new TIntArrayList();
    TIntArrayList onToOffActionNotTaken = new TIntArrayList();

    TIntArrayList remainedOnActionTaken = new TIntArrayList();
    TIntArrayList remainedOnActionNotTaken = new TIntArrayList();

    TIntArrayList remainedOffActionTaken = new TIntArrayList();
    TIntArrayList remainedOffActionNotTaken = new TIntArrayList();

    void addItem(Item item) {
        offToOnActionTaken.add(0);
        offToOnActionNotTaken.add(0);
        onToOffActionTaken.add(0);
        onToOffActionNotTaken.add(0);
        remainedOnActionTaken.add(0);
        remainedOnActionNotTaken.add(0);
        remainedOffActionTaken.add(0);
        remainedOffActionNotTaken.add(0);
    // we should do a check to make sure that the item we're adding has the index we expect
        if (offToOnActionTaken.size() != item.id) {
            logger.debug("addSchema new schema index "+ item.id +
                         " does not match extended context/result size " +
                         offToOnActionTaken.size());
        }
    }

}
