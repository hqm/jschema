package com.beartronics.jschema;
// Basde on code examples from The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2011
// PBox2D example

// Basic example of falling rectangles

import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import java.util.*;

import org.jbox2d.collision.AABB;

import processing.core.*;

import org.apache.log4j.Logger;
public class Box2DSensoriMotorSystem extends BaseSensoriMotorSystem {
    static Logger logger = Logger.getLogger(Box2DSensoriMotorSystem.class.getName());

    // Set holding current sensor states
    SensorState sensors = new SensorState();
    List<String> completedActions = new ArrayList<>();

    // Object planes
    public Plane plane0; // back plane
    public Plane plane1; // front plane

    int WORLD_WIDTH = 4096;
    int WORLD_HEIGHT = 800;

    // Position of body in world
    public float xpos;
    public float ypos;

    float bodyWidth = 600;
    float bodyHeight = 400;

    // horizontal scroll speed for debugging
    float scrollspeed = 25.0f;


    // Map from color to object. Each physobj has a unique color, to
    // give some assistance to the visual system in tagging objects for
    // gaze control.
    public HashMap<Integer,Object2D> objColorMap = new HashMap<Integer,Object2D>();
    public int objCounter = 0;

    void addObjectColorMapping(Object2D obj, int color) {
        objColorMap.put(color, obj);
    }

    public int getNextObjectColor() {
        if (objCounter > 255) {
            throw new RuntimeException("cannot have more than 255 objects");
        } else {
            int c = ObjectColors.objcolors[objCounter++];
            return c;
        }
    }

    public Object2D findObjByColor(int color) {
        return objColorMap.get(color);
    }

    public Object2D testfind() {
        return findObjByColor(-14277082);
    }

    ////////////////////////////////////////////////////////////////
    // Head and Eyes Controls

    // Relative offset from body xpos,ypos.
    // Computed from head gross and fine angles
    public float gazeXpos = 0;
    public float gazeYpos = 0;
    
    public Hand hand1;
    public Hand hand2;

    // max number of gross and fine motor steps that arms can take
    public int reachX = 1;
    public int reachY = 1;
    // arm motor step size
    public int dGross = 100;
    public int dFine = 20;

    // Used to sense objects in the visual field
    VisualRegion vision;

    boolean showWorldState = false;

    Object2D foveatedObject = null;
    // An image context to render the retina view into
    PGraphics retina = null;

    public Box2DSensoriMotorSystem(JSchema a, PGraphics retina) {
        super(a, retina);
        this.retina = retina;
        System.out.println("Box2DSensoriMotorSystem constructor this.app = "+this.app);
    }

    Object2D findObj(int index) {
        for (Plane plane: planes) {
            Object2D o = plane.findObj(index);
            if (o != null) { return o; }
        }
        return null;
    }

    Object2D findObjAt(Vec2 pos) {
        return findObjAt(pos.x, pos.y);
    }

    ArrayList<Object2D> findObjectsAt(Vec2 pos) {
        ArrayList<Object2D> items = new ArrayList<Object2D>();
        for (Plane plane: planes) {
            ArrayList<Object2D> objs = plane.findObjectsAt(pos.x, pos.y);
            items.addAll(objs);
        }
        return items;
    }


    Object2D findObjAt(float x, float y) {
        for (Plane plane: planes) {
            Object2D o = plane.findObjAt(x, y);
            if (o != null) { return o; }
        }
        return null;
    }


    PFont font;
    ArrayList<Plane> planes = new ArrayList<Plane>();
    Plane currentPlane;

    int marker_color;

    static final int RETINA_SIZE = 1000;

    void setupDisplay() {
        // Initial body position
        xpos = app.width/2;
        ypos = app.height/2+100;

        // Initialize box2d physics and create the world
        plane0 = new Plane(app, app.color(255, 55, 55));
        plane1 = new Plane(app, app.color(0,0,0));

        marker_color = app.color(189,22,198,128);
        // Initialize box2d physics and create the world
        planes.add(plane0);
        planes.add(plane1);

        currentPlane = plane1;
        
        for (Plane plane: planes) {
            plane.setup();
        }

        vision = new VisualRegion(this);
        vision.init(NQUADRANT_X, NQUADRANT_Y, QUADRANT_SIZE);

        app.smooth();

        font = app.createFont("Monospaced", 12);
        app.textFont(font);

        initialBoundaries(plane0);
        initialBoundaries(plane1);

        makeNavigationMarkers(plane0);

        initialPhysobjs(plane1);

        // hands start out in plane1
        hand1 = plane1.addHand( xpos, ypos, 32, 32, 5, app.color(0,255,0));
        hand2 = plane1.addHand( xpos, ypos, 32, 32, 5, app.color(255,0,0));
        hand1.alpha = 255;
        hand2.alpha = 255;
        hand1.hjog(-2,0);
        hand1.vjog(3,0);
        hand2.hjog(1,0);

        hand1.updatePosition(xpos,ypos);
        hand2.updatePosition(xpos,ypos);


        /*        plane0.addBox(800, 100, 100, 100, 4, app.color(255,40,30));
                  plane0.addBox(1000, 100, 50, 50, 8, app.color(87,191,22));
        */

        // move body into initial position
        moveBody(xpos,ypos);

    }

    void setTranslations(float x, float y) {
        for (Plane plane: planes) {
            plane.setTranslation(x,y);
        }
    }


