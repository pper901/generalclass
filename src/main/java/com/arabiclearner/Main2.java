/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.io.IOException;
// import org.eclipse.jetty.server.Server;
// import org.eclipse.jetty.server.handler.HandlerCollection;
// import org.eclipse.jetty.websocket.server.WebSocketHandler;
// import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
// import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
// import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
// import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class Main2 {
    static classHandler cHandler = new classHandler();

    public static void main(String[] args) throws Exception {
        // new Thread(() -> {
            try {
                System.out.println("Starting normal server");
                new ServerRunClass2(cHandler);
            }
            catch (IOException e) {
                System.out.println("Error found is " + e);
            }
        // }).start();
        // new Thread(() -> {
        //     try {
        //         Main2.startWebSocketServer();
        //     }
        //     catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // }).start();
    }

    // private static void startWebSocketServer() throws IOException {
    //     int port = 8081;
    //     Server server = new Server(port);
    //     HandlerCollection handlers = new HandlerCollection();
    //     MyWebSocketHandler webSocketHandler = new MyWebSocketHandler(cHandler);
    //     handlers.addHandler(webSocketHandler);
    //     server.setHandler(handlers);
    //     try {
    //         server.start();
    //         server.join();
    //     }
    //     catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    // public static class MyWebSocketHandler
    // extends WebSocketHandler
    // implements WebSocketCreator {
    //     classHandler cm;

    //     public MyWebSocketHandler(classHandler cHandler) {
    //         this.cm = cHandler;
    //     }

    //     @Override
    //     public void configure(WebSocketServletFactory factory) {
    //         factory.setCreator(this);
    //     }

    //     @Override
    //     public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
    //         MyWebSocketEndpoint endpoint = new MyWebSocketEndpoint(this.cm);
    //         return endpoint;
    //     }
    // }
}

