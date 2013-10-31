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
public class ExtendedResult {

    static Logger logger = Logger.getLogger(ExtendedResult.class);

    /* Ignore these items when doing marginal attribution */
    public BitSet ignoreItemsPos = new BitSet();
    public BitSet ignoreItemsNeg = new BitSet();

    TIntArrayList posTransitionActionTaken = new TIntArrayList();
    TIntArrayList posTransitionActionNotTaken = new TIntArrayList();

    TIntArrayList negTransitionActionTaken = new TIntArrayList();
    TIntArrayList negTransitionActionNotTaken = new TIntArrayList();

    /* need to figure out if these are important
    TIntArrayList remainedOnActionTaken = new TIntArrayList();
    TIntArrayList remainedOnActionNotTaken = new TIntArrayList();

    TIntArrayList remainedOffActionTaken = new TIntArrayList();
    TIntArrayList remainedOffActionNotTaken = new TIntArrayList();
    */

    /**
     * Made Up Minds Section 4.1.2  pp. 73
     *
     * @param actionTime the most recent time the action was taken
     * Update transition statistics with respect to whether the our schema's action was taken or not.
     */
    void updateResultItem(Stage stage, Schema schema, Item item, boolean actionTaken, long actionTime) {
        int id = item.id;

        // Was there a transition since the action was taken?
        boolean posTransition = item.lastPosTransition > actionTime;
        boolean negTransition = item.lastNegTransition > actionTime;
        
        if (item.id == 0) {
            logger.info(String.format("item %s item.lastPosTransition %s > actionTime %s ? posTransition == %s",  item, item.lastPosTransition, actionTime, posTransition));
            logger.info(String.format("item %s item.lastNegTransition %s > actionTime %s ? negTransition == %s",  item, item.lastNegTransition, actionTime, negTransition));
        }

        boolean knownState = item.knownState;

        if (posTransition && ignoreItemsPos.get(id)) {
            // ignore
        } else if (negTransition && ignoreItemsNeg.get(id)) {
            // ignore
        } else {

            // read out the existing statistics on the probablity of result transition with/without the action
            
            int positiveTransitionsA = posTransitionActionTaken.get(id);
            int positiveTransitionsNA = posTransitionActionNotTaken.get(id);

            int negativeTransitionsA = negTransitionActionTaken.get(id);
            int negativeTransitionsNA = negTransitionActionNotTaken.get(id);

            // Update the item state transition counters 

            // A synthetic item may be in an unknown state, in which case we do not want
            // to update stats on it. 
            if (knownState) {
                if (posTransition && item.predictedPositiveTransition == null) { // 0->1 transition
                    if (actionTaken) {
                        logger.info(String.format("POS-transition-AT %s %s", item, schema));
                        posTransitionActionTaken.set(id,  positiveTransitionsA + 1);
                    } else {
                        logger.info(String.format("POS-transition-NAT %s %s", item, schema));
                        posTransitionActionNotTaken.set(id, positiveTransitionsNA + 1);
                    }
                } else if (negTransition && item.predictedNegativeTransition == null) { // 1->0 transition
                    if (actionTaken) {
                        logger.info(String.format("NEG-transition-AT %s %s", item, schema));
                        negTransitionActionTaken.set(id, negativeTransitionsA + 1);
                    } else {
                        logger.info(String.format("NEG-transition-NAT %s %s", item, schema));
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

            if (positiveTransitionsA > stage.resultSpinoffMinTrials) {
                double threshold = (stage.resultSpinoffCorrelationThresholds).get((int) Math.floor(Math.log10(totalPositiveTrials)));
                if (positiveTransitionCorrelation > threshold) {
                    schema.spinoffWithNewResultItem(item, true);
                }
            }
                
            if (negativeTransitionsA > stage.resultSpinoffMinTrials) {
                double threshold = (stage.resultSpinoffCorrelationThresholds).get((int)Math.floor(Math.log10(totalNegativeTrials)));
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
                p.println(String.format("%d %s <b>^</b> %f [A: %s, !A: %s],  <b>v</b> %f [A: %s, !A: %s]",
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
    void growArray(TIntArrayList a, int n) {
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
