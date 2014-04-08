package com.beartronics.jschema;

import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;

public class SensoriMotorTest extends TestCase {

    public SensoriMotorTest() {
    }

    SimpleSensoriMotorSystem sms;

    protected void setUp() {
        WorldState worldState = new WorldState();
        sms = new SimpleSensoriMotorSystem(null, worldState, null);
        System.out.println("running setUp");
    }

    public void testItemTransition() {
    assertTrue("this should succeed", true);
    }

    public void test2() {
    assertTrue("this should succeed", true);
    }

    public void testMoveHand() {
    assertTrue("this should succeed", true);
    }

}
