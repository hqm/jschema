package com.beartronics.jschema;

import java.io.InputStream;
import junit.framework.TestCase;

public class SchemaTest extends TestCase {

    public SchemaTest(String name) {
        super(name);
    }

    public void testCreate() throws Exception {
        JSchema app = new JSchema();
        SensoriMotorSystem sms = new SensoriMotorSystem(app, app.createGraphics(1000,1000));
        Stage stage = new Stage(sms);
        Action a = new Action(stage, "testaction", 0);
        Schema s = new Schema(stage, 259, a);
        assertEquals( 259 , s.id );
    }
}
