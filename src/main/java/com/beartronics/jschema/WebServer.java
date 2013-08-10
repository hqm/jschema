package com.beartronics.jschema;



import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class WebServer implements Container {

    JSchema app;
    String stylesheet = "";

    public WebServer(JSchema app) {
        this.app = app;
        stylesheet = contentFromClasspath("/main.css",this.getClass()) ;
    }

   public void handle(Request request, Response response) {
      try {
         PrintStream body = response.getPrintStream();
         long time = System.currentTimeMillis();
   
         response.setValue("Content-Type", "text/html");
         response.setValue("Server", "JSchema/1.0 (Simple 4.0)");
         response.setDate("Date", time);
         response.setDate("Last-Modified", time);
   
         String path = request.getPath().getPath();
         if (path.matches("/")) {
             printIndex(body);
         } else if (path.matches("/raw/map")) {
             printSenseMap(body);
         } else if (path.matches("/items/map")) {
             printItems(body);
         } else if (path.matches("/items/action")) {
             showAction(body, request);
         } else if (path.matches("/items/item")) {
             showItem(body, request);
         } else if (path.matches("/items/schema")) {
             showSchema(body, request);
         }


         body.close();
      } catch(Exception e) {
         e.printStackTrace();
      }
   } 

    void showAction(PrintStream body, Request request) {
        Query query = request.getQuery();
        String value = query.get("id");
        int id = Integer.parseInt(value);
        Action action = app.stage.actions.get(id);
        header(body);
        body.println(action.toHTML());
        footer(body);
    }

    void showSchema(PrintStream body, Request request) {
        Query query = request.getQuery();
        String value = query.get("id");
        int id = Integer.parseInt(value);
        Schema schema = app.stage.schemas.get(id);
        header(body);
        body.println(schema.toHTML());
        footer(body);
    }

    void showItem(PrintStream body, Request request) {
        Query query = request.getQuery();
        String value = query.get("id");
        int id = Integer.parseInt(value);
        Item item = app.stage.items.get(id);
        header(body);
        body.println(item.toHTML());
        footer(body);
    }

    String linkToMainPage() {
        return "<a href=\"/items/map\">Main Map</a>";
    }

    void header(PrintStream body) {
        body.println("<html>");
        body.println("<style>\n"+stylesheet+"\n</style>");
        body.println("<body>");
        body.println(linkToMainPage());

    }

    void footer(PrintStream body) {
        body.println(linkToMainPage());
        body.println("</body>");
        body.println("</html>");
    }


    void printIndex(PrintStream body) {
        header(body);
        body.println("<pre>" +
                     "<a href=\"items/map\">items/map</a>\n"+
                     "<a href=\"raw/map\">raw/map</a>\n"+
                     "</pre>");
        footer(body);
    }

    void printSenseMap(PrintStream body) {
        header(body);
        body.println("<pre>\n");
        body.println(app.sms.getWorldState());
        body.println("</pre>\n");
        footer(body);
    }

    void printItems(PrintStream body) {
        header(body);
        body.println(app.stage.htmlPrintState());
        footer(body);
    }


    public static String contentFromClasspath(String name, Class clasz) {
        try {
        URL resource = clasz.getResource(name);
        if (resource == null) {
            throw new IllegalArgumentException(String.format("Error opening %s from %s", name, clasz));
        }
        return IOUtils.toString(resource.openStream());
        } catch (IOException e) {
            System.err.println("exception in contentFromClasspath "+e);
            throw new RuntimeException(e);
        }
    }

}
