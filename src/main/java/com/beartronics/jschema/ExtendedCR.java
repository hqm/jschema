package com.beartronics.jschema;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

import org.apache.log4j.Logger;

// Holds the extended context or result arrays
public class ExtendedCR {

    static Logger logger = Logger.getLogger(ExtendedCR.class);

    TIntArrayList offToOnActionTaken = new TIntArrayList();
    TIntArrayList offToOnActionNotTaken = new TIntArrayList();

    TIntArrayList onToOffActionTaken = new TIntArrayList();
    TIntArrayList onToOffActionNotTaken = new TIntArrayList();

    /* need to figure out if these are important
    TIntArrayList remainedOnActionTaken = new TIntArrayList();
    TIntArrayList remainedOnActionNotTaken = new TIntArrayList();

    TIntArrayList remainedOffActionTaken = new TIntArrayList();
    TIntArrayList remainedOffActionNotTaken = new TIntArrayList();
    */

    // Loop over all items in system, update the marginal attribution values
    void updateItems(Stage stage, Schema schema, boolean actionTaken) {
        ArrayList<Item> items = stage.items;
        //growArrays(stage.nitems);
        for (int n = 0; n < items.size(); n++) {
            Item item = items.get(n);
            if (item != null) {
                boolean val = item.value;
                boolean prevValue = item.prevValue;

                if (val && !prevValue) {
                    if (actionTaken) {
                        offToOnActionTaken.set(n, offToOnActionTaken.get(n) + 1);
                    } else {
                        offToOnActionNotTaken.set(n, offToOnActionNotTaken.get(n) + 1);
                    }
                } else if (!val && prevValue) {
                    if (actionTaken) {
                        onToOffActionTaken.set(n, onToOffActionTaken.get(n) + 1);
                    } else {
                        onToOffActionNotTaken.set(n, onToOffActionNotTaken.get(n) + 1);
                    }
                }

                /*            } else if (val && prevValue) {
                              if (actionTaken) {
                              remainedOnActionTaken.set(n, remainedOnActionTaken.get(n) + 1);
                              } else {
                              remainedOnActionNotTaken.set(n, remainedOnActionNotTaken.get(n) + 1);
                              }
                              } else if (val && prevValue) {
                              if (actionTaken) {
                              remainedOnActionTaken.set(n, remainedOnActionTaken.get(n) + 1);
                              } else {
                              remainedOnActionNotTaken.set(n, remainedOnActionNotTaken.get(n) + 1);
                              }
                              }
                */
            }
        }
    }

    public String toHTML(Stage stage, Schema schema) {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        
        ArrayList<Item> items = stage.items;
        //growArrays(stage.nitems);
        for (int n = 0; n < items.size(); n++) {
            Item item = items.get(n);
            if (item != null) {
                p.println(String.format("%d %s off->on(A): %s, off->on(!A): %s, on->off(A): %s, on->off(!A): %s",
                                        n, item.makeLink(), offToOnActionTaken.get(n), offToOnActionNotTaken.get(n),
                                        onToOffActionTaken.get(n), onToOffActionNotTaken.get(n)));
            }
        }

        return s.toString();
    }


    /**
       Makes sure array a can be indexed up to n-1
     */
    void growArray(TIntArrayList a, int n) {
        int delta = n - a.size();
        for (int i = 0; i < delta; i++) {
            a.add(0);
        }
    }

    void growArrays(int n) {
        growArray(offToOnActionTaken,n);
        growArray(offToOnActionNotTaken,n);
        growArray(onToOffActionTaken,n);
        growArray(onToOffActionNotTaken,n);
        /*
          growArray(remainedOnActionTaken,n);
          growArray(remainedOnActionNotTaken,n);
          growArray(remainedOffActionTaken,n);
          growArray(remainedOffActionNotTaken,n);
        */
    }

}
