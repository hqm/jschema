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
    PGraphics retina;
    VisualCell cells[][];
    /** Number of cells in x and y dimension */
    int nx, ny;
    /** cell size in pixels */
    int cellsize;
        
    VisualRegion(SensoriMotorSystem sms) {
        this.sms = sms;
        this.app = sms.app;
        this.retina = sms.retina;
    }


    void init(int nx, int ny, int cellsize) {
        cells = new VisualCell[nx][ny];
        this.nx = nx;
        this.ny = ny;
        this.cellsize = cellsize;
        for (int x = 0; x < nx; x++) {
            for (int y = 0; y < ny; y++) {
                cells[x][y] = new VisualCell(this, x,y,cellsize);
            }
        }
    }

    // The visual gaze is centered at xpos, ypos in absolute pixel coordinates


    void updateCells(ArrayList<Object2D> objs, int gazex, int gazey) {
        // offset of left top of visual field from center
        int xoff = gazex - ((nx/2) * cellsize);
        int yoff = gazey - ((ny/2) * cellsize);

        // Find motion vectors, compute x-y offset, update motion sensor in corresponding cells
        for (Object2D obj: objs) {
            

        }

    }

    void clearMotionSensors() {
        for (int x = 0; x < nx; x++) {
            for (int y = 0; y < ny; y++) {
                VisualCell cell = cells[x][y];
                cell.motion = new Vec2(0,0);
                cell.motionSensed = false;
            }
        }
    }


    // Debugging view; draws small map of visual field
    void display() {
        app.pushMatrix();
        app.pushStyle();
        app.rectMode(PConstants.CENTER);
        app.translate(20,20);
        for (int x = 0; x < nx; x++) {
            for (int y = 0; y < ny; y++) {
                VisualCell cell = cells[x][y];
                boolean val = cell.peripheralObjectSensed;
                int bg = val ? 0 : 255;
                app.fill(bg);
                app.rect(x*12, y*12, 10,10);

                boolean mval = cell.motionSensed;
                app.noStroke();
                if (mval) {
                    app.fill(255,0,0);
                } else {
                    app.fill(bg);
                }
                app.rect(x*12, y*12, 5,5);
                app.stroke(0);
            }
        }

        app.popStyle();
        app.popMatrix();

    }


    /**
       returns true if more than N pixels are 'on' in this quadrant
     */
    static final int SMALL_OBJECT_PIXEL_COUNT_THRESHOLD = 10;
    static final int MEDIUM_OBJECT_PIXEL_COUNT_THRESHOLD = 100;
    static final int LARGE_OBJECT_PIXEL_COUNT_THRESHOLD = 5000;
    static final int ON_BRIGHTNESS_THRESHOLD = 10;
    boolean peripheralObjectAtQuadrant(int qx, int qy) {
        int width = retina.width;
        int onpixels = 0;
        for (int x = 0; x < cellsize; x++) {
            for (int y = 0; y < cellsize; y++) {
                //int pixel = retina.pixels[y * width + x];
                int pixel = retina.get((qx * cellsize) + x, (qy * cellsize) + y);
                if (app.brightness(pixel) < (255 - ON_BRIGHTNESS_THRESHOLD)) {
                    onpixels ++;
                    if (onpixels > MEDIUM_OBJECT_PIXEL_COUNT_THRESHOLD) {
                        (cells[qx][qy]).peripheralObjectSensed = true;
                        return true;
                    }
                }
            }
        }
            //return onpixels > MEDIUM_OBJECT_PIXEL_COUNT_THRESHOLD;
        (cells[qx][qy]).peripheralObjectSensed = false;
        return false;
    }


    /** converts retina x,y coordinate to global pixel coordinates

        computes body offset (sms.xpos) + gaze offset
     */
    Vec2 toGlobalPixelPos(float x, float y) {
        float gazedx = sms.gazeXpos;
        float bodydx = sms.xpos - (app.width /2);
        float xpos = x + bodydx;

        float gazedy = sms.gazeYpos;
        float bodydy = sms.ypos - (app.height /2);
        float ypos = y + bodydy;

        return new Vec2(xpos, ypos); 
    }

    /**
     * returns a vector of x,y motion

     This is a cheesy motion sensor system. Instead of trying to
     compute motion fields from two successive bitmap images, we're
     going to loop over all physobs, and take their velocity vector,
     and set the vertical and horizontal motion sensor on each visual
     cell for which the bounding box overlaps.


     */
    VisualCell setMotionAtQuadrant(int qx, int qy, Vec2 v) {
        if ((qx < 0) || (qy < 0) || (qx >= nx) || (qy >= ny)) { return null; }
        VisualCell cell = (cells[qx][qy]);
        cell.motion = v;
        cell.motionSensed = true;
        return cell;
    }


    VisualCell getCell(int x, int y) {
        return cells[x][y];
    }

    boolean isObjectAtGaze(Vec2 pos) {
        Object2D obj = sms.findObjAt(pos);
        return (obj != null);
    }


    boolean isSolidObjectAtGaze(Vec2 pos) {
        Object2D obj = sms.findObjAt(pos);
        if (obj == null) {
            return false;
        } else {
            return obj.isSolid();
        }
    }

    boolean isHollowObjectAtGaze(Vec2 pos) {
        Object2D obj = sms.findObjAt(pos);
        if (obj == null) {
            return false;
        } else {
            return obj.isHollow();
        }
    }

    boolean isRoundObjectAtGaze(Vec2 pos) {
        Object2D obj = sms.findObjAt(pos);
        if (obj == null) {
            return false;
        } else {
            return (obj instanceof Ball);
        }
    }

    boolean isFlatObjectAtGaze(Vec2 pos) {
        Object2D obj = sms.findObjAt(pos);
        if (obj == null) {
            return false;
        } else {
            return (obj instanceof Box);
        }
    }

    /** is object at orientation angle between lower and upper?*/
    boolean isGazeObjectAtAngle(Vec2 pos, int lower, int upper) {
        Object2D obj = sms.findObjAt(pos);
        if (obj == null) {
            return false;
        } else {
            float angle = obj.getAngle();
            return (lower <= angle && angle < upper);
        }
    }


    boolean isRedObjectAtGaze(Vec2 pos) {
        Object2D obj = sms.findObjAt(pos);
        if (obj == null) {
            return false;
        } else {
            float red = app.red(obj.color);
            float green = app.green(obj.color);
            float blue = app.blue(obj.color);
            return (red > 200 && green < 50 && blue < 50);
        }
    }
    boolean isBlueObjectAtGaze(Vec2 pos) {
        Object2D obj = sms.findObjAt(pos);
        if (obj == null) {
            return false;
        } else {
            float red = app.red(obj.color);
            float green = app.green(obj.color);
            float blue = app.blue(obj.color);
            return (red < 50 && green < 50 && blue > 200);
        }
    }
    boolean isGreenObjectAtGaze(Vec2 pos){
        Object2D obj = sms.findObjAt(pos);
        if (obj == null) {
            return false;
        } else {
            float red = app.red(obj.color);
            float green = app.green(obj.color);
            float blue = app.blue(obj.color);
            return (red < 50 && green > 200 && blue < 50);
        }
    }

    boolean isDarkObjectAtGaze(Vec2 pos) {
        Object2D obj = sms.findObjAt(pos);
        if (obj == null) {
            return false;
        } else {
            float bright = app.brightness(obj.color);
            return (bright > 128);
        }
    }

    boolean isLightObjectAtGaze(Vec2 pos) {
        Object2D obj = sms.findObjAt(pos);
        if (obj == null) {
            return false;
        } else {
            float bright = app.brightness(obj.color);
            return (bright <= 128);
        }
    }


}

class VisualCell {

    VisualRegion vr;
    ArrayList<Object2D> items;
    int cx, cy;
    int size;
    boolean peripheralObjectSensed = false;
    boolean motionSensed = false;
    // The linear sum of all motion vectors of objects that lie in this cell
    Vec2 motion = new Vec2();
    
    // flags to say whether these features were detected in this cell
    boolean small_obj;
    boolean medium_obj;
    boolean large_obj;

    VisualCell(VisualRegion vr, int cx, int cy, int size) {
        this.vr = vr;
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
