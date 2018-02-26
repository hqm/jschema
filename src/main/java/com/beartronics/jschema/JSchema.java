package com.beartronics.jschema;

import processing.core.PApplet;


import com.shigeodayo.pframe.*;

import pbox2d.*;

import processing.core.*;

import org.apache.log4j.Logger;
import java.util.*;


import com.typesafe.config.*;

// serialization lib


public class JSchema extends PApplet {

    static Logger logger = Logger.getLogger(JSchema.class.getName());
    static final String VERSION = "0.1";

    public static JSchema app;
    public static Box2DSensoriMotorSystem sms;

    public boolean interactive = true;

    float gravity = -25.0f;

    Config config;

    public JSchema() {
        JSchema.app = this;
        logger.info("JSchema app created.");
        // load config file
        config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference(), "box2d");
        logger.info(config.root().render());

    }

    PBox2D box2d = null;

    PBox2D createBox2D() {
        box2d = new PBox2D(this);
        return box2d;
    }

    PFrame retinaFrame = null;
    RetinaView retinaView = null;
    PGraphics retinaImage = null;
    

    // We don't want to really call stepPhysicalWorld here, this is just placeholder until
    // someone calls it from some brain loop.
    public void draw() {
        retinaView.set(0,0,retinaImage);
    }


    public void setup() {
        size(1200, 800);

        retinaView = new RetinaView();
        retinaFrame = new PFrame(retinaView, 210, 0);
        retinaFrame.setTitle("Retina View");

        retinaImage = createGraphics(1000,1000);

        smooth();
        background(255);

        sms = new Box2DSensoriMotorSystem(this, retinaImage);
        sms.setupDisplay();
        sms.computeWorldState();
        frameRate(1024);

        SMSCrossbarClient smsclient = new SMSCrossbarClient( sms );
        smsclient.connect();

    }


    public void keyPressed() {


        sms.keyPressed();
    }
        
    public void keyReleased() {
        sms.keyReleased();
    }

    public void mousePressed() {
        sms.mousePressed();
    }

    public void mouseReleased() {
        sms.mouseReleased();
    }

 static public void main(String[] passedArgs) {
     //    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--stop-color=#cccccc", "com.beartronics.jschema.JSchema" };
     String[] appletArgs = new String[] { "--bgcolor=#666666", "--stop-color=#cccccc", "com.beartronics.jschema.JSchema" };

    if (passedArgs != null) {
        PApplet.main(concat(appletArgs, passedArgs));
    } else {
        PApplet.main(appletArgs);
    }
  }

}
