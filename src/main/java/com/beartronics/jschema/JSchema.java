package com.beartronics.jschema;

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

void setup() {
    sms = new SensoriMotorSystem(this);
    stage = new Stage(sms);
    stage.initWorld(10, 10);

    size(1280, 640);
    smooth();
    background(255);
}

void draw() {
}

}
