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


    SensoriMotorSystem sms;
    Stage stage;

    PBox2D box2d = null;

    PBox2D createBox2D() {
        box2d = new PBox2D(this);
        return box2d;
    }

    public void setup() {
        sms = new SensoriMotorSystem(this);
        stage = new Stage(sms);
        stage.initWorld(10, 10);

        size(1280, 640);
        smooth();
        background(255);
    }

    public void draw() {
    }


 static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--stop-color=#cccccc", "com.beartronics.jschema.JSchema" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }

}
