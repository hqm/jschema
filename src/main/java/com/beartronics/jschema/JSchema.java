package com.beartronics.jschema;

import processing.core.PApplet;
import com.google.gson.GsonBuilder;


import com.shigeodayo.pframe.*;

import com.google.gson.*;

import controlP5.*;
import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream; 

import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;


import com.typesafe.config.*;

// serialization lib
import java.io.*;

public class JSchema extends PApplet {

    static Logger logger = Logger.getLogger(JSchema.class.getName());
    static final String VERSION = "0.1";

    public static JSchema app;
    public SensoriMotorSystem sms;

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
    
    public void step() {
        //sms.processActions();
        sms.stepPhysicalWorld();
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

        WorldState w = new WorldState();
        sms = new SensoriMotorSystem(this, w, retinaImage);
        sms.setupDisplay();
        sms.computeWorldState();
        frameRate(1024);

    }

    public void draw() {
        sms.stepPhysicalWorld();
        retinaView.set(0,0,retinaImage);
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
