package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The "Stage" holds the schemas and related items, and a pointer to a
 * sensorimotor system which is hooked to a microworld simulator.
 *
 */
public class Stage
{
  final Logger logger =
    LoggerFactory.getLogger(Stage.class);

  SensoriMotorSystem sms;

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
      Item item = new Item(String.format(Integer.toString(i), i), i, 0, Item.ItemType.PRIMITIVE);
      items.add(item);
    }
  }

  void initSchemas() {
    for (int i = 0; i < nschemas; i++) {
      Action action = new Action(String.format(Integer.toString(i), i), i);
      Schema schema = new Schema(i, action);
      schema.initialize(this);
      actions.add(action);
      schemas.add(schema);
    }
  }

  public String toString() {
    return String.format("{{ stage %s: nitems=%d nactions=%d schemas=%d }}", this.hashCode(), nitems, nactions, nschemas);
  }
}

