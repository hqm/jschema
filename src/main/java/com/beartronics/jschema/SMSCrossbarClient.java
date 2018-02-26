package com.beartronics.jschema;


import java.util.logging.Logger;

import java.net.UnknownHostException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.crossbar.autobahn.wamp.Client;
import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.wamp.types.CloseDetails;
import io.crossbar.autobahn.wamp.types.ExitInfo;
import io.crossbar.autobahn.wamp.types.InvocationDetails;
import io.crossbar.autobahn.wamp.types.SessionDetails;

import org.apache.commons.cli.*;


public class SMSCrossbarClient {

    private static final Logger logger = Logger.getLogger(SMSCrossbarClient.class.getName());

    public Box2DSensoriMotorSystem sms = null;

    public SMSCrossbarClient(Box2DSensoriMotorSystem bss) {
        this.sms = bss;
    }

    // SMS WAMP commands
    public static final String SMS_GET_CAPABILITIES    = "ai.leela.sms.get_capabilities"; // () => capabilities_json
    public static final String SMS_STEP_WORLD          = "ai.leela.sms.step_world";       // [actionNames] => world_state
    public static final String SMS_GET_OBJECTS         = "ai.leela.sms.get_objects";  // () => Map(objectname -> value)
    public static final String SMS_DO_SOMETHING_IN_SMS = "ai.leela.sms.do_something_in_sms";   // name, value => value

    public static String CROSSBAR_URI = "ws://127.0.0.1:1964/ws";
    public static String CROSSBAR_REALM = "leela";


    public void initializeSensoriMotorSystem() {
 //       sms.initPrimitiveActions();
  //      sms.initializeObjects();
    }


    public CompletableFuture<ExitInfo> connect() {

        return connect(CROSSBAR_URI, CROSSBAR_REALM);
    }

    public CompletableFuture<ExitInfo> connect(String websocketURL, String realm) {
        logger.info("SMS connecting to URI="+SMSCrossbarClient.CROSSBAR_URI + ", realm="+SMSCrossbarClient.CROSSBAR_REALM);

        Session session = new Session();
        session.addOnConnectListener(this::onConnectCallback);
        session.addOnJoinListener(this::onJoinCallback);
        session.addOnLeaveListener(this::onLeaveCallback);
        session.addOnDisconnectListener(this::onDisconnectCallback);

        // finally, provide everything to a Client instance and connect
        Client crossbar = new Client(session, websocketURL, realm);
        return crossbar.connect();
    }

    private void onConnectCallback(Session session) {
        logger.info("SMS Session connected, ID=" + session.getID());
    }

    private void onJoinCallback(Session session, SessionDetails details) {
        System.out.println("SMS.onJoinCallback registering handlers");
        session.register(SMS_STEP_WORLD, this::handleStepWorld)
            .thenAccept(reg -> System.out.println("SMSCrossbarClient registered procedure: " + SMS_STEP_WORLD));

       session.register(SMS_GET_OBJECTS, this::handleGetObjects)
           .thenAccept(reg -> logger.info("Registered procedure: " + SMS_GET_OBJECTS));

  /*
       session.register(CrossbarClient.SMS_GET_SOMETHING_FROM_SMS, this::handleDoSomethingInSMS)

           .thenAccept(reg -> logger.info("Registered procedure: " + CrossbarClient.SMS_GET_SOMETHING_FROM_SMS));
           */

       session.register(SMS_DO_SOMETHING_IN_SMS, this::handleDoSomethingInSMS)
           .thenAccept(reg -> logger.info("Registered procedure: " +SMS_DO_SOMETHING_IN_SMS));

        session.register(SMS_GET_CAPABILITIES, this::handleGetCapabilities)
                .thenAccept(reg -> logger.info("Registered procedure: "+SMS_GET_CAPABILITIES));
    }


    private void onLeaveCallback(Session session, CloseDetails detail) {
        logger.info(String.format("Left reason=%s, message=%s", detail.reason, detail.message));
    }

    private void onDisconnectCallback(Session session, boolean wasClean) {
        logger.info(String.format("Session with ID=%s, disconnected.", session.getID()));
        connect(CROSSBAR_URI, CROSSBAR_REALM);
    }

    /**************** Command Handlers  ****************/

    /**
       Sends JSON SMS capabilities

     "capabilities": {
         "sensors": {
            "actions": ["nullaction", "handl", ..., "eyeb" ], 
            "clock": 1, 
            "items": {
                "fov11.00": false, 
                "hp23": false, 
                ...,
                "vp33": false
                }
         }
     }

     *
     * @param
     * @return
     */
    List<Object> handleGetCapabilities(List<Object> args, InvocationDetails details) {
        System.out.println("SMSCrossbarClient received get_capabilities command");
        return Arrays.asList(sms.getCapabilities(), details.session.getID(), "Java");
    }

    private List<Object> handleStepWorld(List<Object> args, InvocationDetails details) {
        sms.stepPhysicalWorld( (List<String>) args.get(0));
        SensorState sensors = sms.getSensorState();
        Map<String, Object> stateMap = sensors.toMap();
        return Arrays.asList(stateMap, details.session.getID(), "Java");
    }

    private List<Object> handleGetObjects(List<Object> args, InvocationDetails details) {
        return Arrays.asList(sms.getDebugState(), details.session.getID(), "Java");
    }

    private List<Object> handleDoSomethingInSMS(List<Object> args, InvocationDetails details) {
        String something = (String) (args.get(0));
        Map<String,Object> values = (Map<String,Object>) args.get(1);
        return Arrays.asList(sms.doSomethingInSMS(something, values), details.session.getID(), "Java");

    }
    /*
    private List<Object> handleGetSomethingFromSMS(List<Object> args, InvocationDetails details) {
        Map<String,Object> kwargs = (Map<String,Object>) args.get(0);
        String nameOfThingInSMS = (String) kwargs.get("name_of_thing");
        return Arrays.asList(sms.getInfoFromSMS(nameOfThingInSMS), details.session.getID(), "Java");
    }
    */



}
