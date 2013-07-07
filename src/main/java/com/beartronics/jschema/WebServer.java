package com.beartronics.jschema;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class WebServer implements Container {

    JSchema app;

    public WebServer(JSchema app) {
        this.app = app;
    }

   public void handle(Request request, Response response) {
      try {
         PrintStream body = response.getPrintStream();
         long time = System.currentTimeMillis();
   
         response.setValue("Content-Type", "text/plain");
         response.setValue("Server", "JSchema/1.0 (Simple 4.0)");
         response.setDate("Date", time);
         response.setDate("Last-Modified", time);
   
         String path = request.getPath().getPath();
         if (path.matches("/smap")) {
             printSenseMap(body);
         }

         body.close();
      } catch(Exception e) {
         e.printStackTrace();
      }
   } 

    void printSenseMap(PrintStream body) {
        body.println(app.sms.getWorldState());
    }

}
