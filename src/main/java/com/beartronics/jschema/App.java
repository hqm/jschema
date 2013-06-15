package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import com.typesafe.config.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main class for schema mechanism implementation
 */
public class App 
{
    final Logger logger =
        LoggerFactory.getLogger(App.class);

    public static void main( String[] args )
    {
        App app = new App();
        app.init();
        app.run();
    }

    Stage stage;


    Config config;

    public App() {
        // load config file
        config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference(), "application");
    }

    public void init() {
        stage = new Stage(config);
        stage.initWorld();
    }


    public void run() {
        stage.run();
    }

}
