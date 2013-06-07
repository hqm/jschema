package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import com.typesafe.config.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class App 
{
    final Logger logger =
        LoggerFactory.getLogger(App.class);

    Config config;

    public static void main( String[] args )
    {
        App app = new App();
        app.run(100);
    }

    Stage stage;

    public App() {
        config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference(), "application");
    }

    public void run(int n) {
        logger.info("The setting 'foo' is: " + config.getString("foo"));
        logger.info("The setting 'bar' is: " + config.getString("bar"));
        logger.info( "creating schemas n="+n );

        stage = new Stage();

        stage.initWorld(n);
    }

}
