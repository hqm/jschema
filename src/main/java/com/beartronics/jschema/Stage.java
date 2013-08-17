package com.beartronics.jschema;

import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

/**
 * The "Stage" holds the schemas and related items, and a pointer to a
 * sensorimotor system which is hooked to a microworld simulator.
 *
 */
public class Stage
{
    static Logger logger = Logger.getLogger(Stage.class.getName());

    public JSchema app;
    public SensoriMotorSystem sms;

    public ArrayList<Schema> schemas = new ArrayList<Schema>();
    public ArrayList<Action> actions = new ArrayList<Action>();
    public ArrayList<Item> items     = new ArrayList<Item>();

    // Actions that we decide to take in a given time step
    public ArrayList<Action> voluntaryActions = new ArrayList<Action>();

    public long clock = 0;
    public long clockStep() {
        if (clock % 100 == 0) {
            logger.info(String.format("clock = %d", clock));
        }
        return clock++;
    }

    public int countSyntheticItems() {
        int n = 0;
        for (Item item: items) {
            if (item.type == Item.ItemType.SYNTHETIC) {
                n++;
            }
        }
        return n;
    }

    public void clearPredictedItemTransitions() {
        for (Item item: items) {
            item.predictedPositiveTransition = null;
            item.predicteNegativeTransition = null;
        }
    }

    /**
     * print an HTML table of state of items,schemas,actions
     */
    public String htmlPrintState() {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        p.println("<html><body>");
        p.println(String.format("clock: %d, items: %d (%d syn), schemas: %d, actions: %d\n",
                                clock,
                                items.size(),
                                countSyntheticItems(),
                                schemas.size(),
                                actions.size()));
        p.println("<table border=1>");
        p.println("<tr><th>Items</th><th>Schemas</th><th>Actions</th></tr>");
        for (int i = 0; i < items.size(); i++) {
            p.println("<tr>");
            p.println("<td>");
            Item item = items.get(i);
            if (item != null) {
                p.println(item.makeLink());
            }
            p.println("<td>");
            if (i < schemas.size()) {
                Schema schema = schemas.get(i);
                p.println(schema.makeLink());
            }
            p.println("<td>");
            if (i < actions.size()) {
                Action action = actions.get(i);
                p.println(action.makeLink());
            }
            p.println("</tr>");
        }
        p.println("</table>");
        p.println("</body></html>");
        return s.toString();

    }

    public Stage(SensoriMotorSystem s) {
        this.sms = s;
    }

    public void run() {
        System.err.println("stage.run not yet implemented");
    }

    /**  Ensure that the extended contexts have slots for all items */
    public void ensureXCRcapacities() {
        for (Schema s: schemas) {
            s.growArrays(items.size());
        }
    }

    public void initWorld() {
        logger.info("Stage initializing world "+ this);
        WorldState w = sms.getWorldState();
        initSchemas();
        copySMSInputToItems(w);
        ensureXCRcapacities();
    }

    // name some well known actions for debugging
    Action hand1grasp = null;
    Action hand1ungrasp = null;

    void initSchemas() {
        // Create schemas for the primitive actions
        Action.Type types[] = {
            //Action.Type.HAND1_LEFT, Action.Type.HAND1_RIGHT, 
            Action.Type.HAND1_UP, Action.Type.HAND1_DOWN,
            Action.Type.HAND1_GRASP, Action.Type.HAND1_UNGRASP,
            //            Action.Type.CENTER_GAZE,
            //            Action.Type.FOVEATE_NEXT_MOTION,
            /*

            Action.Type.MOVE_LEFT, Action.Type.MOVE_RIGHT, Action.Type.MOVE_UP, Action.Type.MOVE_DOWN,
            Action.Type.GAZE_LEFT, Action.Type.GAZE_RIGHT, Action.Type.GAZE_UP, Action.Type.GAZE_DOWN,
            Action.Type.FOVEATE_NEXT_OBJECT_LEFT,
            Action.Type.FOVEATE_NEXT_OBJECT_RIGHT,
            Action.Type.FOVEATE_NEXT_OBJECT_UP,
            Action.Type.FOVEATE_NEXT_OBJECT_DOWN, 

            Action.Type.HAND2_LEFT, Action.Type.HAND2_RIGHT, Action.Type.HAND2_UP, Action.Type.HAND2_DOWN,
            Action.Type.HAND1_FINE_LEFT, Action.Type.HAND1_FINE_RIGHT, Action.Type.HAND1_FINE_UP, Action.Type.HAND1_FINE_DOWN,
            Action.Type.HAND2_FINE_LEFT, Action.Type.HAND2_FINE_RIGHT, Action.Type.HAND2_FINE_UP, Action.Type.HAND2_FINE_DOWN,
            Action.Type.HAND2_GRASP, Action.Type.HAND2_UNGRASP,
            Action.Type.HAND1_WELD, Action.Type.HAND2_WELD,
            Action.Type.HAND1_UNWELD, Action.Type.HAND2_UNWELD
            */
        };
        
        int i = 0;
        for (Action.Type atype: types) {
            Action action = new Action(this, atype.toString(), atype, i, false);
            // for debugging 
            if (atype == Action.Type.HAND1_GRASP) {
                hand1grasp = action;
            } else if (atype == Action.Type.HAND1_UNGRASP) {
                hand1ungrasp = action;
            }


            logger.info("action = "+action);
            Schema schema = new Schema(this, i, action);
            schema.initialize();
            actions.add(action);
            schemas.add(schema);
            i++;
        }
    }


