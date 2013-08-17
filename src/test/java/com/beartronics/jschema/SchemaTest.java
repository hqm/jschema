package com.beartronics.jschema;

import java.io.InputStream;
import junit.framework.TestCase;

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
        // Make a transition on grasp-sensor
        worldState.setSensorInput("hand1.grasp-one", 0, false);
        SensorInput s = worldState.inputs.get("hand1.grasp-one");
        assertEquals(s.path, "hand1.grasp-one");        
        assertEquals(s.value, false);
        assertEquals(s.lastPosTransition, Integer.MIN_VALUE);
        assertEquals(s.lastNegTransition, Integer.MIN_VALUE);

        
        clock  = 5;
        worldState.setClock(clock);
        worldState.setSensorInput("hand1.grasp-one", 0, true);        
        assertEquals(s.path, "hand1.grasp-one");
        assertEquals(s.lastPosTransition, 5);
        assertEquals(s.lastNegTransition, Integer.MIN_VALUE);        

        clock = 8;
        worldState.setClock(clock);
        worldState.setSensorInput("hand1.grasp-one", 0, false);        
        assertEquals(s.lastPosTransition, 5);
        assertEquals(s.lastNegTransition, 8);        

        
    }

}
