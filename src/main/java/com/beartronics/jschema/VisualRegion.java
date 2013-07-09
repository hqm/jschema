package com.beartronics.jschema;
import pbox2d.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import java.util.*;

import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.shapes.Shape;

import processing.core.*;


public class VisualRegion {
    JSchema app;
    SensoriMotorSystem sms;
    VisualCell cells[][];
    int nx, ny;
    int cellsize;
        
    VisualRegion(SensoriMotorSystem sms) {
        this.sms = sms;
        this.app = sms.app;
    }


    void init(int nx, int ny, int cellsize) {
        this.nx = nx;
        this.ny = ny;
        this.cellsize = cellsize;
        for (int x = 0; x < nx; x++) {
            for (int y = 0; y < ny; y++) {
                cells[x][y] = new VisualCell(x,y,cellsize);
            }
        }
    }

    // The visual gaze is centered at xpos, ypos in absolute pixel coordinates
    void updateCells(ArrayList<Object2D> objs, int gazex, int gazey) {
        // offset of left top of visual field from center
        int xoff = gazex - ((nx/2) * cellsize);
        int yoff = gazey - ((ny/2) * cellsize);

        for (Object2D obj: objs) {


        }

    }

    void display() {
        app.pushMatrix();
        app.pushStyle();
        app.rectMode(PConstants.CENTER);
        app.translate(20,20);
        for (int x = 0; x < nx; x++) {
            for (int y = 0; y < ny; y++) {
                boolean val = ((cells[x][y]).items.size() > 0);
                app.fill(val ? 0 : 255);
                app.rect(x*12, y*12, 10,10);
            }
        }

        app.popStyle();
        app.popMatrix();

    }


}

class VisualCell {

    ArrayList<Object2D> items;
    int cx, cy;
    int size;


    VisualCell(int cx, int cy, int size) {
        this.cx = cx;
        this.cy = cy;
        this.size = size;
    }
    

    /**
        // TODO
        // We need to scan all objects in plane1, plane0
        //  [1] compute which quadrants they cover any part of.
        //  [2]  compute which quadrant their center of mass lies in.
        // Also adjust these so that if an opaque box or circle in plane1 completely hides an object in plane0, don't
        // set the visual input for the plane0 item.


          Make a 10x10 visual map array of cells.

          for each physobj in plane 1 and plane 0, put it into plane0,plane1 lists of all cells it touches.

          for each cell, compute which plane1 objects are visible
             TODO::: Need to handle containment: calculate for each plane0 obj if it is completely covered by plane1 opaque objects.


        
     For peripheral vision, we take UNION of the two planes; you just detect an object at XY in peripheral vision.
     You need foveated gaze at an item to get depth perception.
    
     compute, for each physobj in each plane, what quadrant they are in. If more than some percentage overlap,
           set boolean to true.

           We want a datastructure to hold the list of objects which overlap each quadrant. Then can compute partial or total
           overlap from plane1 over plane0, taking into account transparency. 


     */
 


}