    void clearPredictedTransitions() {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item != null) {
                item.clearPredictedTransitions();
            } else {
                app.println("should not occur: null item at "+i);
            }
        }
    }

    /**
       
     */
    void processWorldStep(SensoriMotorSystem sms) {
        WorldState w = sms.getWorldState();

        // See sec 4.1.2 pp 73
        //        clearPredictedTransitions();

        // decide what to do next
        chooseNextActions(w);
    }

    void updateMarginalAttribution() {
        for (int i = 0 ; i < schemas.size(); i++) {
            Schema s = schemas.get(i);
            s.updateSyntheticItems();
        }
        for (int i = 0 ; i < schemas.size(); i++) {
            Schema s = schemas.get(i);
            s.runMarginalAttribution();
        }


    }

    // Map from input path name string to object
    public HashMap<String,Item> itemPathnameToObject = new HashMap<String,Item>();

    // for debugging 
    HashSet<Item> changedItems = new HashSet<Item>();

    HashSet<Item> copySMSInputToItems(WorldState w) {
        changedItems.clear();

        for (Map.Entry<String, SensorInput> entry : w.inputs.entrySet())
        {
            SensorInput s = entry.getValue();
            String path = s.path;
            boolean newValue = s.value;

            // USE HASHMAP
            // ensure that we have enough slots in the items list
            Item item = itemPathnameToObject.get(path);
            if (item == null) {
                item = new Item(this, null, -1, newValue, Item.ItemType.PRIMITIVE);
                itemPathnameToObject.put(path, item);
                items.add(item);
                int index = items.size()-1;
                item.id = index;
                item.name = String.format("#%d:%s",index,path);
            }
            
            item.value = newValue;
            item.lastNegTransition = s.lastNegTransition;
            item.lastPosTransition = s.lastPosTransition;
            if ((clock - s.lastNegTransition) < ExtendedCR.eventTransitionMaxInterval
                || (clock - s.lastPosTransition) < ExtendedCR.eventTransitionMaxInterval) {
                changedItems.add(item);
            }
        }

        if (changedItems.size() > 0) {
            logger.info("changed items = "+changedItems);
        }

        return changedItems;
    }

    Random rand = new Random();

    /* */
    static final int ACTION_DURATION = 15;

    /** decides what to do next, sets the appropriate motor actions primitives for WorldState.
        This is a placeholder method for a real action-selection mechanism. It also eventually needs
        to be able to chain together schema action sequences for compound actions.
    */
    void chooseNextActions(WorldState w) {
        // deactivate any prior actions from last time step
        for (Action a: w.actions) {
            a.activated = false;
        }
        w.actions.clear();

        // Clock speed is 60Hz
        if ((clock % ACTION_DURATION) == 0) { // perform an action, and do learning, every nth clock cycle

            // copies the sensori-motor input values from the world into the corresponding Schema items

            copySMSInputToItems(w);

            clearPredictedItemTransitions(); // Sec. 4.1.2 pp. 73

            updateMarginalAttribution(); // update statistics, from results of last action taken

            // TODO [hqm 2013-08] This will of course need to be elaborated when we have compound actions implemented.
            // For now each schema is mapped one-to-one to a primitive action.
            // We pick a primitive action at random, then execute a schema that uses it at random.
            Action action = actions.get(rand.nextInt(actions.size()));
            Schema schema = action.schemas.get(rand.nextInt(action.schemas.size()));

            if (action.type == Action.Type.COMPOUND) {
                throw new RuntimeException("setMotorActions: we do not support the mapping from compound actions to primitive actions yet");
            }
            schema.activate();
            logger.debug("select schema "+schema);
            w.actions.add(action);

            // 
            sms.processActions(w);
        }
    }

    // Make a synthetic item for a schema
    Item makeSyntheticItem(Schema s) {
        int nitems = items.size();
        Item item = new Item(this, String.format(Integer.toString(nitems), nitems), nitems, false, Item.ItemType.SYNTHETIC);
        item.hostSchema = s;
        items.add(item);
        nitems++;
        return item;
    }

    public int nitems() {
        return items.size();
    }

    public static int INITIAL_ITEMS = 1000;

    public String toString() {
        int nitems = items.size();
        return String.format("{{ stage %s: nitems=%d nactions=%d schemas=%d }}", this.hashCode(), nitems, actions.size(), schemas.size());
    }
}