    void initialBoundaries(Plane p) {
        // Add a bunch of fixed boundaries
        // floor
        p.addBoundary(WORLD_WIDTH/2, app.height+30, WORLD_WIDTH, 100f );
        // left wall
        p.addBoundary(-40,           app.height-200, 50f, 2000f );
        // right wall
        p.addBoundary(WORLD_WIDTH+40, app.height-200, 50f, 2000f );


        p.addBoundary(100, app.height-50, 10f, 100f );
        p.addBoundary(200, app.height-150, 10f, 300f );
        p.addBoundary(1000, app.height-150, 10f, 300f );
        p.addBoundary(1200, app.height-100, 10f, 200f );
    }

    void makeNavigationMarkers(Plane p) {
        // add markers to help locate position

        for (int x = 200; x < WORLD_WIDTH; x+=200) {
            p.addBoundary(x, app.height-400, 20+x/100f, 20+x/50f, marker_color );
        }
    }



    void initialPhysobjs(Plane p) {
        int bottom = app.height;
        p.addBox(100, bottom -10, 64, 64, 1);
        p.addBox(100, bottom -10, 64, 64, 1);
        p.addBox(100, bottom -10, 64, 64, 1);
        p.addBox(100, bottom -10, 64, 64, 1);
        /*
        p.addBox(500, bottom -10, 64, 64, 1);
        p.addBox(500, bottom -10, 64, 64, 1);
        */
        /*        p.addBox(500, bottom-10, 64, 64, 2);
        p.addBox(800, bottom-10, 32, 32, 2);
        p.addBox(1200, bottom-10, 64, 64, 2);

        p.addBox(1500, bottom-10, 64, 64, 1);
        p.addBox(2000, bottom-10, 64, 64, 10);

        p.addBox(300, bottom-200, 200, 5, 6);
        */
        /*
        p.addBox(300, bottom-200, 400, 5, 6);
        p.addBox(280, bottom-200, 20, 10, 8);
        p.addBox(260, bottom-200, 20, 20, 8);
        p.addBox(240, bottom-200, 30, 20, 8);

        */

        /*
          p.addBall(200, bottom-100, 40);
          p.addBall(260, bottom-100, 40);
        */

        /*
          p.addBall(250, bottom-100, 40);
        p.addBall(500, bottom-100, 30);
        p.addCustomShape1(50,bottom-100,app.color(123,201,122));
        */

    }

    String showHandInfo(Hand h) {
        StringBuilder touchList = new StringBuilder();
        for (Object2D obj: h.touchingObjects()) {
            touchList.append(" "+obj.index);
        }

        StringBuilder graspList = new StringBuilder();
        for (Object2D obj: h.getWeldedObjects()) {
            graspList.append(" "+obj.index);
        }


        Vec2 jf = h.getJointForce();

        float torque = h.getJointTorque();

        Vec2 pos = h.getPosition();

        Vec2 actualGPos = h.actualGrossPosition();
        Vec2 actualFPos = h.actualFinePosition();
        String info = String.format("sensedGross: (X: %.1f, Y: %.1f)  gross: (X: %.1f, Y: %.1f) fine: (X: %.1f, Y: %.1f) FJoint:x=%.1f y=%.1f TRQ=%.1f %s [touching %s] [grasp %s]",
                                    actualGPos.x, actualGPos.y,
                                    h.grossX, h.grossY,
                                    h.fineX, h.fineY,
                                    jf.x*100, jf.y*100,
                                    torque,
                                    h.touchString(), touchList, graspList);
        return info;
    }


    // Do one timestep of simulation of the world
    void stepPhysicalWorld(List<String> actions) {
        processActions(actions);

        app.rectMode(PConstants.CORNER);
        if (planes.indexOf(currentPlane) == 0) {
            app.background(255,225,225);
        } else {
            app.background(255);
        }

        for (Plane plane: planes) {
            plane.step();
        }

        // draw viewport and gaze location
        drawViewPort();

        displayRetinaView();

        computeWorldState();

        // Display info text 
        displayInfoText();

        if (showWorldState) {
            displayWorldState(sensors);
        }

    }

    String lastActionString = "";


    void displayInfoText() {
        app.fill(0);
        app.text("alt-click to create box, click to grasp, ctrl-click to lift, L and R key to rotate grip, shift for transparent, space/enter toggle single-step", 20,12);
        app.text("clock = "+clock + " plane="+planes.indexOf(currentPlane), 20,22);
        app.text("xpos="+xpos+ "   ypos="+ypos,20,32);
        app.text("hand1 "+showHandInfo(hand1),20,42);
        app.text("hand2 "+showHandInfo(hand2),20,52);
        app.text("gazeX="+gazeXpos+" gazeY="+gazeYpos, 20, 62);

        String desc = sensors.actions.toString();
        if (!desc.equals("[]")) {
            lastActionString = desc;
        }
        app.text(String.format("current actions: %s", sensors.actions), 20, 82);
        app.text(String.format("last actions: %s", lastActionString), 20, 92);
    }

    void displayRetinaView() {
        retina.beginDraw();
        retina.background(255);
        for (Plane plane: planes) {
            
            plane.drawRetina(retina);
        }
        retina.endDraw();
    }

    void displayWorldState(SensorState w) {

        app.pushMatrix();
        app.pushStyle();
        app.rectMode(PConstants.CENTER);
        app.translate(app.width-250,20);

        for (Map.Entry<String, SensorInput> entry : w.items.entrySet())
        {
            SensorInput s = entry.getValue();
            int id = s.id;
            boolean v = s.value;

            float cx = (id % 20) * 12;
            float cy = (id / 20) * 12;

            if (v) {
                app.fill(0);
            } else {
                app.fill(225);
            }

            app.rect(cx, cy, 10,10);
        }

        // draw the max range the hands can move

        app.popStyle();
        app.popMatrix();
        vision.display();

    }

