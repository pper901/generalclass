/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.io.IOException;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class startWebSocket {
    static Server server = new Server();
    private static classHandler cHandler;
    private boolean isRunning = false;

    startWebSocket(classHandler cHandler) {
        startWebSocket.cHandler = cHandler;
    }

    public boolean isWebsocketStart() {
        return this.isRunning;
    }

    public void startWebS() {
        System.out.println("Starting a new Websocket server");
        new Thread(() -> {
            try {
                startWebSocket.startWebSocketServer();
                this.isRunning = true;
            }
            catch (IOException e) {
                this.isRunning = false;
                System.out.print("an error occured with the websocket server" + e);
            }
        }).start();
    }

    private static void startWebSocketServer() throws IOException {
        String keystorePath = "keystore.jks";
        String keystorePassword = "generalclass";
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStorePassword(keystorePassword);
        ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory());
        sslConnector.setPort(8443);
        server.addConnector(sslConnector);
        HandlerCollection handlers = new HandlerCollection();
        MyWebSocketHandler webSocketHandler = new MyWebSocketHandler(cHandler);
        handlers.addHandler(webSocketHandler);
        server.setHandler(handlers);
        try {
            server.start();
            server.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopWebS() {
        try {
            server.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MyWebSocketHandler
    extends WebSocketHandler
    implements WebSocketCreator {
        classHandler cm;

        public MyWebSocketHandler(classHandler cHandler) {
            this.cm = cHandler;
        }

        @Override
        public void configure(WebSocketServletFactory factory) {
            factory.setCreator(this);
        }

        @Override
        public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
            MyWebSocketEndpoint endpoint = new MyWebSocketEndpoint(this.cm);
            return endpoint;
        }
    }
}

