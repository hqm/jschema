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

    /** Actions that we decide to take in a given time step */
    public ArrayList<Action> voluntaryActions = new ArrayList<Action>();
    
    /** List of schemas that were activated on the last time step */
    public ArrayList<Schema> activatedSchemas = new ArrayList<Schema>();

    /** The time at which the most recent action was initiated */
    public long lastActionTime = -1000;
    public WorldState worldState;

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
            if (item != null) {
                if (item.type == Item.ItemType.SYNTHETIC) {
                    n++;
                }
            }
        }
        return n;
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
        worldState = sms.getWorldState();
        initSchemas();
        copySMSInputToItems(worldState);
        ensureXCRcapacities();
    }

    // name some well known actions for debugging
    Action hand1grasp = null;
    Action hand1ungrasp = null;

    void initSchemas() {
        // Create schemas for the primitive actions
        Action.Type types[] = {
            //Action.Type.HAND1_LEFT, Action.Type.HAND1_RIGHT, 
            Action.Type.HAND1_DOWN,
            Action.Type.HAND1_GRASP, 
            Action.Type.HAND1_UP, 
            Action.Type.HAND1_UNGRASP,
            Action.Type.CENTER_GAZE,
            Action.Type.FOVEATE_NEXT_MOTION,

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

        };
        
        int i = 0;
        for (Action.Type atype: types) {
            Action action = new Action(this, atype.toString(), atype, i);
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

    ArrayList<Item> changedItems = new ArrayList<Item>();

    /**
       
     */
    void updateMarginalAttribution() {
        changedItems.clear();
        // Copy the state information from SensorInputs which changed state since the last action, to Items
        for (SensorInput si : worldState.inputs.values()) {
            long mostRecentTransitionTime = Math.max(si.lastPosTransition, si.lastNegTransition);
            if (mostRecentTransitionTime > lastActionTime) {
                Item item = items.get(si.id);
                if (item == null) {
                    logger.error(String.format("updateMarginalAttribution: error no Item %d found for %s", si.id, si));
                } else {
                    item.lastNegTransition = si.lastNegTransition;
                    item.lastPosTransition = si.lastPosTransition;
                    item.setValue(si.value);
                    changedItems.add(item);
                    logger.debug(clock + ": add changed item "+item);
                }
            }
        }
        if (changedItems.size() > 0) {
            logger.debug("changedItems = "+changedItems);
        }
        
        // [A] TODO Need to augment this to transition "conjunct" items. E.g., if there's a "xyz/a/p" then we need
        // to find if x,y,z all just transitioned with correct sign, and then run marginal attribution on the
        // corresponding conjunct item "xyz".

        long lastActionTime = clock-ACTION_STEP_TIME;

        // Update ExtendedResults counters on all bare schemas
        int nschemas = schemas.size();
        for (Item item: changedItems) {
            for (int j = 0; j < nschemas; j++) {
                Schema schema = schemas.get(j);
                if (schema.bare) {
                    schema.updateResultsCounters(item, lastActionTime);
                }
            }
        }


        //For all schemas which were activated, check if they succeeded
        // If so, update their context counters for every item.                                                        

        // Don't forget we must implement [A] above and have virtual items for each conjunct which is in any schema's result

        for (Schema schema: activatedSchemas) {
            if (!schema.bare) {
                schema.handleActivation();
            }
            schema.actionTaken = false;
        }

        activatedSchemas.clear();

    }

    /** This is called once during init to create corresponding Items for the primitive SensorInputs
        in the SensoriMotorSystem
     */
    void copySMSInputToItems(WorldState w) {
        for (Map.Entry<String, SensorInput> entry : w.inputs.entrySet())
        {
            SensorInput s = entry.getValue();
            String path = s.path;
            boolean newValue = s.value;

            // Ensure that we have enough slots in the items list to put this item by id
            while(items.size() < s.id+1) {
                items.add(null);
            }
            
            Item item = items.get(s.id);
            if (item == null) {
                String name = String.format("#%d:%s",s.id,path);
                item = new Item(this, name, s.id, newValue, Item.ItemType.PRIMITIVE);
            }
            item.lastNegTransition = s.lastNegTransition;
            item.lastPosTransition = s.lastPosTransition;
            items.set(s.id, item);
        }
    }

    Random rand = new Random();

    /** How long to wait between actions */
    public int ACTION_STEP_TIME = 5;

    /** The current schema we are executing */
    public Schema currentSchema = null;
    public Action currentAction = null;

    public boolean run = true;

    /** decides what to do next, sets the appropriate motor actions primitives for WorldState.
        This is a placeholder method for a real action-selection mechanism. It also eventually needs
        to be able to chain together schema action sequences for compound actions.
    */
    void processWorldStep(SensoriMotorSystem sms) {
        // deactivate any prior actions from last time step
        for (Action a: worldState.actions) {
            a.activated = false;
        }

        worldState.actions.clear();

        // Clock speed is 60Hz
        if (run && ((clock % ACTION_STEP_TIME) == 0)) { // perform an action, and do learning, every nth clock cycle

            //logger.info("processWorldStep clock="+clock);
            updateMarginalAttribution(); // update statistics, from results of last action taken

            if (currentSchema != null) {
                // Sec. 4.1.2 pp. 73
                currentSchema.clearPredictedTransitions();
            }

            // TODO [hqm 2013-08] This will of course need to be elaborated when we have composite-actions implemented.
            // For now each schema is mapped one-to-one to a primitive action.
            // We pick a primitive action at random, then activate all applicable schemas that use it, at random.

            currentAction = actions.get(rand.nextInt(actions.size()));
            currentSchema = currentAction.schemas.get(rand.nextInt(currentAction.schemas.size()));

            /*currentAction = actions.get((int)(clock / ACTION_STEP_TIME) % actions.size());
             currentSchema = currentAction.schemas.get(0);
            */

            if (currentAction.type == Action.Type.COMPOSITE) {
                throw new RuntimeException("setMotorActions: we do not support the mapping from compound actions to primitive actions yet");
            }


            // Need to implicitly activate any schemas who share the newly chosen action
            for (Schema schema: currentAction.schemas) {
                schema.activate();
                activatedSchemas.add(schema);
            }
            lastActionTime = clock;

            logger.debug("select action "+currentAction);
            // Send the selected actions to the sensorimotor system to be executed
            worldState.actions.add(currentAction);
            sms.processActions(worldState);
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