    // screen is always drawn such that body is located at horizontal center
    void drawViewPort() {
        // draw where the gaze is centered
        float dx, dy;
        dx = gazeXpos;
        dy = gazeYpos;
        float cx = app.width/2;
        float cy = ypos;

        app.pushMatrix();
        app.pushStyle();
        app.rectMode(PConstants.CENTER);


        // draw yellow circle at body (xpos,ypos)
        app.strokeWeight(3);
        app.stroke(177,177,102);
        app.noFill();
        app.rect(cx, ypos, 40, 40);
        app.ellipse(cx,ypos,20,20);
        app.strokeWeight(1);
        // draw X at gaze position
        app.stroke(app.color(128,128,128,200));
        app.line(cx + dx - 50, cy+dy-50,
                 cx + dx + 50, cy+dy+50);
        app.line(cx + dx + 50, cy+dy-50,
                 cx + dx - 50, cy+dy+50);
        

        // draw the max range the hands can move
        app.rect(cx, ypos, 2*reachX*dGross + 2*reachX*dFine, 2*reachY*dGross+ 2*reachY*dFine);
        app.popStyle();
        app.popMatrix();


        


    }


    int downKeys[] = new int[1024];

    public Plane nextPlane() {
        int idx = planes.indexOf(currentPlane);
        idx = (idx+1) % planes.size();
        return planes.get(idx);
    }

    public Plane prevPlane() {
        int idx = planes.indexOf(currentPlane);
        idx = (idx-1);
        if (idx < 0) {
            idx = planes.size()-1;
        }
        return planes.get(idx);
    }

    final int MAX_HAND_FORCE = 100;

    /** Moves the body position.
     * checks the hand forces, and if too large, will not move the body, thus preventing
     * moving too far from the hands
     */
    public void jogBody(float dx, float dy) {
        Vec2 f1 = hand1.getJointForce();
        Vec2 f2 = hand2.getJointForce();
        if (dx < 0) {
            if ((f1.x > -MAX_HAND_FORCE) && (f2.x > -MAX_HAND_FORCE)) {
                xpos += dx;
            } else {
                app.println(String.format("HAND X FORCE h1x=%f h2x=%f TOO LARGE, CANNOT MOVE BODY dx=%f", f1.x, f2.x, dx));
            }
        }

        if (dx > 0) {
            app.println(String.format("** dx=%f f1.x=%f f2.x=%f", dx, f1.x, f2.x));
            if ((f1.x < MAX_HAND_FORCE) && (f2.x < MAX_HAND_FORCE)) {
                xpos += dx;
            } else {
                app.println(String.format("HAND X FORCE h1x=%f h2x=%f TOO LARGE, CANNOT MOVE BODY dx=%f", f1.x, f2.x, dx));
            }
        }


        if (dy < 0) {
            if ((f1.y > -MAX_HAND_FORCE) && (f2.y > -MAX_HAND_FORCE)) {
                ypos += dy;
            } else {
                app.println(String.format("HAND Y FORCE h1y=%f h2y=%f TOO LARGE, CANNOT MOVE BODY dy=%f", f1.y, f2.y, dy));
            }
        }

        if (dy > 0) {
            if ((f1.y < MAX_HAND_FORCE) && (f2.y < MAX_HAND_FORCE)) {
                ypos += dy;
            } else {
                app.println(String.format("HAND Y FORCE h1y=%f h2y=%f TOO LARGE, CANNOT MOVE BODY dy=%f", f1.y, f2.y, dy));
            }
        }

        moveBody(xpos, ypos);
    }

    void moveBody(float x, float y) {
        xpos = x;
        xpos = (float)Math.max(0, xpos);
        xpos = (float)Math.min(WORLD_WIDTH,xpos);

        ypos = y;
        ypos = (float)Math.min(WORLD_HEIGHT,ypos);
        ypos = (float)Math.max(0,ypos);

        plane0.updateHeadPosition(xpos,ypos);
        plane1.updateHeadPosition(xpos,ypos);

        hand1.updatePosition(xpos,ypos);
        hand2.updatePosition(xpos,ypos);

        setTranslations(xpos,ypos);
    }

    boolean run = false;
    boolean singleStep = true;
    // Step N clock cycles, where N is the marginal attribution action-time period
    boolean multiStep = true;

    public void keyPressed() {
        downKeys[app.keyCode] = 1;
        if (app.keyCode == PConstants.LEFT) {
            jogBody(-scrollspeed,0);
        } else if (app.keyCode == PConstants.RIGHT) {
            jogBody(scrollspeed,0);
        } else if (app.keyCode == PConstants.UP || app.keyCode == PConstants.DOWN) {
            // If we're interactively grasping an object with the mouse, move it to next plane
            Plane next = app.keyCode == PConstants.UP ? nextPlane() : prevPlane();
            if (currentPlane.pickedThing != null) {
                Object2D obj = currentPlane.pickedThing;
                currentPlane.mouseDropObject();
                obj.moveToPlane(next);
                next.mouseGraspObject(obj);
            }
            currentPlane = next;
        }
        currentPlane.keyPressed();
        if (app.keyCode == PConstants.SHIFT) {
            for (Plane plane: planes) {
                plane.setTransparent(true);
            }
        }

        if (app.key == 'w') {
            showWorldState = !showWorldState;
        }

        if (app.key == 'e') {
            gazeUp(GAZE_INCR);
        } else if (app.key == 'c') {
            gazeDown(GAZE_INCR);
        } else if (app.key == 's') {
            gazeLeft(GAZE_INCR);
        } else if (app.key == 'f') {
            gazeRight(GAZE_INCR);
        } else if (app.key == ' ') { // SPACE char means single step n clock steps ( one action time )
            run = false;
            multiStep = true;
        } else if (app.key == '1') { // step a single clock
            run = false;
            singleStep = true;
        } else if (app.keyCode == PConstants.ENTER) {
            run = true;
            multiStep = false;
        }
    }
        
