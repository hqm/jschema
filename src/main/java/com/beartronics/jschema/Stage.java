package com.beartronics.jschema;

import java.util.*;
import java.io.*;
import com.typesafe.config.*;

import org.apache.log4j.Logger;

/**
 * The "Stage" holds the schemas and related items, and a pointer to a
 * sensorimotor system which is hooked to a microworld simulator.
 *
 */
public class Stage
{
    static Logger logger = Logger.getLogger(Stage.class.getName());

    Config config;

    public int gravity = -50;

    public JSchema app;
    public SensoriMotorSystem sms;

    public ArrayList<Schema> schemas = new ArrayList<Schema>();
    public ArrayList<Action> actions = new ArrayList<Action>();
    public ArrayList<Item> items     = new ArrayList<Item>();

    // List of the 'conjunct items', which appear as the context of schemas.
    public ArrayList<Item> conjunctItems     = new ArrayList<Item>();

    // Items which represent the context item list of an existing schema
    public ArrayList<Item> conjunct_items     = new ArrayList<Item>();

    /** Actions that we decide to take in a given time step */
    public ArrayList<Action> voluntaryActions = new ArrayList<Action>();
    
    /** List of schemas that were activated on the last time step */
    public ArrayList<Schema> activatedSchemas = new ArrayList<Schema>();

    /** The time at which the most recent action was initiated */
    public long lastActionTime = -1000;
    public WorldState worldState;

    public boolean atActionStep() {
        return ((clock % actionStepTime) == 0);
    }

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
    public String htmlPrintItems() {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        p.println("<html><body>");
        p.println("<table border=1>");
        p.println("<tr><th>Items</th></tr>");
        for (int i = 0; i < items.size(); i++) {
            p.println("<tr>");
            p.println("<td>");
            Item item = items.get(i);
            if (item != null) {
                p.println(item.makeLink());
            }
            p.println("</tr>");
        }
        p.println("</table>");
        p.println("</body></html>");
        return s.toString();

    }

    public String htmlPrintSchemas() {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        p.println("<html><body>");
        p.println("<table border=1>");
        p.println("<tr><th>Schemas</th></tr>");
        for (int i = 0; i < schemas.size(); i++) {
            p.println("<tr>");
            p.println("<td>");
            if (i < schemas.size()) {
                Schema schema = schemas.get(i);
                p.println(schema.makeLink());
            }
            p.println("<td>");
            p.println("</tr>");
        }
        p.println("</table>");
        p.println("</body></html>");
        return s.toString();

    }

