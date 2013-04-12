package com.beartronics.jschema;

import java.io.InputStream;
import junit.framework.TestCase;

public class SchemaTest extends TestCase {

    public SchemaTest(String name) {
        super(name);
    }

    public void testCreate() throws Exception {
        Action a = new Action("testaction", 0);
        Schema s = new Schema(259, a);
        assertEquals( 259 , s.id );
    }
}