    public void keyReleased() {
        downKeys[app.keyCode] = 0;
        currentPlane.keyReleased();
        if (app.keyCode == PConstants.SHIFT) {
            for (Plane plane: planes) {
                plane.setTransparent(false);
            }
        }

    }
    
    boolean isKeyDown(int k) {
        return downKeys[k] == 1;
    }

    public void mouseReleased() {
        currentPlane.mouseReleased();
    }

    public void mousePressed() {
        currentPlane.mousePressed();
    }


    /** Reads primitive actions from worldstate and performs them.
     */
    public void processActions(List<String> actions) {
        //HashMap<String,Action> outputList
        for (String aname : actions) {
            Action.Type action = Action.Type.valueOf(aname);
            // CODE HERE To execute actions
            switch (action.type) {
              case CENTER_GAZE:
                // Set the gaze to the body/head center position
                gazeAt(new Vec2(xpos,ypos));
                break;
              case MOVE_LEFT:
                break;
              case MOVE_RIGHT:
                break;
              case MOVE_UP:
                break;
              case MOVE_DOWN:
                break;
              case GAZE_LEFT:
                gazeLeft(GAZE_INCR);
                break;
              case GAZE_RIGHT:
                gazeRight(GAZE_INCR);
                break;
              case GAZE_UP:
                gazeUp(GAZE_INCR);
                break;
              case GAZE_DOWN:
                gazeDown(GAZE_INCR);
                break;
              case FOVEATE_NEXT_MOTION:
                foveateNextMovingObject(gazeAbsPosition());
                break;
              case FOVEATE_NEXT_OBJECT_LEFT:
                foveateNextObjectLeft();
                break;
              case FOVEATE_NEXT_OBJECT_RIGHT:
                foveateNextObjectRight();
                break;
              case FOVEATE_NEXT_OBJECT_UP:
                foveateNextObjectUp();
                break;
              case FOVEATE_NEXT_OBJECT_DOWN:
                foveateNextObjectDown();
                break;
              case HAND2_LEFT:  // move right hand, gross motor
                hand2.hjog(-1,0);
                break;
              case HAND2_RIGHT:
                hand2.hjog(1,0);
                break;
              case HAND2_UP:
                hand2.vjog(-1, 0);
                break;
              case HAND2_DOWN:
                hand2.vjog(1,0);
                break;
              case HAND2_FINE_LEFT: // move right hand, fine motor
                hand2.hjog(0,-1);
                break;
              case HAND2_FINE_RIGHT:
                hand2.hjog(0,1);
                break;
              case HAND2_FINE_UP:
                hand2.vjog(0,-1);
                break;
              case HAND2_FINE_DOWN:
                hand2.vjog(0,1);
                break;

              case HAND1_LEFT:  // move left hand, gross motor
                hand1.hjog(-1,0);
                break;
              case HAND1_RIGHT:
                hand1.hjog(1,0);
                break;
              case HAND1_UP:
                hand1.vjog(-1, 0);
                break;
              case HAND1_DOWN:
                hand1.vjog(1,0);
                break;
              case HAND1_FINE_LEFT: // move left hand, fine motor
                hand1.hjog(0,-1);
                break;
              case HAND1_FINE_RIGHT:
                hand1.hjog(0,1);
                break;
              case HAND1_FINE_UP:
                hand1.vjog(0,-1);
                break;
              case HAND1_FINE_DOWN:
                hand1.vjog(0,1);
                break;




              case HAND1_GRASP:
                hand1.weldContacts();
                break;
              case HAND1_UNGRASP:
                hand1.removeWeldJoints();
                break;
              case HAND2_GRASP:
                hand2.weldContacts();
                break;
              case HAND2_UNGRASP:
                hand2.removeWeldJoints();
                break;
              case HAND1_WELD:
                hand1.weldGraspedObjects();
                break;
              case HAND2_WELD:
                hand2.weldGraspedObjects();
                break;
              case HAND1_UNWELD:
                hand1.unWeldGraspedObjects();
                break;
              case HAND2_UNWELD:
                hand2.unWeldGraspedObjects();
                break;
              case NULL_ACTION:
                break;
              default:
                app.println("unknown Action type "+action.type);
            }
        }
    }

    public int sensorID = 0;

    /// Fills in the sensory input values
    public void computeWorldState() {

        sensorID = 0;

        computeTouchSensors();

        computeVisionSensor();
        computeAudioSensors();


    }

    public void computeAudioSensors() {
    }
    