    public String htmlPrintActions() {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        p.println("<html><body>");
        p.println("<table border=1>");
        p.println("<tr><th>Actions</th></tr>");
        for (int i = 0; i < actions.size(); i++) {
            p.println("<tr>");
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


    public Stage(SensoriMotorSystem s, Config config) {
        this.config = config;
        this.sms = s;
        s.stage = this;
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

    List<Double> contextSpinoffReliabilityThresholds;
    List<Double> resultSpinoffCorrelationThresholds;
    int contextSpinoffMinTrials;
    int resultSpinoffMinTrials;

    public void readConfigParams() {
        contextSpinoffReliabilityThresholds = config.getDoubleList("extended-context.spinoff-reliability-thresholds");
        resultSpinoffCorrelationThresholds = config.getDoubleList("extended-result.spinoff-correlation-thresholds");
        contextSpinoffMinTrials = config.getInt("extended-context.min-trials");
        resultSpinoffMinTrials = config.getInt("extended-result.min-trials");
        actionStepTime = config.getInt("action-step-time");
        gravity = config.getInt("gravity");
    }


    public void initWorld() {
        logger.info("Stage initializing world "+ this);

        readConfigParams();
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
            Action.Type.HAND1_LEFT,
            Action.Type.HAND1_RIGHT, 

            Action.Type.HAND1_UP,
            Action.Type.HAND1_DOWN,

            Action.Type.NULL_ACTION, 

            Action.Type.HAND1_GRASP, 
            Action.Type.HAND1_UNGRASP
            /*
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
            */

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
        long actionLookback = lastActionTime;
        int nschemas = schemas.size();

        changedItems.clear();
            
        // N.B.: Run this before we update the item.values, so the
        // inputs we check reflect if a schema was applicable before
        // the action was taken.
        for (int j = 0; j < nschemas; j++) {
            Schema schema = schemas.get(j);
            if (!schema.bare) {
                schema.updateApplicableFlag();
            }
        }

        int i = 0;
        // for each item, copy its current value to prevValue slot
        for (Item item: items) {
            if (item == null) {
                System.err.println("items "+i+" is null");
            } else {
                item.prevValue = item.value;
                item.prevKnownState = item.knownState;
            }
            i++;
        }

        // Update primitive items' values from the primitive inputs.
        // Copy the state information from SensorInputs which changed state since the last action, to Items
        for (SensorInput si : worldState.inputs.values()) {
            long mostRecentTransitionTime = Math.max(si.lastPosTransition, si.lastNegTransition);
            if (mostRecentTransitionTime >= lastActionTime) {
                Item item = items.get(si.id);
                if (item == null) {
                    logger.error(String.format("updateMarginalAttribution: error no Item %d found for %s", si.id, si));
                } else {
                    item.lastNegTransition = si.lastNegTransition;
                    item.lastPosTransition = si.lastPosTransition;
                    item.setValue(si.value);
                    changedItems.add(item);
                    logger.debug(clock + ": add changed item "+item+
                                 (si.lastPosTransition >= lastActionTime ? " POS " : "") +
                                 (si.lastNegTransition >= lastActionTime ? " NEG " : "") );
                }
            }
        }
        if (changedItems.size() > 0) {
            logger.debug("changedItems = "+changedItems);
        }
        
        // [A] TODO Need to augment this to transition "conjunct" items. E.g., if there's a "xyz/a/p" then we need
        // to find if x,y,z all just transitioned with correct sign, and then run marginal attribution on the
        // corresponding conjunct item "xyz".

        // This *could* differ, if we don't want learning to look back
        // too far. But for now, looking back to the last action seems
        // reasonable. Later we might want to look back over the last
        // several actions, but then there is a credit assignment
        // problem.  If a composite action is initiated, we need to
        // store it's start time, to properly correlate results. So in that case maybe the sub-actions that are run
        // as part of it's execution don't count for learning? Or is there a way to do both at once?  The xresult update
        // assignment might run for multiple schemas, but the context learning would go back to the composite action init time?

        // Update ExtendedResults counters on all bare schemas
        for (Item item: changedItems) {
            for (int j = 0; j < nschemas; j++) {
                Schema schema = schemas.get(j);
                if (schema.bare) {
                    schema.updateResultsCounters(item, actionLookback);
                } else {
                    if (schema.conjunctItem != null) {
                        schema.updateConjunctItem();
                    }
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
    public int actionStepTime;

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
        if (run && atActionStep()) { // perform an action, and do learning, every nth clock cycle

            logger.info(String.format("processWorldStep clock=%d HAND1-Y=%s",clock, sms.hand1.grossY ));

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
            // List of all schemas that have this action

            /*currentAction = actions.get((int)(clock / actionStepTime) % actions.size());
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

        sms.stepPhysicalWorld();

    }

    // Make a synthetic item for a schema
    Item makeSyntheticItem(Schema s) {
        int nitems = items.size();
        Item item = new Item(this, Integer.toString(nitems), nitems, false, Item.ItemType.SYNTHETIC);
        item.hostSchema = s;
        items.add(item);
        nitems++;
        return item;
    }

    public int nitems() {
        return items.size();
    }

    public String toString() {
        int nitems = items.size();
        return String.format("{{ stage %s: nitems=%d nactions=%d schemas=%d }}", this.hashCode(), nitems, actions.size(), schemas.size());
    }
}

