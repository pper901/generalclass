package com.arabiclearner;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;

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

public class startWebSocket2 {
    static Server server = new Server();
    private static classHandler cHandler;
    private boolean isRunning = false;

    public startWebSocket2(classHandler cHandler) {
        startWebSocket2.cHandler = cHandler;

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            
            // Shut down server socket if it's being managed
            if (isRunning){
                stopWebS();
            }
    
            }));
    }

    public boolean isWebSocketStart() {
        return this.isRunning;
    }

    public void startWebS() {
        System.out.println("Starting a new WebSocket server");
        new Thread(() -> {
            try {
                this.startWebSocketServer();
                this.isRunning = true;
            } catch (IOException e) {
                this.isRunning = false;
                System.out.print("An error occurred with the WebSocket server: " + e);
            }
        }).start();
    }

    private void startWebSocketServer() throws IOException {
        boolean isLocalHost = isLocalHost();
        int port = isLocalHost ? 8081 : 8443;

        System.out.println("is it localhost "+isLocalHost);

        if (!isLocalHost) {
            String keystorePassword = "generalclass";
        
        // Load the keystore from the resources folder
        try (InputStream keystoreStream = getClass().getResourceAsStream("keystore.jks")) {
                if (keystoreStream == null) {
                    throw new RuntimeException("Keystore file not found in the resources folder");
                }

                // Create and initialize the KeyStore
                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(keystoreStream, keystorePassword.toCharArray());

                // Create and configure the SslContextFactory
                SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
                sslContextFactory.setKeyStore(keyStore);
                sslContextFactory.setKeyStorePassword(keystorePassword);

                
                    ServerConnector sslConnector = new ServerConnector(server, 
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()), 
                    new HttpConnectionFactory());
                sslConnector.setPort(port);
                server.addConnector(sslConnector);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to load the keystore", e);
            }
        } else {
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(port);
            connector.setHost("0.0.0.0");
            server.addConnector(connector);
        }

        HandlerCollection handlers = new HandlerCollection();
        MyWebSocketHandler webSocketHandler = new MyWebSocketHandler(cHandler);
        handlers.addHandler(webSocketHandler);
        server.setHandler(handlers);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isLocalHost() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        String hostname = addr.getHostName();
        System.out.println("The address is of the hostname is "+addr);
        return hostname.equals("DESKTOP-NROO013") || hostname.equals("127.0.0.1");
    }

    public void stopWebS() {
        try {
            server.stop();
            server.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MyWebSocketHandler extends WebSocketHandler implements WebSocketCreator {
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

