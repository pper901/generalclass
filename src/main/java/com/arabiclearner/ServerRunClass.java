package com.arabiclearner;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class ServerRunClass {
    @SuppressWarnings("unused")
    private sessionManager sManager;
    private classHandler cH;
    private HashMap<String, Object> websites = new HashMap<>();
    private ExecutorService sslExecutor;
    private ExecutorService httpExecutor;
    private ExecutorService clientHandlerExecutor;
    private Socket listensv;
    Socket lsv;

    public ServerRunClass(classHandler cH) throws IOException {
        this.cH = cH;
        loadWebsiteResources();

        // SSL Server Executor
        sslExecutor = Executors.newFixedThreadPool(10);
        Runnable sslServerTask = this::startSSLServer;
        sslExecutor.submit(sslServerTask);

        // HTTP Server Executor
        httpExecutor = Executors.newFixedThreadPool(10);
        Runnable httpServerTask = this::startHTTPServer;
        httpExecutor.submit(httpServerTask);

        // Client Handler Executor
        clientHandlerExecutor = Executors.newFixedThreadPool(10);


        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            
        // Shut down server socket if it's being managed
        if (listensv != null && !listensv.isClosed()) {
                try {
                    listensv.close();
                    System.out.println("SSl Server socket closed");
                } catch (IOException e) {
                    System.err.println("Error closing ssl server socket: " + e.getMessage());
                }
            }
        // Shut down server socket if it's being managed
        if (lsv != null && !lsv.isClosed()) {
            try {
                lsv.close();
                System.out.println("http Server socket closed");
            } catch (IOException e) {
                System.err.println("Error closing http server socket: " + e.getMessage());
            }
        }

        this.shutdown();
        }));
    }

    private void loadWebsiteResources() throws IOException {
        websites.put("index", readFileToString("/index.html"));
        websites.put("stu-index", readFileToString("/stu-index.html"));
        websites.put("style", readFileToString("/style.css"));
        websites.put("engine", readFileToString("/engine.js"));
        websites.put("adapter", readFileToString("/adapter-latest.js"));
        websites.put("lengine", readFileToString("/lengine.js"));
        websites.put("home", readFileToString("/home.html"));
        websites.put("google", readFileToString("/google4e383da5d2ff81b8.html"));
        websites.put("ads", readFileToString("/ads.txt"));
        websites.put("favicon.ico", readFiletoByteArray("favicon.ico"));
        websites.put("classroompix.jpg", readFiletoByteArray("classroompix.jpg"));
        websites.put("Spinner-1s-200px.gif", readFiletoByteArray("Spinner-1s-200px.gif"));
        websites.put("generalClass.png", readFiletoByteArray("generalClass.png"));
    }

    private void startSSLServer() {
        try {
            String keystorePath = "keystore.jks";
            String keystorePassword = "generalclass";
            SSLContext sslContext = createSSLContext(keystorePath, keystorePassword);
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setProtocols(new String[]{"TLSv1.1", "TLSv1.2", "TLSv1.3"});
            sslServerSocket.setSSLParameters(sslParameters);
            String[] cipherSuites = sslServerSocket.getSupportedCipherSuites();
            sslServerSocket.setEnabledCipherSuites(cipherSuites);
            String publicIpAddress = "0.0.0.0";
            InetAddress serverAddress = InetAddress.getByName(publicIpAddress);

            // Bind to all available network interfaces on port 443
            sslServerSocket.bind(new InetSocketAddress("0.0.0.0", 443));
            System.out.println("Server IP Address: " + serverAddress.getHostAddress() + ", SSL Port: " + 443 + ", Server Hostname: " + serverAddress.getHostName());

            while (true) {
                listensv = sslServerSocket.accept();
                System.out.println("New Client Request Received: " + listensv);
                // clientHandlerExecutor.submit(() -> handleSSLClient(listensv));
                handleSSLClient(listensv);

            }
        } catch (IOException e) {
            System.out.print("An error occurred with the SSL server: " + e);
        }
    }

    private void startHTTPServer() {
        try (ServerSocket sv = new ServerSocket(80)) {
            while (true) {
                lsv = sv.accept();
                // clientHandlerExecutor.submit(() -> handleHTTPClient(lsv));
                handleHTTPClient(lsv);
            }
        } catch (IOException e) {
            System.out.print("An error occurred with the plain/text HTTP server: " + e);
        }
    }

    private void handleSSLClient(Socket clientSocket) {
        try {
            long startTime = System.nanoTime();
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("Creating a Processor For this Client...");
            clientHandler client = new clientHandler(clientSocket, in, out, cH, this.websites, startTime);
            client.run(); //Use run() if clientHandler is a Runnable
            if(client.isClosed){
                shutdownExecutor(clientHandlerExecutor);
            }
        } catch (Exception e) {
            System.out.print("Server Error:" + e);
        }
    }

    private void handleHTTPClient(Socket clientSocket) {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("Creating a Processor For this Client...");
            httpClientHandler client = new httpClientHandler(clientSocket, in, out);
            client.run(); // Use run() if httpClientHandler is a Runnable
            if(client.isClosed){
                shutdownExecutor(clientHandlerExecutor);
            }
        } catch (Exception e) {
            System.out.print("Server Error:" + e);
        }
    }

    private SSLContext createSSLContext(String keystorePath, String keystorePassword) {
        try {
            // Load keystore from resources
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream is = getClass().getResourceAsStream(keystorePath)) {
                if (is != null) {
                    keyStore.load(is, keystorePassword.toCharArray());
                } else {
                    System.err.println("Keystore file not found: " + keystorePath);
                    throw new IllegalArgumentException("Keystore file not found: " + keystorePath);
                }
            } catch (IOException e) {
                System.err.println("Error loading keystore file: " + e.getMessage());
                throw new RuntimeException(e);
            }
    
            // Initialize KeyManagerFactory and TrustManagerFactory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
    
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
    
            // Create and initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
    
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Error creating SSL context", e);
        }
    }
    
    private synchronized String readFileToString(String resourceName) throws IOException {
        StringBuilder sb = new StringBuilder();
        System.out.println("This is the resource name: " + resourceName);
        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }
        System.out.println("This is the resource name after: " + resourceName);
        try (InputStream inputStream = this.getClass().getResourceAsStream(resourceName);
             InputStreamReader streamReader = new InputStreamReader(inputStream);
             BufferedReader reader = new BufferedReader(streamReader)) {
            if (inputStream != null) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } else {
                throw new IOException("Resource not found: " + resourceName);
            }
        }
        return sb.toString();
    }

    private synchronized byte[] readFiletoByteArray(String resourceName) {
        System.out.println("This is the resource name I am dealing with: " + resourceName);
        try {
            int nRead;
            InputStream inputStream = this.getClass().getResourceAsStream(resourceName);
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to shut down all executors
    public void shutdown() {
        shutdownExecutor(sslExecutor, listensv);
        shutdownExecutor(httpExecutor, lsv);
        shutdownExecutor(clientHandlerExecutor);
    }

    // Helper method to shut down an executor
    private void shutdownExecutor(ExecutorService executor, Socket serverSocket) {
    
        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait for existing tasks to terminate
            if (!executor.awaitTermination(120, TimeUnit.SECONDS)) { // Increased timeout to 120 seconds
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait again for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait for existing tasks to terminate
            if (!executor.awaitTermination(120, TimeUnit.SECONDS)) { 
                // Increased timeout to 120 seconds
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait again for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
