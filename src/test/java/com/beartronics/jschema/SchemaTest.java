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
    WorldState worldState;

    protected void setUp() {
        app = new JSchema();
        sms = new SensoriMotorSystem(app, null);
        stage = new Stage(sms);
        worldState = new WorldState();
        worldState.setSensorInput("hand1.grasp-one", 0, false);
        sms.worldState = worldState;
        System.out.println("SchemaTest setup sms.worldState ="+sms.worldState);
        stage.initWorld();
    }

    public void testCreate() {

        Action a = new Action(stage, "testaction", 0);
        Schema s = new Schema(stage, 259, a);
        assertEquals( 259 , s.id );
    }

    static final long NEVER = -1000;
    /**
     * change a Sensory item value, and check that Schema mechanism sees the change.
     */
    public void testItemTransition() {
        worldState.setClock(0);

        // Make a transition on grasp-sensor input
        SensorInput s = worldState.inputs.get("hand1.grasp-one");
        assertEquals("hand1.grasp-one", s.path);
        assertEquals(false, s.value);
        assertEquals( NEVER, s.lastPosTransition);
        assertEquals( NEVER, s.lastNegTransition );

        
        // transition grasp-sensor from 0->1. lastPosTransition should be equal to clock time
        worldState.setClock(5);
        worldState.setSensorInput("hand1.grasp-one", 0, true);        
        assertEquals("hand1.grasp-one", s.path);
        assertEquals(5, s.lastPosTransition);
        assertEquals(NEVER, s.lastNegTransition);        

        // transition back 1->0 , should see lastNegTransition set to clock time
        worldState.setClock(8);
        worldState.setSensorInput("hand1.grasp-one", 0, false);        
        assertEquals(5, s.lastPosTransition);
        assertEquals(8, s.lastNegTransition);        

        // advance clock, copy sensorimotor inputs to schema engine.
        // Should see the values copied from the SensorInput to the corresponding Item object.
        worldState.setClock(12);
        stage.updateMarginalAttribution();
        assertEquals(1, stage.items.size());
        Item item = stage.items.get(0);
        System.out.println("item = "+item);
        assertEquals(false, item.value);
        assertEquals(s.lastNegTransition, item.lastNegTransition);
        assertEquals(s.lastPosTransition, item.lastPosTransition );
        
        // Create a schema, and activate it.
        worldState.setClock(14);
        Action grasp = new Action(stage, "hand1-grasp", Action.Type.HAND1_GRASP, 0);
        Schema schema = new Schema(stage, 0, grasp);
        assertEquals(false, schema.actionTaken);
        assertEquals(null, schema.syntheticItem);
        schema.activate();

        // Run marginal attribution. Schema should show as activated.
        // extended result stats should increment one on the item's positive-transition-with-action-taken counter.
        worldState.setClock(20);
        stage.updateMarginalAttribution();
        /*
          assertEquals(true, schema.actionTaken);
        assertEquals(1.0f, schema.xresult.posTransitionActionTaken.get(0));
        assertEquals(0.0f, schema.xresult.negTransitionActionTaken.get(0));
        assertEquals(0.0f, schema.xresult.posTransitionActionNotTaken.get(0));
        assertEquals(0.0f, schema.xresult.negTransitionActionNotTaken.get(0));
        */
        
    }

}
