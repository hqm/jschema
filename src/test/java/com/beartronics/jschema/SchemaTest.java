package com.beartronics.jschema;

import java.io.InputStream;
import junit.framework.TestCase;
import java.util.*;

public class SchemaTest extends TestCase {

    public SchemaTest() {
    }

    JSchema app;
    SensoriMotorSystem sms;
    Stage stage;
    
    protected void setUp() {
        app = new JSchema();
        sms = new SensoriMotorSystem(app, null);
        stage = new Stage(sms);
    }

    public void testCreate() {

        Action a = new Action(stage, "testaction", 0);
        Schema s = new Schema(stage, 259, a);
        assertEquals( 259 , s.id );
    }

    /**
     * change a Sensory item value, and check that Schema mechanism sees the change.
     */
    public void testItemTransition() {
        long clock = 0;
        WorldState worldState = new WorldState();
        sms.worldState = worldState;
        worldState.setClock(clock);

        // Make a transition on grasp-sensor input
        worldState.setSensorInput("hand1.grasp-one", 0, false);
        SensorInput s = worldState.inputs.get("hand1.grasp-one");
        assertEquals("hand1.grasp-one", s.path);
        assertEquals(false, s.value);
        assertEquals( Integer.MIN_VALUE, s.lastPosTransition);
        assertEquals( Integer.MIN_VALUE, s.lastNegTransition );

        
        // transition grasp-sensor from 0->1. lastPosTransition should be equal to clock time
        clock  = 5;
        worldState.setClock(clock);
        worldState.setSensorInput("hand1.grasp-one", 0, true);        
        assertEquals("hand1.grasp-one", s.path);
        assertEquals(5, s.lastPosTransition);
        assertEquals(Integer.MIN_VALUE, s.lastNegTransition);        

        // transition back 1->0 , should see lastNegTransition set to clock time
        clock = 8;
        worldState.setClock(clock);
        worldState.setSensorInput("hand1.grasp-one", 0, false);        
        assertEquals(5, s.lastPosTransition);
        assertEquals(8, s.lastNegTransition);        

        // advance clock, copy sensorimotor inputs to schema engine.
        // Should see the values copied from the SensorInput to the corresponding Item object.
        clock = 12;
        HashSet<Item> changed = stage.copySMSInputToItems(worldState);
        Item item = stage.items.get(0);
        assertEquals(false, item.value);
        assertEquals(s.lastNegTransition, item.lastNegTransition);
        assertEquals(s.lastPosTransition, item.lastPosTransition );
        assertEquals(1, changed.size());
        assertEquals(true, changed.contains(item));
        
        // Create a schema, and activate it.
        clock = 14;
        Action grasp = new Action(stage, "testaction", Action.Type.HAND1_GRASP, 0, false);
        Schema schema = new Schema(stage, 0, grasp);
        assertEquals(false, schema.actionTaken);
        assertEquals(null, schema.syntheticItem);
        schema.activate();

        // Run marginal attribution. Schema should show as activated.
        // extended result stats should increment one on the item's positive-transition-with-action-taken counter.
        clock = 20;
        changed = stage.copySMSInputToItems(worldState);
        schema.runMarginalAttribution(item);
        assertEquals(true, schema.actionTaken);
        assertEquals(1.0f, schema.xresult.posTransitionActionTaken.get(0));
        assertEquals(0.0f, schema.xresult.negTransitionActionTaken.get(0));
        assertEquals(0.0f, schema.xresult.posTransitionActionNotTaken.get(0));
        assertEquals(0.0f, schema.xresult.negTransitionActionNotTaken.get(0));

        
    }

}
