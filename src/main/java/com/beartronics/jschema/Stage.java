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

    int nitems;    
    int nschemas;
    int nactions;

    public Stage(SensoriMotorSystem s) {
        this.sms = s;
    }

    public void run() {
        System.err.println("stage.run not yet implemented");
    }

    public void initWorld(int nitems, int nactions) {
        this.nitems = nitems;
        this.nactions = nactions;
        logger.info("Initializing world "+ this + ", nitems = "+nitems + ", nactions = "+nactions);
        nschemas = nactions;
        initItems();
        initSchemas();
    }

    void initItems() {
        for (int i = 0; i < nitems; i++) {
            items.add(null);
        }
    }

    void initSchemas() {
        for (int i = 0; i < nschemas; i++) {
            Action action = new Action(this, String.format(Integer.toString(i), i), i);
            Schema schema = new Schema(this, i, action);
            schema.initialize();
            actions.add(action);
            schemas.add(schema);
        }
    }

    /**
TODO TODO ++++++++++++++++
     */
    void processWorldStep(SensoriMotorSystem sms) {
        WorldState w = sms.getWorldState();

        //
        copySMSInputToItems(w);

        // run the marginal attribution step
        runMarginalAttribution();

        // decide what to do next
        setMotorActions(w);
        sms.processActions(w);
    }

    void runMarginalAttribution() {
        logger.debug("Stage.runMarginalAttribution not yet implemented");
    }


    void copySMSInputToItems(WorldState w) {
        for (Map.Entry<String, SensorInput> entry : w.inputList.entrySet())
        {
            SensorInput s = entry.getValue();
            int index = s.id;
            String path = s.path;
            boolean newValue = s.value;

            Item item = items.get(index);
            if (item == null) {
                item = new Item(String.format("#%d:%s",index,path), index, newValue, Item.ItemType.PRIMITIVE);
                items.set(index,item);
            } else {
                item.prevValue = item.value;
                item.value = newValue;
            }
        }
    }

    /** decides what to do next, sets primitive motor actions on WorldState */
    void setMotorActions(WorldState w) {
        logger.debug("Stage.setMotorActions not yet implemented");
    }

    // Make a synthetic item for a schema
    Item makeSyntheticItem(Schema s) {
        Item item = new Item(String.format(Integer.toString(nitems), nitems), nitems, false, Item.ItemType.SYNTHETIC);
        items.add(item);
        nitems++;
        return item;
    }

    public String toString() {
        return String.format("{{ stage %s: nitems=%d nactions=%d schemas=%d }}", this.hashCode(), nitems, nactions, nschemas);
    }
}

