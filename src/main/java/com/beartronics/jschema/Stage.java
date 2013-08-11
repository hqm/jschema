package com.beartronics.jschema;

import java.util.*;

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
        return clock++;
    }

    /**
     * print an HTML table of state of items,schemas,actions
     */
    public String htmlPrintState() {
        StringBuilder s = new StringBuilder();
        s.append("<html><body>");
        s.append(String.format("%d items, %d schemas, %d actions\n", items.size(), schemas.size(), actions.size()));
        s.append("<table border=1>");
        s.append("<tr><th>Items</th><th>Schemas</th><th>Actions</th></tr>");
        for (int i = 0; i < items.size(); i++) {
            s.append("<tr>");
            s.append("<td>");
            Item item = items.get(i);
            if (item != null) {
                s.append(item.makeLink());
            }
            s.append("<td>");
            if (i < schemas.size()) {
                Schema schema = schemas.get(i);
                s.append(schema.makeLink());
            }
            s.append("<td>");
            if (i < actions.size()) {
                Action action = actions.get(i);
                s.append(action.makeLink());
            }
            s.append("</tr>");
        }
        s.append("</table>");
        s.append("</body></html>");
        return s.toString();

    }

    public Stage(SensoriMotorSystem s) {
        this.sms = s;
    }

    public void run() {
        System.err.println("stage.run not yet implemented");
    }

    public void initWorld() {
        logger.info("Stage initializing world "+ this);
        WorldState w = sms.getWorldState();
        int ninputs = w.inputs.size();
        logger.info("initWorld adding "+ninputs +"inputs slots");
        for (int i = 0; i < ninputs; i++) {
            items.add(null);
        }

        initSchemas();
    }

    // name some well known actions for debugging
    Action hand1grasp = null;
    Action hand1ungrasp = null;

    void initSchemas() {
        // Create schemas for the primitive actions
        Action.Type types[] = {
            Action.Type.CENTER_GAZE,
            Action.Type.FOVEATE_NEXT_MOTION,
            Action.Type.MOVE_LEFT, Action.Type.MOVE_RIGHT, Action.Type.MOVE_UP, Action.Type.MOVE_DOWN,
            Action.Type.GAZE_LEFT, Action.Type.GAZE_RIGHT, Action.Type.GAZE_UP, Action.Type.GAZE_DOWN,
            Action.Type.FOVEATE_NEXT_OBJECT_LEFT,
            Action.Type.FOVEATE_NEXT_OBJECT_RIGHT,
            Action.Type.FOVEATE_NEXT_OBJECT_UP,
            Action.Type.FOVEATE_NEXT_OBJECT_DOWN, 
            Action.Type.HAND1_LEFT, Action.Type.HAND1_RIGHT, Action.Type.HAND1_UP, Action.Type.HAND1_DOWN,
            Action.Type.HAND2_LEFT, Action.Type.HAND2_RIGHT, Action.Type.HAND2_UP, Action.Type.HAND2_DOWN,
            Action.Type.HAND1_FINE_LEFT, Action.Type.HAND1_FINE_RIGHT, Action.Type.HAND1_FINE_UP, Action.Type.HAND1_FINE_DOWN,
            Action.Type.HAND2_FINE_LEFT, Action.Type.HAND2_FINE_RIGHT, Action.Type.HAND2_FINE_UP, Action.Type.HAND2_FINE_DOWN,
            Action.Type.HAND1_GRASP, Action.Type.HAND1_UNGRASP,
            Action.Type.HAND2_GRASP, Action.Type.HAND2_UNGRASP,
            Action.Type.HAND1_WELD, Action.Type.HAND2_WELD,
            Action.Type.HAND1_UNWELD, Action.Type.HAND2_UNWELD
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

    Schema spinoffNewSchema(Schema parent) {
        Schema schema = new Schema(this, schemas.size(), parent.action);
        schema.parent = parent;
        schemas.add(schema);
        return schema;
    }


    void clearPredictedTransitions() {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            item.clearPredictedTransitions();
        }
    }

    /**
       
     */
    void processWorldStep(SensoriMotorSystem sms) {
        WorldState w = sms.getWorldState();

        // See sec 4.1.2 pp 73
        clearPredictedTransitions();

        // copies the sensori-motor input values from the world into the corresponding Schema items
        copySMSInputToItems(w);

        // run the marginal attribution step
        runMarginalAttribution();

        // decide what to do next
        setMotorActions(w);

        // 
        sms.processActions(w);
    }

    void runMarginalAttribution() {
        for (Schema s: schemas) {
            s.runMarginalAttribution(this);
        }
    }


    void copySMSInputToItems(WorldState w) {
        for (Map.Entry<String, SensorInput> entry : w.inputs.entrySet())
        {
            SensorInput s = entry.getValue();
            int index = s.id;
            String path = s.path;
            boolean newValue = s.value;
            // ensure that we have enough slots in the items list
            if (index > items.size()-1) {
                for (int i = 0; i < (index - items.size()) + 1; i++) {
                    items.add(null);
                }
            }

            Item item = items.get(index);
            if (item == null) {
                item = new Item(this, String.format("#%d:%s",index,path), index, newValue, Item.ItemType.PRIMITIVE);
                logger.debug("created new item "+item);
                items.set(index,item);
            } else {
                item.prevValue = item.value;
                item.value = newValue;
            }
        }
    }

    Random rand = new Random();

    /** decides what to do next, sets primitive motor actions on WorldState */
    void setMotorActions(WorldState w) {
        // deactivate any prior actions
        for (Action a: w.actions) {
            a.activated = false;
        }
        w.actions.clear();
        // hardcode this for debugging for now
        Action action = actions.get(rand.nextInt(actions.size()));
        action.activated = true;
        app.println("select action "+action);
        w.actions.add(action);
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

