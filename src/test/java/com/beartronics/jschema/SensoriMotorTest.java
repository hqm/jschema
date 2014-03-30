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
        sms = new SimpleSensoriMotorSystem(null, null);
    }

    public void testItemTransition() {
    assertTrue(true);
    }

}
