package com.beartronics.jschema;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;

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

    TFloatArrayList posTransitionActionTaken = new TFloatArrayList();
    TFloatArrayList posTransitionActionNotTaken = new TFloatArrayList();

    TFloatArrayList negTransitionActionTaken = new TFloatArrayList();
    TFloatArrayList negTransitionActionNotTaken = new TFloatArrayList();

    /* need to figure out if these are important
    TFloatArrayList remainedOnActionTaken = new TFloatArrayList();
    TFloatArrayList remainedOnActionNotTaken = new TFloatArrayList();

    TFloatArrayList remainedOffActionTaken = new TFloatArrayList();
    TFloatArrayList remainedOffActionNotTaken = new TFloatArrayList();
    */

    
    static final int MIN_TRIALS = 5;
    static final float recencyBias = 0.999f;

    // event transitions can be seen up to 1 second in the past
    static final int eventTransitionMaxInterval = 60; 

    /** table of correlation threshold needed to spin off a schema, vs log of number of trials */
    double spinoff_correlation_threshold[] = {10.0, 6.0, 4.0, 3.0, 2.5, 2.0, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5 };

    /**
     * Made Up Minds Section 4.1.2  pp. 73
     * Loop over all items in system, viewing them as results items.
     *
     * @param actionTime the most recent time the action was taken
     * Update transition statistics with respect to whether the our schema's action was taken or not.
     */
    void updateResultItem(Stage stage, Schema schema, Item item, boolean actionTaken, long actionTime) {
        int id = item.id;
        if (!ignoreItems.get(id) 
            // TODO [are these filters needed?? redundant with ignoreItems?]
            && !schema.posResult.contains(item)  // dont evalute items already in our pos result set
            && !schema.negResult.contains(item)) {// dont evaluate items already in our neg result set

            // Was there a transition since the action was taken?
            boolean posTransition = item.lastPosTransition > actionTime;
            boolean negTransition = item.lastNegTransition > actionTime;

            boolean knownState = item.knownState;

            // read out the existing statistics on the probablity of result transition with/without the action

            float positiveTransitionsA = posTransitionActionTaken.get(id);
            float positiveTransitionsNA = posTransitionActionNotTaken.get(id);

            float negativeTransitionsA = negTransitionActionTaken.get(id);
            float negativeTransitionsNA = negTransitionActionNotTaken.get(id);

            // Update the item state transition counters 

            // A synthetic item may be in an unknown state, in which case we do not want
            // to update stats on it. 
            if (knownState) {
                if (posTransition && item.predictedPositiveTransition == null) { // 0->1 transition
                    if (actionTaken) {
                        posTransitionActionTaken.set(id,  positiveTransitionsA*recencyBias + 1);
                        posTransitionActionNotTaken.set(id,  positiveTransitionsNA*recencyBias);
                    } else {
                        posTransitionActionNotTaken.set(id, positiveTransitionsNA + 1);
                    }
                } else if (negTransition && item.predictedNegativeTransition == null) { // 1->0 transition
                    if (actionTaken) {
                        negTransitionActionTaken.set(id, negativeTransitionsA*recencyBias + 1);
                        negTransitionActionNotTaken.set(id, negativeTransitionsNA*recencyBias);
                    } else {
                        negTransitionActionNotTaken.set(id, negativeTransitionsNA + 1);
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
            int totalPositiveTrials = (int) (positiveTransitionsA + positiveTransitionsNA);
            int totalNegativeTrials = (int) (negativeTransitionsA + negativeTransitionsNA);

            /** per GLD: "My implementation used an ad hoc method that was tied to its
                space-limited statistics collection method. But the real way to do it
                is to use a threshold of statistical significance. So just pre-compute
                a lookup table that says what the minimum correlation is that can be
                supported by a given sample size."
            */

            if (positiveTransitionsA > MIN_TRIALS) {
                double threshold = spinoff_correlation_threshold[(int) Math.floor(Math.log(totalPositiveTrials))];
                if (positiveTransitionCorrelation > threshold) {
                    logger.info("attempt spinoff "+schema+" item "+item+" pos correlation="+positiveTransitionCorrelation+" threshold="+threshold+" totaltrials="+totalPositiveTrials+" p(A)/p(NA)"+positiveTransitionsA+" / "+positiveTransitionsNA);
                    //if (item.id == 129) {
                    //logger.info("attempt spin off "+schema + " with result item "+item+" pos_child-with-result-exists="+
                    //schema.childWithResultExists(item, true));
                    //}
                    schema.spinoffWithNewResultItem(item, true);
                }
            }
                
            if (negativeTransitionsA > MIN_TRIALS) {
                double threshold = spinoff_correlation_threshold[(int)Math.floor(Math.log(totalNegativeTrials))];
                if (negativeTransitionCorrelation > threshold) {
                    schema.spinoffWithNewResultItem(item, false);
                }
            }
        }
    }

    public void clearNegativeItems(int itemId) {
        negTransitionActionNotTaken.set(itemId, 0);
        negTransitionActionTaken.set(itemId, 0);
    }

    public void clearPositiveItems(int itemId) {
        posTransitionActionNotTaken.set(itemId, 0);
        posTransitionActionTaken.set(itemId, 0);
    }

    public String toHTML(Stage stage, Schema schema) {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        
        ArrayList<Item> items = stage.items;
        //growArrays(stage.nitems);
        for (int n = 0; n < items.size(); n++) {
            Item item = items.get(n);
            if (item != null) {
                p.println(String.format("%d %s &uarr; %f [A: %s, !A: %s],  &darr; %f [A: %s, !A: %s]",
                                        n, item.makeLink(),
                                        (float) posTransitionActionTaken.get(n) /  (float) posTransitionActionNotTaken.get(n),
                                        posTransitionActionTaken.get(n), posTransitionActionNotTaken.get(n),
                                        (float) negTransitionActionTaken.get(n) / (float) negTransitionActionNotTaken.get(n),
                                        negTransitionActionTaken.get(n), negTransitionActionNotTaken.get(n)));
            }
        }

        return s.toString();
    }


    /**
       Makes sure array a can be indexed up to n-1
     */
    void growArray(TFloatArrayList a, int n) {
        int delta = n - a.size();
        for (int i = 0; i < delta; i++) {
            a.add(0);
        }
    }

    void growArrays(int n) {
        growArray(posTransitionActionTaken,n);
        growArray(posTransitionActionNotTaken,n);
        growArray(negTransitionActionTaken,n);
        growArray(negTransitionActionNotTaken,n);
        /*
          growArray(remainedOnActionTaken,n);
          growArray(remainedOnActionNotTaken,n);
          growArray(remainedOffActionTaken,n);
          growArray(remainedOffActionNotTaken,n);
        */
    }

}
