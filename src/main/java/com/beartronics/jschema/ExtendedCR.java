package com.beartronics.jschema;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;
import java.io.*;

import org.apache.log4j.Logger;

// Holds the extended context or result arrays
public class ExtendedCR {

    static Logger logger = Logger.getLogger(ExtendedCR.class);

    /* Ignore these items when doing marginal attribution */
    public BitSet ignoreItems = new BitSet();

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

    
    static final float POSITIVE_TRANSITION_CORRELATION_SPINOFF_THRESHOLD = 4.0f;
    static final float NEGATIVE_TRANSITION_CORRELATION_SPINOFF_THRESHOLD = 4.0f;

    static final int MIN_TRIALS = 10;


    /**
     * Made Up Minds Section 4.1.2  pp. 73
     * Loop over all items in system, viewing them as results items.
     *
     * Update transition statistics with respect to whether the our schema's action was taken or not.
     */
    void updateResultItems(Stage stage, Schema schema, boolean actionTaken) {
        ArrayList<Item> items = stage.items;
        //growArrays(stage.nitems);
        for (int n = 0; n < items.size(); n++) {
            if (!ignoreItems.get(n)) {
                Item item = items.get(n);
                if (item != null
                    && !schema.posResult.contains(item)  // dont evalute items already in our pos result set
                    && !schema.negResult.contains(item)) {// dont evaluate items already in our neg result set
                
                    boolean val = item.value;
                    boolean prevValue = item.prevValue;
                    boolean knownState = item.knownState;

                    // read out the existing statistics on the probablity of result transition with/without the action

                    int positiveTransitionsA = offToOnActionTaken.get(n);
                    int positiveTransitionsNA = offToOnActionNotTaken.get(n);

                    int negativeTransitionsA = onToOffActionTaken.get(n);
                    int negativeTransitionsNA = onToOffActionNotTaken.get(n);

                    // Update the item state transition counters 

                    // A synthetic item may be in an unknown state, in which case we do not want
                    // to update stats on it. 
                    if (knownState) {
                        if ( !prevValue && val) { // off to on transition

                            if (actionTaken) {
                                offToOnActionTaken.set(n,  positiveTransitionsA + 1);
                            } else {
                                offToOnActionNotTaken.set(n, positiveTransitionsNA + 1);
                            }
                        } else if (prevValue && !val ) { // on to off transition
                        
                            if (actionTaken) {
                                onToOffActionTaken.set(n, negativeTransitionsA + 1);
                            } else {
                                onToOffActionNotTaken.set(n, negativeTransitionsNA + 1);
                            }
                        }
                        /* code for taking stats on items which remain in their state, with no transition

                           } else if (val && prevValue) {
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

                    float positiveTransitionCorrelation = (float) positiveTransitionsA / (float) positiveTransitionsNA;
                    float negativeTransitionCorrelation = (float) negativeTransitionsA / (float) negativeTransitionsNA;

                    /** per GLD: "My implementation used an ad hoc method that was tied to its
                        space-limited statistics collection method. But the real way to do it
                        is to use a threshold of statistical significance. So just pre-compute
                        a lookup table that says what the minimum correlation is that can be
                        supported by a given sample size."
                    */

                    if (positiveTransitionsA > MIN_TRIALS &&
                        positiveTransitionCorrelation > POSITIVE_TRANSITION_CORRELATION_SPINOFF_THRESHOLD) {
                        //if (item.id == 129) {
                        //logger.info("attempt spin off "+schema + " with result item "+item+" pos_child-with-result-exists="+
                        //schema.childWithResultExists(item, true));
                        //}
                        schema.spinoffWithNewResultItem(item, true);
                    }
                
                    if (negativeTransitionsA > MIN_TRIALS &&
                        negativeTransitionCorrelation > NEGATIVE_TRANSITION_CORRELATION_SPINOFF_THRESHOLD) {
                        schema.spinoffWithNewResultItem(item, false);
                    }
                }
            }
        }
    }

    public void clearNegativeItems(int itemId) {
        onToOffActionNotTaken.set(itemId, 0);
        onToOffActionTaken.set(itemId, 0);
    }

    public void clearPositiveItems(int itemId) {
        offToOnActionNotTaken.set(itemId, 0);
        offToOnActionTaken.set(itemId, 0);
    }

    public String toHTML(Stage stage, Schema schema) {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        
        ArrayList<Item> items = stage.items;
        //growArrays(stage.nitems);
        for (int n = 0; n < items.size(); n++) {
            Item item = items.get(n);
            if (item != null) {
                p.println(String.format("%d %s OFF->ON %f [A: %s, !A: %s],  ON->OFF  %f [A: %s, !A: %s]",
                                        n, item.makeLink(),
                                        (float) offToOnActionTaken.get(n) /  (float) offToOnActionNotTaken.get(n),
                                        offToOnActionTaken.get(n), offToOnActionNotTaken.get(n),
                                        (float) onToOffActionTaken.get(n) / (float) onToOffActionNotTaken.get(n),
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
