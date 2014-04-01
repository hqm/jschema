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
    public Stage stage;

    public boolean interactive = true;
    
    public GsonBuilder gsonBuilder;
    public Gson gson;

    Config config;

    public JSchema() {
        JSchema.app = this;
        logger.info("JSchema app created.");
        // load config file
        config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference(), "application");
        logger.info(config.root().render());

        // Setup for JSON serializer
        gsonBuilder = new GsonBuilder();
        new GraphAdapterBuilder()
            .addType(Stage.class)
            .registerOn(gsonBuilder);
        gson  = gsonBuilder.create();
    }

    PBox2D box2d = null;

    PBox2D createBox2D() {
        box2d = new PBox2D(this);
        return box2d;
    }

    // writes the Stage out
    public void serialize(String fname, Stage stg) {
        try {
            OutputStream ostream = new FileOutputStream(fname);
            try {

                if (fname.endsWith(".gz")) {
                    ostream = new GZIPOutputStream(ostream);
                }

                Writer writer = new OutputStreamWriter(ostream,  "UTF-8");
                gson.toJson(stg, writer);
                writer.close();
            } catch (Exception e) {
                logger.error("could not write serialization for Stage", e);
            } finally {
                ostream.close();
            }
        } catch (Exception e) {
            logger.error("could not write serialization for Stage", e);
        }
    }

    public void saveStage(String fname) {
        serialize(fname, stage);
    }

    public void saveStage(boolean compress) {
        Date now = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-M-d-hh-mm");
        String fname = "jschema-stage-"+dateFormatter.format(now)+".json";
        if (compress) {
            fname += ".gz";
        }

        serialize(fname, stage);
    }

    // reads a Stage in
    public Stage deserialize(String filename) {
        Stage s = null;
        try {
            InputStream istream = new FileInputStream(filename);
            // Is it a compressed file?
            if (filename.endsWith(".gz")) {
                istream = new GZIPInputStream(istream);
            }

            InputStreamReader reader = new InputStreamReader(istream, "UTF-8");

            s = gson.fromJson(reader, Stage.class);
            this.stage = s;
        } catch (Exception e) {
            logger.error("could not read serialization for Stage from "+filename, e);
        }
        return s;
    }

    public void loadStage(String fname) {
        Stage newstage = deserialize(fname);
        newstage.config = config;
        sms.setupDisplay(); // make new WorldState
        newstage.sms = sms;
        newstage.app = this;

        sms.stage = newstage;
        this.stage = newstage;
        this.stage.worldState = sms.worldState;

        sms.computeWorldState();

    }


    public void startWebServer() {
        try {
            println("starting web server on port 8080");
            Container container = new WebServer(this);
            Server server = new ContainerServer(container);
            Connection connection = new SocketConnection(server);
            SocketAddress address = new InetSocketAddress(8080);

            connection.connect(address);
        } catch (Exception e) {
            println("web server caught exception "+e);
        }
    }

    PFrame retinaFrame = null;
    RetinaView retinaView = null;
    PGraphics retinaImage = null;
    
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
        stage = new Stage(sms, config);
        sms.setupDisplay();
        sms.computeWorldState();
        stage.initWorld();
        frameRate(1024);

        startWebServer();
    }

    public void draw() {
        if (interactive) {
            try {
                // The SensoriMotorSystem will draw a global view for debugging, and also will render an image from
                // the head viewpoint into the retinaImage view.
                if (sms.run || sms.singleStep || sms.multiStep) {
                    // The Schema engine will read the worldState from the sms, and
                    // set any motor actions it wants to
                    stage.processWorldStep();
                    retinaView.set(0,0,retinaImage);
                    stage.clockStep();
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("JSchema top-level caught exception "+e);
            }
        }
    }

    public void keyPressed() {
        // disengage the clutch
        if (key == 'p') {
            stage.run = !stage.run;
        }

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

     /* Test json serializer/deserializer */
     A a = new A();
     B b = new B(a);
     a.b = b;
     
     GsonBuilder gsonBuilder = new GsonBuilder();
     new GraphAdapterBuilder()
         .addType(A.class)
         .registerOn(gsonBuilder);
     Gson gson = gsonBuilder.create();
     System.out.println(gson.toJson(a));


     StringReader reader = new StringReader(gson.toJson(a));

     A thawed = gson.fromJson(reader, A.class);
     System.out.println(thawed);

     /* */

    if (passedArgs != null) {
        PApplet.main(concat(appletArgs, passedArgs));
    } else {
        PApplet.main(appletArgs);
    }
  }

}
