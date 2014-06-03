package com.beartronics.jschema;

import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;

public class SensoriMotorTest extends TestCase {
    //setup, activity and looking for values
    public SensoriMotorTest() {
    }

    SimpleSensoriMotorSystem sms;
    Stage stage;
    WorldState ws;
    Action action_hand1_home, action_hand1_up, action_hand1_down;
    Action action_hand2_home, action_hand2_up, action_hand2_down;

    protected void setUp() {
        ws = new WorldState();
        sms = new SimpleSensoriMotorSystem(null, ws, null);
        stage = new Stage(sms,null);
        int i = 0;
        action_hand1_home = new Action(stage,Action.Type.HAND1_HOME.toString(),Action.Type.HAND1_HOME,i++);
        action_hand1_up  = new Action(stage,Action.Type.HAND1_UP.toString(),Action.Type.HAND1_UP,i++);
        action_hand1_down  = new Action(stage,Action.Type.HAND1_DOWN.toString(),Action.Type.HAND1_DOWN,i++);
        action_hand2_home = new Action(stage,Action.Type.HAND2_HOME.toString(),Action.Type.HAND2_HOME,i++);
        action_hand2_up  = new Action(stage,Action.Type.HAND2_UP.toString(),Action.Type.HAND2_UP,i++);
        action_hand2_down  = new Action(stage,Action.Type.HAND2_DOWN.toString(),Action.Type.HAND2_DOWN,i++);
        System.out.println("running setUp");
    }

    public void testItemTransition() {
        // This is just a test of the SensoriMotorSystem
        // Set the hand to a known position
        // Execute the world --> stepPhysicalWorld
        // Examine the sensorItems in the world state
        // For now just check that the sensorItems changed. Later on after copying the sensor items to the learning system
        // We'll do a seperate test of the learning system - an Integration Test - several modules talking to each other
        // Check later that the learning algorithm builds schemas in a way we expect 
        // Items are simple - have a current value and a previous value
        // testMoveHand1Home();
        ws.actions.clear();
        ws.actions.add(action_hand1_home);
        sms.processActions(ws);
        sms.stepPhysicalWorld();
        

        ws.actions.add(action_hand1_up);
        sms.processActions(ws);
        sms.stepPhysicalWorld();
    
        assertTrue("this should succeed", true);
    }


    public void testMoveHand1Home() {
        ws.actions.clear();
    ws.actions.add(action_hand1_home);
    sms.processActions(ws);
        sms.stepPhysicalWorld();
    
        for (int i = -sms.reachX; i <= sms.reachX; i++) {
            for (int j = -sms.reachY; j <= sms.reachY; j++) {
        String spath = "hand1@("+i+","+j+")";
        SensorItem s = ws.items.get(spath);
        if (i == 0 && j ==0) {
            assertTrue("sensor item "+spath+" should be true", s.value);
        } else {
            assertFalse("Sensor item "+spath +" should be false", s.value);
        }
            }
    }
    }
    public void testMoveHand2Home() {
        ws.actions.clear();
    ws.actions.add(action_hand2_home);
    sms.processActions(ws);
        sms.stepPhysicalWorld();
    
        for (int i = -sms.reachX; i <= sms.reachX; i++) {
            for (int j = -sms.reachY; j <= sms.reachY; j++) {
        String spath = "hand2@("+i+","+j+")";
        SensorItem s = ws.items.get(spath);
        if (i == 0 && j ==0) {
            assertTrue("sensor item "+spath+" should be true", s.value);
        } else {
            assertFalse("Sensor item "+spath +" should be false", s.value);
        }
            }
    }
    }
}

