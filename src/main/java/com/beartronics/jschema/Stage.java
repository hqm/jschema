package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class Stage
{
    public ArrayList<Schema> schemas = new ArrayList<Schema>();
    public ArrayList<Action> actions = new ArrayList<Action>();
    public ArrayList<Item> items     = new ArrayList<Item>();

    int nitems = 0;
    int nschemas = 0;

    public Stage() {

    }

    public void initWorld(int n) {
        System.out.println("initializing world "+ this + ", n = "+n);

        nitems = n;
        nschemas = n;

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
        return String.format("{{ stage %s: nitems=%d schemas=%d }}", this.hashCode(), nitems,nschemas);
    }

}
