package com.beartronics.jschema;

import processing.core.PApplet;

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


public class JSchema extends PApplet {

    public static JSchema app;
    public SensoriMotorSystem sms;
    public Stage stage;

    public JSchema() {
        JSchema.app = this;
    }
    PBox2D box2d = null;

    PBox2D createBox2D() {
        box2d = new PBox2D(this);
        return box2d;
    }

    public void setup() {
        size(1024, 300);
        smooth();
        background(255);

        sms = new SensoriMotorSystem(this);
        stage = new Stage(sms);
        stage.initWorld(10, 10);

        sms.setupDisplay();

    }

    public int clock = 0;
    public void draw() {
        sms.draw();
        clock++;
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