    public void computeVisionSensor() {
        // copies the bitmap into the pixels[] array for retina
        retina.loadPixels();
        // is fovea seeing a solid object?
        sensors.setSensorItem("vision.fovea.object", vision.isObjectAtGaze(gazeAbsPosition()),clock);
        sensors.setSensorItem("vision.fovea.solid_object", vision.isSolidObjectAtGaze(gazeAbsPosition()),clock);
        sensors.setSensorItem("vision.fovea.hollow_object", vision.isHollowObjectAtGaze(gazeAbsPosition()),clock);
        sensors.setSensorItem("vision.fovea.round_object", vision.isRoundObjectAtGaze(gazeAbsPosition()),clock);
        sensors.setSensorItem("vision.fovea.flat_object", vision.isFlatObjectAtGaze(gazeAbsPosition()),clock);

        sensors.setSensorItem("vision.fovea.red_object", vision.isRedObjectAtGaze(gazeAbsPosition()),clock);
        sensors.setSensorItem("vision.fovea.blue_object", vision.isBlueObjectAtGaze(gazeAbsPosition()),clock);
        sensors.setSensorItem("vision.fovea.green_object", vision.isGreenObjectAtGaze(gazeAbsPosition()),clock);

        sensors.setSensorItem("vision.fovea.dark_object", vision.isDarkObjectAtGaze(gazeAbsPosition()),clock);
        sensors.setSensorItem("vision.fovea.light_object", vision.isLightObjectAtGaze(gazeAbsPosition()),clock);

        for (int angle = 0 ; angle < 180; angle+= 10) {
            sensors.setSensorItem("vision.fovea.angle."+angle,
                                  vision.isGazeObjectAtAngle(gazeAbsPosition(), angle-10, angle),
                                  clock);
        }

        // Look for closed and open boundary objects
        //sensors.setSensorItem("vision.fovea.closed_object", closedObjectAt(0,0), clock);
        
        computeMotionSensors();
        for (int x = 0; x < NQUADRANT_X; x++) {
            for (int y = 0; y < NQUADRANT_Y; y++) {
                sensors.setSensorItem("vision.peripheral.obj."+x+"."+y, vision.peripheralObjectAtQuadrant(x,y), clock);

                VisualCell cell = vision.getCell(x,y);
                sensors.setSensorItem("vision.peripheral.motion."+x+"."+y, cell.motionSensed, clock);
                sensors.setSensorItem("vision.peripheral.pos_x_motion."+x+"."+y, cell.motion.x > 0, clock);
                sensors.setSensorItem("vision.peripheral.neg_x_motion."+x+"."+y, cell.motion.x < 0, clock);
                sensors.setSensorItem("vision.peripheral.pos_y_motion."+x+"."+y, cell.motion.y > 0, clock);
                sensors.setSensorItem("vision.peripheral.neg_y_motion."+x+"."+y, cell.motion.y < 0, clock);
            }
        }
    }

    static final double MOTION_THRESHOLD = 0.1;

    /** This attempts to compute all quadrants which are covered by an objects bounding box */
    public void computeMotionSensorsBBOX() {
        vision.clearMotionSensors();
        for (Plane plane: planes) {
            for (Object2D obj: plane.physobjs) {
                Fixture f = obj.body.getFixtureList();
                AABB bbox = f.getAABB(0);
                Vec2 lb = plane0.box2d.coordWorldToPixels(bbox.lowerBound);
                Vec2 ub = plane0.box2d.coordWorldToPixels(bbox.upperBound);

                Vec2 vmotion = obj.body.getLinearVelocity();

                // translate these coordinates to retina frame of reference
                Vec2 offset = new Vec2(xpos-app.width/2, ypos-app.width/2);
                offset.x -= gazeXpos;
                offset.y += gazeYpos;
                lb.subLocal(offset);
                ub.subLocal(offset);
                
                int x1 = (int) Math.floor(lb.x / QUADRANT_SIZE);
                int x2 = (int) Math.floor(ub.x / QUADRANT_SIZE);

                int y1 = (int) Math.floor(lb.y / QUADRANT_SIZE);
                int y2 = (int) Math.floor(ub.y / QUADRANT_SIZE);

                int xmin = Math.min(x1, x2);
                int xmax = Math.max(x1, x2);
                int ymin = Math.min(y1, y2);
                int ymax = Math.max(y1, y2);

                if (vmotion.lengthSquared() > MOTION_THRESHOLD) {
                    for (int x = xmin; x <= xmax; x++) {
                        for (int y = ymin; y <= ymax; y++) {
                            vision.setMotionAtQuadrant(x, y, vmotion);
                        }
                    }
                }

            }
        }
    }

    public void computeMotionSensors() {
        vision.clearMotionSensors();
        for (Plane plane: planes) {
            for (Object2D obj: plane.physobjs) {
                Vec2 pos = obj.getPosition();

                Vec2 vmotion = obj.body.getLinearVelocity();

                // translate these coordinates to retina frame of reference
                Vec2 offset = new Vec2(xpos-app.width/2, ypos-app.width/2);
                offset.x -= gazeXpos;
                offset.y += gazeYpos;
                pos.subLocal(offset);
                
                int x1 = (int) Math.floor(pos.x / QUADRANT_SIZE);
                int y1 = (int) Math.floor(pos.y / QUADRANT_SIZE);

                if (vmotion.lengthSquared() > MOTION_THRESHOLD) {
                    vision.setMotionAtQuadrant(x1, y1, vmotion);
                }
            }
        }
    }



    static final int QUADRANT_SIZE = 200;
    static final int NQUADRANT_X = 5; // 10x10 field
    static final int NQUADRANT_Y = 5; // 

    static final int GAZE_INCR = 50;
    static final int GAZE_MAX_XOFFSET = 650;
    static final int GAZE_MAX_YOFFSET = 350;

    public boolean objectAtQuadrant(float qx, float qy) {
        Object2D obj = findObjAt(xpos + qx*QUADRANT_SIZE, ypos+qy*QUADRANT_SIZE);
        return obj != null;
    }

