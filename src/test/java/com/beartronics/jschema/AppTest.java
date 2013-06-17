package com.beartronics.jschema;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Tests go here
     */
    public void testStage()
    {
        // Need to pass in dummy sensorimotor system? 
        Stage stage = new Stage(null);
        stage.initWorld(10,10);
        assertEquals("Initial number of items must be as configured", stage.nitems, stage.items.size());
        assertEquals("Initial number of actions must be as configured", stage.nactions, stage.actions.size());
        assertEquals("Initial number of schemas must be equal to nactions", stage.actions.size(), stage.schemas.size());
    }
}
