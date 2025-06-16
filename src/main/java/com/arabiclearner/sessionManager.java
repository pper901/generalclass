/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class sessionManager {
    private HashMap<String, String> map = new HashMap();
    private String sessionId;

    sessionManager() {
    }

    public String genSessionId() {
        String sessionId1 = "myCookie=" + UUID.randomUUID().toString();
        return sessionId1;
    }

    public void setSessionId(String s) {
        this.sessionId = s;
    }

    public String getSessionId() {
        if (this.sessionId == null || this.sessionId.isEmpty()) {
            this.setSessionId(this.genSessionId());
        }
        return this.sessionId;
    }

    public synchronized void addSession(String sessionId, String content) {
        System.out.println("The content for this session: " + sessionId + " is: " + content);
        if (!this.map.containsKey(sessionId)) {
            System.out.println("Writing session to map");
            this.map.put(sessionId.trim(), content);
        } else {
            System.out.println("session already exists");
        }
    }

    public synchronized String getSessionContent(String session) {
        if (this.map.containsKey(session.trim())) {
            System.out.println("This session:" + session);
            System.out.println("Contains this content:" + this.map.get(session));
            return this.map.get(session.trim());
        }
        System.out.println("Session before failing:" + session);
        return "Session Not Found";
    }

    public void removeSession(String session) {
        this.map.remove(session);
    }

    public void printMap() {
        System.out.println("Printing session map:");
        for (Map.Entry<String, String> entry : this.map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key + " : " + value);
        }
    }

    public void writeMapToFile() {
        String filePath = "tempfile.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));){
            for (Map.Entry<String, String> entry : this.map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                try {
                    String fileContent = sessionManager.readFileToString(filePath);
                    if (fileContent.contains(key + "==" + value)) {
                        System.out.println("Value already exists in the file.");
                        continue;
                    }
                    sessionManager.writeToFile(filePath, key + "==" + value);
                    System.out.println("Value written to the file.");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("File created and populated successfully.");
        }
        catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static String readFileToString(String filePath) throws IOException {
        File file = new File(filePath);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file));){
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static void writeToFile(String filePath, String value) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, true);){
            writer.write(value + "\n");
        }
    }

    public void readMapFromFile() {
        String filePath = "tempfile.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("==");
                if (parts.length != 2) continue;
                String key = parts[0];
                String value = parts[1];
                this.map.put(key, value);
            }
            System.out.println("Map populated from file successfully.");
        }
        catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void deleteSessionFile() {
        File file = new File("tempfile.txt");
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("File deleted successfully.");
            } else {
                System.out.println("Unable to delete the file.");
            }
        } else {
            System.out.println("File does not exist.");
        }
    }
}