    // Vision motor primitives
    /**
       send the gaze to center on this object
       @return dx,dy of gaze motion
     */
    public Vec2 gazeAt(Object2D thing) {
        return gazeAt(thing.getPosition());
    }

    /**
       This moves the gaze to abs position pos, and has side effect of
       setting the proprioceptive sense gazeMotion to tell how far the gaze moved.
    */
    public Vec2 gazeAt(Vec2 pos) {
        float oldgx = gazeXpos;
        float oldgy = gazeYpos;
        gazeXpos = limit(pos.x - xpos, -GAZE_MAX_XOFFSET, GAZE_MAX_XOFFSET);
        gazeYpos = limit(pos.y - ypos, -GAZE_MAX_YOFFSET, GAZE_MAX_YOFFSET);
        gazeMotion = new Vec2(gazeXpos - oldgx, gazeYpos-oldgy);
        return gazeMotion;
    }

    // tracks how far the gaze moved
    Vec2 gazeMotion = new Vec2();

    public float limit(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    public void gazeLeft(int n) {
        float oldgx = gazeXpos;
        gazeXpos = limit(gazeXpos - n, -GAZE_MAX_XOFFSET, GAZE_MAX_XOFFSET);
        gazeMotion =  new Vec2(gazeXpos - oldgx, 0);
    }

    public void gazeRight(int n) {
        gazeLeft(-n);
    }

    public void gazeUp(int n) {
        float oldgy = gazeYpos;
        gazeYpos = limit(gazeYpos - n, -GAZE_MAX_YOFFSET, GAZE_MAX_YOFFSET);
        gazeMotion =  new Vec2(0, gazeYpos - oldgy);        
    }

    public void gazeDown(int n) {
        gazeUp(-n);
    }

    public void setGazePosition(float x, float y) {
        gazeXpos = x;
        gazeYpos = y;
    }

    public Vec2 gazeAbsPosition() {
        return new Vec2(xpos+gazeXpos, ypos+gazeYpos);
    }

    public Object2D objectAtGaze() {
        return findObjAt(gazeAbsPosition());
    }


    /** Moves the gaze to center on the next item in the fovea. */
    public void gazeNext() {
        ArrayList<Object2D> items = findObjectsAt(gazeAbsPosition());
        // NYI
    }


    /**
       sorts objects list by proximity to position
     */
    public ArrayList<Object2D> sortPhysobjsByDistance(final Vec2 position) {
        ArrayList<Object2D> items = new ArrayList<Object2D>(plane0.physobjs);
        items.addAll(plane1.physobjs);

        // sort items by horizontal (x) position
        Collections.sort(items, new Comparator<Object2D>() {
                public int compare(Object2D o1, Object2D o2) {
                    float a = o1.getPosition().sub(position).length();
                    float b = o2.getPosition().sub(position).length();
                    return Integer.signum(Math.round(a - b));
                }
            });

        sortedItems = items;
        return items;
    }
    /**
       Foveate on the closest moving object to the current gaze
     */
    public Object2D foveateNextMovingObject(Vec2 position) {
        ArrayList<Object2D> items = sortPhysobjsByDistance(position);
        logger.debug("sorted by dist from "+position+" items = "+items.toString());
        logger.debug("foveateNextMovingObject foveatedObject = "+foveatedObject);
        int idx = -1;
        // find the index of the closest moving object to the 

        for (int i = 0; i < items.size(); i++) {
            Object2D obj = items.get(i);
            logger.debug(i + " checking object "+obj +" isMoving "+obj.isMoving());
            // if we're already looking at this obj, go to the next one
            if (obj == foveatedObject) continue;

            if (obj.isMoving()) {
                idx = i;
                break;
            }
        }

        if (idx == -1) {
            logger.info("foveateNextMovingObject, could not find any moving object!");
            return null;
        } else {
            foveatedObject = items.get(idx);
            gazeAt(foveatedObject);
            logger.info("foveateNextMovingObject foveatedObject => "+foveatedObject);
            return foveatedObject;
        }

    }


    /*
      FOVEATE_NEXT_OBJECT_LEFT,
      FOVEATE_NEXT_OBJECT_RIGHT,
      FOVEATE_NEXT_OBJECT_UP,
      FOVEATE_NEXT_OBJECT_DOWN
    */
    /**
       Move gaze to center of nearest object whose center of mass is to the left.
     */
    public ArrayList<Object2D> sortedItems = null;

    public ArrayList<Object2D> sortPhysobjsHorizontal() {
        ArrayList<Object2D> items = new ArrayList<Object2D>(plane0.physobjs);
        items.addAll(plane1.physobjs);

        // sort items by horizontal (x) position
        Collections.sort(items, new Comparator<Object2D>() {
                public int compare(Object2D o1, Object2D o2) {
                    Vec2 a = o1.getPosition();
                    Vec2 b = o2.getPosition();
                    return Integer.signum(Math.round(a.x - b.x));
                }
            });

        sortedItems = items;
        return items;
    }

    public ArrayList<Object2D> sortPhysobjsVertical() {
        ArrayList<Object2D> items = new ArrayList<Object2D>(plane0.physobjs);
        items.addAll(plane1.physobjs);

        // sort items by horizontal (x) position
        Collections.sort(items, new Comparator<Object2D>() {
                public int compare(Object2D o1, Object2D o2) {
                    Vec2 a = o1.getPosition();
                    Vec2 b = o2.getPosition();
                    return Integer.signum(Math.round(a.y - b.y));
                }
            });

        sortedItems = items;
        return items;
    }

    public Object2D foveateNextObjectLeft() {
        ArrayList<Object2D> items = sortPhysobjsHorizontal();
        
        Vec2 gaze = gazeAbsPosition();
        //app.println("gazeAbsPosition = "+gaze);

        int idx = -1;

        // find the index of the closest object to the left of the gaze position

        for (int i = items.size()-1; i >= 0; i--) {
            Object2D obj = items.get(i);
            if (obj.getPosition().x < gaze.x) {
                idx = i;
                break;
            }
        }
        //app.println("...idx = "+idx);

        if (idx == -1) {
            app.println("Error in foveateNextObjectLeft, could not find any object!");
            return null;
        } else {
            foveatedObject = items.get(idx);
            gazeAt(foveatedObject);
            return foveatedObject;
        }
    }

    public Object2D foveateNextObjectRight() {
        ArrayList<Object2D> items = sortPhysobjsHorizontal();
        Vec2 gaze = gazeAbsPosition();
        app.println("gazeAbsPosition = "+gaze);

        int idx = -1;

        // find the index of the closest object to the left of the gaze position

        for (int i = 0; i < items.size(); i++) {
            Object2D obj = items.get(i);
            if (obj.getPosition().x > gaze.x) {
                idx = i;
                break;
            }
        }
        app.println("...idx = "+idx);

        if (idx == -1) {
            app.println("Error in foveateNextObjectRight, could not find any object!");
            return null;
        } else {
            foveatedObject = items.get(idx);
            gazeAt(foveatedObject);
            return foveatedObject;
        }
    }


    public Object2D foveateNextObjectUp() {
        ArrayList<Object2D> items = sortPhysobjsHorizontal();
        
        Vec2 gaze = gazeAbsPosition();
        app.println("gazeAbsPosition = "+gaze);

        int idx = -1;

        // find the index of the closest object to the left of the gaze position

        for (int i = items.size()-1; i >= 0; i--) {
            Object2D obj = items.get(i);
            if (obj.getPosition().y < gaze.y) {
                idx = i;
                break;
            }
        }
        app.println("...idx = "+idx);

        if (idx == -1) {
            app.println("Error in foveateNextObjectLeft, could not find any object!");
            return null;
        } else {
            foveatedObject = items.get(idx);
            gazeAt(foveatedObject);
            return foveatedObject;
        }
    }


    public Object2D foveateNextObjectDown() {
        ArrayList<Object2D> items = sortPhysobjsHorizontal();
        Vec2 gaze = gazeAbsPosition();
        app.println("gazeAbsPosition = "+gaze);

        int idx = -1;

        // find the index of the closest object to the left of the gaze position

        for (int i = 0; i < items.size(); i++) {
            Object2D obj = items.get(i);
            if (obj.getPosition().y > gaze.y) {
                idx = i;
                break;
            }
        }
        app.println("...idx = "+idx);

        if (idx == -1) {
            app.println("Error in foveateNextObjectRight, could not find any object!");
            return null;
        } else {
            foveatedObject = items.get(idx);
            gazeAt(foveatedObject);
            return foveatedObject;
        }
    }

    // Includes proprioceptive sensors
    public void computeTouchSensors() {
        // update joint position sensors
        Vec2 h1gross = hand1.actualGrossPosition();
        Vec2 h1fine = hand1.actualFinePosition();

        Vec2 h2gross = hand2.actualGrossPosition();
        Vec2 h2fine = hand2.actualFinePosition();

        for (int i = -reachX; i <= reachX; i++) {
            for (int j = -reachY; j <= reachY; j++) {
                sensors.setSensorItem("hand1.gross.("+i+","+j+")", ((int)(h1gross.x) == i) && ((int)(h1gross.y) == j), clock);
                sensors.setSensorItem("hand2.gross.("+i+","+j+")", ((int)(h2gross.x) == i) && ((int)(h2gross.y) == j), clock);

                //sensors.setSensorItem("hand1.fine."+i+","+j, ((int)(h1fine.x) == i) && ((int)(h1fine.y) == j), clock);
                //sensors.setSensorItem("hand2.fine."+i+","+j, ((int)(h2fine.x) == i) && ((int)(h2fine.y) == j), clock);

            }
        }

        // ****************************************************************
        // *** TODO DEBUGGING REMOVE ME
        if (true) return;
        // ****************************************************************

        // gaze angle sensor
        for (int i = -5; i < 6; i++) {
            for (int j = -5; j < 6; j++) {
                sensors.setSensorItem("gaze.gross.("+i+","+j+")",
                     Math.round(gazeXpos/50) == i && Math.round(gazeYpos/50) == j, clock);
            }
        }

        // update joint force sensors
        Vec2 f1 = hand1.getJointForce();
        f1.mulLocal(100.0f);
        float trq1 = hand1.getJointTorque();

        Vec2 f2 = hand2.getJointForce();
        f2.mulLocal(100.0f);
        float trq2 = hand2.getJointTorque();
        // forces should be normalized to range [-1.0, 1.0]

        sensors.setSensorItem("hand1.force.x.neg", f1.x < 0, clock);
        sensors.setSensorItem("hand1.force.x.pos", f1.x > 0, clock);
        sensors.setSensorItem("hand1.force.y.neg", f1.y < 0, clock);
        sensors.setSensorItem("hand1.force.y.pos", f1.y > 0, clock);
        sensors.setSensorItem("hand1.torque.neg",   trq1 < 0, clock);
        sensors.setSensorItem("hand1.torque.pos",   trq1 > 0, clock);
        sensors.setSensorItem("hand2.force.x.neg", f2.x < 0, clock);
        sensors.setSensorItem("hand2.force.x.pos", f2.x > 0, clock);
        sensors.setSensorItem("hand2.force.y.neg", f2.y < 0, clock);
        sensors.setSensorItem("hand2.force.y.pos", f2.y > 0, clock);
        sensors.setSensorItem("hand2.torque.neg",   trq2 < 0, clock);
        sensors.setSensorItem("hand2.torque.pos",   trq2 > 0, clock);

        // update gripper touch and force sensors
        int t1 = hand1.touchingSides();
        sensors.setSensorItem("hand1.touch.left", (t1 & Hand.TOUCH_LEFT) != 0, clock);
        sensors.setSensorItem("hand1.touch.right", (t1 & Hand.TOUCH_RIGHT) != 0, clock);
        sensors.setSensorItem("hand1.touch.top", (t1 & Hand.TOUCH_TOP) != 0, clock);
        sensors.setSensorItem("hand1.touch.bottom", (t1 & Hand.TOUCH_BOTTOM) != 0, clock);

        int h1ObjectsGrasped = hand1.getWeldedObjects().size();
        sensors.setSensorItem("hand1.empty-grasp", h1ObjectsGrasped == 0, clock);
        sensors.setSensorItem("hand1.grasp-one", h1ObjectsGrasped == 1, clock);
        sensors.setSensorItem("hand1.grasp-two", h1ObjectsGrasped == 2, clock);
        sensors.setSensorItem("hand1.grasp-three", h1ObjectsGrasped == 3, clock);
        sensors.setSensorItem("hand1.grasp-many", h1ObjectsGrasped > 3, clock);

        
        int t2 = hand2.touchingSides();
        sensors.setSensorItem("hand2.touch.left", (t2 & Hand.TOUCH_LEFT) != 0, clock);
        sensors.setSensorItem("hand2.touch.right", (t2 & Hand.TOUCH_RIGHT) != 0, clock);
        sensors.setSensorItem("hand2.touch.top", (t2 & Hand.TOUCH_TOP) != 0, clock);
        sensors.setSensorItem("hand2.touch.bottom", (t2 & Hand.TOUCH_BOTTOM) != 0, clock);

        int h2ObjectsGrasped = hand2.getWeldedObjects().size();
        sensors.setSensorItem("hand2.empty-grasp", h2ObjectsGrasped == 0, clock);
        sensors.setSensorItem("hand2.grasp-one", h2ObjectsGrasped == 1, clock);
        sensors.setSensorItem("hand2.grasp-two", h2ObjectsGrasped == 2, clock);
        sensors.setSensorItem("hand2.grasp-three", h2ObjectsGrasped == 3, clock);
        sensors.setSensorItem("hand2.grasp-many", h2ObjectsGrasped > 3, clock);

        
    }


    public void moveHand2(int x, int y) {
        hand2.updateMouseJointPos(x,y);
    }

    public void moveHand1(int x, int y) {
        hand2.updateMouseJointPos(x,y);
    }



    ArrayList<String> noactions = new ArrayList<>();

    /**
     Return map containing {sensors: {items: { }, actions: [a1,a2,...  ]}}
     */
    public HashMap getCapabilities() {
        stepPhysicalWorld(noactions);
        HashMap c = new HashMap();
        c.put("sensors", sensors);
        sensors.setActions(Action.usableActionNames());
        return c;
    }



    public List getDebugState() {
        LinkedList<SimpleObject> objs = new LinkedList(Arrays.asList( hand1, hand2, block1, block2, eye));
        ArrayList val = new ArrayList();
        for (SimpleObject obj : objs) {
            val.add(obj.toMap());
        }
            
        // add in eyepos, hand positions
        return val;
    }

    // Convert debug state to LinkedList<Map<String, Object>>
    public LinkedList getDebugObjsList() {
        LinkedList debugobjs = new LinkedList();

        try {
            JSONObject jmap = new JSONObject();

            jmap.put("debuginfo", getDebugState());
            JSONParser jsonParser = new JSONParser();
            JSONObject msg = (JSONObject) jsonParser.parse(JSONObject.toJSONString(jmap));
            JSONArray debuginfo = (JSONArray) msg.get("debuginfo");
            for (int i = 0; i < debuginfo.size(); i++) {
                debugobjs.add(debuginfo.get(i));
            }
        } catch (org.json.simple.parser.ParseException e) {
        }
        return debugobjs;

    }

    public Map<String, Object> doSomethingInSMS(String cmdName, Object value) {
        Map <String, Object> args = ((Map <String, Object>)value);
        Map<String, Object> vals = new HashMap<>();
        if ("move_to".equals(cmdName)) {

            String objname = (String) args.get("name_of_thing");
            int x = (int) args.get("x");
            int y = (int) args.get("y");

            SimpleObject obj = findObjectByName(objname);

            moveTo(obj, x ,y);

        }
        else if("get_obj_pos".equals(cmdName)){
            String objname = (String) args.get("name_of_thing");
            LinkedList<String> objs = new LinkedList(Arrays.asList( "h", "j", "b1", "b2", "v"));
            if (objs.contains(objname)) {
                SimpleObject obj = findObjectByName(objname);
                int x = obj.pos.x;
                int y = obj.pos.y;
                vals.put(objname+"x",x);
                vals.put(objname+"y",y);
            }

        }
        return vals;
    }

}

