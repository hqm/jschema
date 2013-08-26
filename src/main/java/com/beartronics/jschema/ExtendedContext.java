package com.beartronics.jschema;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;
import java.io.*;

import org.apache.log4j.Logger;

// Holds the extended context or result arrays
public class ExtendedContext {

    static Logger logger = Logger.getLogger(ExtendedContext.class);

    /* Ignore these items when doing marginal attribution */
    public BitSet ignoreItems = new BitSet();

    TIntArrayList onWhenActionSucceeds = new TIntArrayList();
    TIntArrayList onWhenActionFails = new TIntArrayList();
    TIntArrayList offWhenActionSucceeds = new TIntArrayList();
    TIntArrayList offWhenActionFails = new TIntArrayList();

    static final int MIN_TRIALS = 25;

    /** table of correlation threshold needed to spin off a schema, vs log of number of trials */
    double spinoff_correlation_threshold[] = {5.0, 4.0, 2.0, 2.0, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5};

    /**
     * Made Up Minds Section 4.1.2  
     *
     * @param the schema was activated 
     * @params whether the action succeeded or failed on last activation
     * take statistics on which items were on/off right before the action was initiated
     * Update transition statistics with respect to whether the our schema's action was taken or not.
     */
    void updateContextItems(Stage stage, Schema schema, Boolean succeeded, long actionTime) {
        ArrayList<Item> items = stage.items;
        int nitems = items.size();
        for (int id = 0; id < nitems; id++) {
            if (!ignoreItems.get(id)) {
                Item item = items.get(id);
                if (item != null) {

                    int on_succeeded = onWhenActionSucceeds.get(id);
                    int on_failed = onWhenActionFails.get(id);

                    int off_succeeded = offWhenActionSucceeds.get(id);
                    int off_failed = offWhenActionFails.get(id);


                    if (item.prevKnownState) {
                        if (item.prevValue == true) {
                            if (succeeded) {
                                onWhenActionSucceeds.set(id, on_succeeded+ 1);
                            } else {
                                onWhenActionFails.set(id, on_failed + 1);
                            }
                        } else {
                            if (succeeded) {
                                offWhenActionSucceeds.set(id, off_succeeded + 1);
                            } else {
                                offWhenActionFails.set(id, off_failed + 1);
                            }
                        }
                    }

                    float onValueCorrelation = (float) on_succeeded / (float) Math.max(1, on_failed);
                    float offValueCorrelation = (float) off_succeeded / (float) Math.max(1, off_failed);

                    int totalOnTrials = (int) (on_succeeded + on_failed);
                    int totalOffTrials = (int) (off_succeeded + off_failed);

                    
                    if (totalOnTrials + totalOffTrials > MIN_TRIALS) {
                        double threshold = spinoff_correlation_threshold[(int) Math.floor(Math.log(totalOnTrials))];
                        if ((onValueCorrelation / offValueCorrelation) > threshold) {
                            schema.spinoffWithNewContextItem(item, true);
                        }
                    }

                    if (totalOnTrials + totalOffTrials > MIN_TRIALS) {
                        double threshold = spinoff_correlation_threshold[(int) Math.floor(Math.log(totalOffTrials))];
                        if ((offValueCorrelation / onValueCorrelation) > threshold) {
                            schema.spinoffWithNewContextItem(item, false);
                        }
                    }
                }
            }
        }
    }

    public void clearOffItems(int itemId) {
        offWhenActionFails.set(itemId, 0);
        offWhenActionSucceeds.set(itemId, 0);
    }

    public void clearOnItems(int itemId) {
        onWhenActionSucceeds.set(itemId, 0);
        onWhenActionFails.set(itemId, 0);
    }

    public String toHTML(Stage stage, Schema schema) {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        
        ArrayList<Item> items = stage.items;
        //growArrays(stage.nitems);
        for (int n = 0; n < items.size(); n++) {
            Item item = items.get(n);
            float reliabilityWhenOn = (float) onWhenActionSucceeds.get(n) /  (float) onWhenActionFails.get(n);
            float reliabilityWhenOff =  (float) offWhenActionSucceeds.get(n) / (float) offWhenActionFails.get(n);
            if (item != null) {
                p.println(String.format("%d %s On %f [Succ.: %s, Fail: %s],  Off %f [Succ.: %s, Fail: %s] 1/0 %f 0/1 %f <b>%s</b>",
                                        n, item.makeLink(),
                                        reliabilityWhenOn,
                                        onWhenActionSucceeds.get(n), onWhenActionFails.get(n),
                                        reliabilityWhenOff,
                                        offWhenActionSucceeds.get(n), offWhenActionFails.get(n),
                                        reliabilityWhenOn / reliabilityWhenOff,
                                        reliabilityWhenOff / reliabilityWhenOn,
                                        ignoreItems.get(n) ? "IGNORE" : ""
                                        ));
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
        growArray(onWhenActionSucceeds,n);
        growArray(onWhenActionFails,n);
        growArray(offWhenActionSucceeds,n);
        growArray(offWhenActionFails,n);
    }

}
