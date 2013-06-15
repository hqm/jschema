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
    public void testApp()
    {
        App app = new App();
        app.init();
        assertEquals("Initial number of items must be as configured", app.stage.nitems, app.stage.items.size());
        assertEquals("Initial number of actions must be as configured", app.stage.nactions, app.stage.actions.size());
        assertEquals("Initial number of schemas must be equal to nactions", app.stage.actions.size(), app.stage.schemas.size());
    }
}
