package com.arabiclearner;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerRunClass2 {
    private final ExecutorService executorService;
    @SuppressWarnings("unused")
    private final sessionManager sManager;
    private final classHandler cH;
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final HashMap<String, Object> websites = new HashMap();

    public ServerRunClass2(classHandler cHandler) throws IOException {
        this.executorService = Executors.newFixedThreadPool(10); // Adjust pool size as needed
        this.sManager = new sessionManager();
        this.cH = cHandler;

        initializeWebsiteContent();

        ServerSocket sv = new ServerSocket(8080);
        System.out.println("Starting the serverrunclass");

        while (true) {
            try {
                Socket listensv = sv.accept();
                System.out.println("New Client Request Received: " + listensv);

                long startTime = System.nanoTime();
                DataInputStream in = new DataInputStream(listensv.getInputStream());
                DataOutputStream out = new DataOutputStream(listensv.getOutputStream());

                // Use ExecutorService to handle each request in a separate thread
                executorService.execute(new ClientHandlerRunnable(listensv, in, out, startTime));

            } catch (Exception e) {
                System.out.print("Server Error:" + e);
                sv.close();
                break;
            }
        }
    }

    private void initializeWebsiteContent() throws IOException {
        // Read files and populate the websites HashMap
        this.websites.put("index", readFileToString("/index.html"));
        this.websites.put("stu-index", readFileToString("/stu-index.html"));
        this.websites.put("style", readFileToString("/style.css"));
        this.websites.put("engine", readFileToString("/engine.js"));
        this.websites.put("adapter", readFileToString("/adapter-latest.js"));
        this.websites.put("lengine", readFileToString("/lengine.js"));
        this.websites.put("home", readFileToString("/home.html"));
        this.websites.put("google", readFileToString("/google4e383da5d2ff81b8.html"));
        this.websites.put("ads", readFileToString("/ads.txt"));
        this.websites.put("favicon.ico", readFiletoByteArray("favicon.ico"));
        this.websites.put("classroompix.jpg", readFiletoByteArray("classroompix.jpg"));
        this.websites.put("Spinner-1s-200px.gif", readFiletoByteArray("Spinner-1s-200px.gif"));
        this.websites.put("generalClass.png", readFiletoByteArray("generalClass.png"));
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

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        return sb.toString();
    }

    private synchronized byte[] readFiletoByteArray(String resourceName) throws IOException {
        System.out.println("This is the resource name: " + resourceName);

        try (InputStream inputStream = this.getClass().getResourceAsStream(resourceName);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }

            byte[] data = new byte[4096];
            int nRead;

            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            return buffer.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private class ClientHandlerRunnable implements Runnable {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;
        private final long startTime;

        public ClientHandlerRunnable(Socket socket, DataInputStream in, DataOutputStream out, long startTime) {
            this.socket = socket;
            this.in = in;
            this.out = out;
            this.startTime = startTime;
        }

        @Override
        public void run() {
            try {
                System.out.println("Creating a Processor For this Client...");
                clientHandler client = new clientHandler(socket, in, out, cH, websites, startTime);
                client.start(); // Assuming clientHandler extends Thread and has start() method
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
