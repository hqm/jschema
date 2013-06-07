package com.beartronics.jschema;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import com.typesafe.config.*;

/**
 *
 */
public class App 
{
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
        System.out.println("The setting 'foo' is: " + config.getString("foo"));
        System.out.println("The setting 'bar' is: " + config.getString("bar"));
        System.out.println( "creating schemas n="+n );

        stage = new Stage();

        stage.initWorld(n);
    }

}
