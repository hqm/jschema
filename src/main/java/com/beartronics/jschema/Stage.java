package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import com.typesafe.config.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Hello world!
 *
 */
public class Stage
{
    final Logger logger =
        LoggerFactory.getLogger(Stage.class);


    public ArrayList<Schema> schemas = new ArrayList<Schema>();
    public ArrayList<Action> actions = new ArrayList<Action>();
    public ArrayList<Item> items     = new ArrayList<Item>();

    int nitems;    
    int nschemas;
    int nactions;

    Config config;

    public Stage(Config conf) {
        config = conf;
        nitems = config.getInt("n-items");
        nactions = config.getInt("n-actions");
    }

    public void run() {
        System.err.println("stage.run not yet implemented");
    }

    public void initWorld() {
        logger.info("Initializing world "+ this + ", nitems = "+nitems + ", nactions = "+nactions);
        nschemas = nactions;
        initItems();
        initSchemas();
    }

    void initItems() {
        for (int i = 0; i < nitems; i++) {
            Item item = new Item(String.format(Integer.toString(i),i), i,  0, Item.ItemType.PRIMITIVE);
            items.add(item);
        }
    }

    void initSchemas() {
        for (int i = 0; i < nschemas; i++) {
            Action action = new Action(String.format(Integer.toString(i),i), i);
            Schema schema = new Schema(i, action);
            schema.initialize(this);
            actions.add(action);
            schemas.add(schema);
        }
    }

    public String toString() {
        return String.format("{{ stage %s: nitems=%d nactions=%d schemas=%d }}", this.hashCode(), nitems,nactions,nschemas);
    }

}
