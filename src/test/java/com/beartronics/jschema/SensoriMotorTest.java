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
    WorldState worldState;

    protected void setUp() {
        worldState = new WorldState();
        sms = new SimpleSensoriMotorSystem(null, worldState, null);
	stage = new Stage(sms,null);
      System.out.println("running setUp");
    }

    public void testItemTransition() {
    assertTrue("this should succeed", true);
    }

    public void test2() {
    assertTrue("this should succeed", true);
    }

    public void testMoveHand() {
	Action a = new Action(stage,Action.Type.HAND1_HOME,Action.Type.HAND1_HOME,0);
        worldState.actions.clear();
	worldState.actions.add(a);
    assertTrue("this should succeed", true);
    }

}
