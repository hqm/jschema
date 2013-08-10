package com.beartronics.jschema;

import processing.core.PApplet;

import com.shigeodayo.pframe.*;

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

import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

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




public class JSchema extends PApplet {

    static Logger logger = Logger.getLogger(JSchema.class.getName());

    public static JSchema app;
    public SensoriMotorSystem sms;
    public Stage stage;

    public boolean interactive = true;

    public JSchema() {
        JSchema.app = this;
        logger.info("JSchema app created.");
    }
    PBox2D box2d = null;

    PBox2D createBox2D() {
        box2d = new PBox2D(this);
        return box2d;
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

        sms = new SensoriMotorSystem(this, retinaImage);
        sms.setupDisplay();
        sms.computeWorldState();
        stage = new Stage(sms);
        stage.initWorld();


        frameRate(1024);

        startWebServer();
    }

    public void draw() {
        if (interactive) {
            try {
                // The SensoriMotorSystem will draw a global view for debugging, and also will render an image from
                // the head viewpoint into the retinaImage view.
                if (sms.run || sms.singleStep) {

                    sms.draw();
                    retinaView.set(0,0,retinaImage);

                    // The Schema engine will read the worldState from the sms, and
                    // set any motor actions it wants to
                    stage.processWorldStep(sms);
                    stage.clockStep();

                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("JSchema top-level caught exception "+e);
            }
        }
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
