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
    public BitSet ignoreItemsOn = new BitSet();
    public BitSet ignoreItemsOff = new BitSet();

    TIntArrayList onWhenActionSucceeds = new TIntArrayList();
    TIntArrayList onWhenActionFails = new TIntArrayList();
    TIntArrayList offWhenActionSucceeds = new TIntArrayList();
    TIntArrayList offWhenActionFails = new TIntArrayList();

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

        logger.info("*********************");
        logger.info(String.format("updateContextItems Schema %s succeeded=%s", schema, succeeded));

        /* See 4.1.3 Suppressing redundant attribution, to avoid exponential spinoffs.
           We check our lists of  "ignoreItems", to see if any item listed there has the specified value,
           and if so we do not do marginal attribution. Some child schema that we spun off
           will do the marginal attribution.
        */

        int nitems = items.size();
        for (int id = 0; id < nitems; id++) {
            Item item = items.get(id);
            if (item.prevKnownState) {
                boolean val = item.prevValue;
                if ( (ignoreItemsOn.get(id) && val) ||
                     (ignoreItemsOff.get(id) && !val) ) {
                    logger.info("updateContextItems suppressed by "+item);
                    return;
                }
            }
        }


        for (int id = 0; id < nitems; id++) {
            Item item = items.get(id);
            if (item != null) {

                int on_succeeded = onWhenActionSucceeds.get(id);
                int on_failed = onWhenActionFails.get(id);

                int off_succeeded = offWhenActionSucceeds.get(id);
                int off_failed = offWhenActionFails.get(id);
                //
                //logger.info(String.format("   %s pval=%s pknownstate=%s", item, item.prevValue, item.prevKnownState));


                if (item.prevKnownState) {
                    if (item.prevValue == true) {
                        if (succeeded) {
                            on_succeeded += 1;
                            onWhenActionSucceeds.set(id, on_succeeded);
                            //logger.info("increment onWhenActionSucceeds "+item+" "+on_succeeded);
                        } else {
                            on_failed += 1;
                            onWhenActionFails.set(id, on_failed);
                            //logger.info("increment onWhenActionfails "+item+" "+on_failed);
                        }
                    } else { // item prev value was off
                        if (succeeded) {
                            off_succeeded += 1;
                            offWhenActionSucceeds.set(id, off_succeeded);
                            //logger.info("increment offWhenActionSucceeds "+item+" "+off_succeeded);
                        } else {
                            off_failed += 1;
                            offWhenActionFails.set(id, off_failed);
                            //logger.info("increment offWhenActionFails "+item+" "+off_failed);
                        }
                    }
                }

                if ( (on_succeeded == 0 && on_failed == 0) ||
                     (off_succeeded == 0 && off_failed == 0)) {
                    continue;
                }


                float onValueReliability = (float) on_succeeded / ((float) (on_failed + on_succeeded));
                float offValueReliability = (float) off_succeeded / ((float) (off_succeeded + off_failed ));

                int totalOnTrials = (int) (on_succeeded + on_failed);
                int totalOffTrials = (int) (off_succeeded + off_failed);

                    
                double threshold = stage.contextSpinoffReliabilityThresholds.get((int) Math.floor(Math.log10(totalOnTrials + totalOffTrials)));

                if (totalOnTrials + totalOffTrials > stage.contextSpinoffMinTrials) {
                    if ((onValueReliability / offValueReliability) > threshold) {
                        schema.spinoffWithNewContextItem(item, true);
                    }
                }

                if (totalOnTrials + totalOffTrials > stage.contextSpinoffMinTrials) {
                    /* XXX TURN THIS BACK ON WHEN WE FIGURE OUT BUG
                    if ((offValueReliability / onValueReliability) > threshold) {
                        schema.spinoffWithNewContextItem(item, false);
                    }
                    */
                }
            }
        }
    }


    public void clearCounterForItem(int itemNumber) {
        offWhenActionFails.set(itemNumber, 0);
        offWhenActionSucceeds.set(itemNumber, 0);
        onWhenActionSucceeds.set(itemNumber, 0);
        onWhenActionFails.set(itemNumber, 0);
    }

    public void clearAllCounters() {
        offWhenActionFails.fill(0);
        offWhenActionSucceeds.fill(0);
        onWhenActionSucceeds.fill(0);
        onWhenActionFails.fill(0);
    }

    public String toHTML(Stage stage, Schema schema) {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        
        ArrayList<Item> items = stage.items;
        //growArrays(stage.nitems);
        for (int n = 0; n < items.size(); n++) {
            Item item = items.get(n);
            if (item != null) {
                p.println(describeContextItem(item));
            }
        }

        return s.toString();
    }

    String describeContextItem(Item item) {
        int n = item.id;
        float reliabilityWhenOn = (float) onWhenActionSucceeds.get(n) /  (float) onWhenActionFails.get(n);
        float reliabilityWhenOff =  (float) offWhenActionSucceeds.get(n) / (float) offWhenActionFails.get(n);


        return String.format("%d %s On %f [Succ.: %s, Fail: %s],  Off %f [Succ.: %s, Fail: %s] 1/0 %f 0/1 %f <b>%s</b> <b>%s</b>",
                             n, item.makeLink(),
                             reliabilityWhenOn,
                             onWhenActionSucceeds.get(n), onWhenActionFails.get(n),
                             reliabilityWhenOff,
                             offWhenActionSucceeds.get(n), offWhenActionFails.get(n),
                             reliabilityWhenOn / reliabilityWhenOff,
                             reliabilityWhenOff / reliabilityWhenOn,
                             ignoreItemsOn.get(n) ? "IGNORE ON" : "",
                             ignoreItemsOff.get(n) ? "IGNORE OFF" : ""
                             );
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
