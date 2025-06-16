/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class requestManager {
    BufferedReader br;
    String[] firstline;
    HashMap<String, String> map = new HashMap();

    requestManager(DataInputStream in) throws IOException {
        this.br = new BufferedReader(new InputStreamReader(in));
        if (this.br == null) {
            System.out.println("The Buffered reader is null");
            return;
        }
        this.firstline = this.br.readLine().split(" ");
        this.populateTheMap();
    }

    public String getTheMethod() {
        System.out.println("The Method is: " + this.firstline[0]);
        return this.firstline[0];
    }

    public String getTheAddress() {
        System.out.println("The Address is: " + this.firstline[1]);
        return this.firstline[1];
    }

    public String getTheContent() {
        if (this.map != null && this.map.containsKey("Content")) {
            System.out.println("The content is: " + this.map.get("Content"));
            return this.map.get("Content");
        }
        return null;
    }

    public BufferedReader getTheReader() {
        return this.br;
    }

    public String getTheName() {
        System.out.println("Getting the name " + this.map.get("Name:"));
        return this.map.get("Name:");
    }

    public String getTheMessage() {
        System.out.println("Getting the message:" + this.map.get("Message:"));
        return this.map.get("Message:");
    }

    public String getTheOffer() {
        System.out.println("Getting Offer " + this.map.get("Offer:"));
        return this.map.get("Offer:");
    }

    public String getTheReady() {
        System.out.println("Getting the ready state " + this.map.get("ready:"));
        return this.map.get("ready:");
    }

    public String getTheAnswerReady() {
        System.out.println("Getting the answer ready state " + this.map.get("Answerready:"));
        return this.map.get("Answerready:");
    }

    public String getTheAnswer() {
        System.out.println("Getting Answer " + this.map.get("Answer:"));
        return this.map.get("Answer:");
    }

    public String getTheIce() {
        System.out.println("Getting Ice " + this.map.get("ICE:"));
        return this.map.get("ICE:");
    }

    public String getTheNull() {
        System.out.println("Getting Null " + this.map.get("null:"));
        if (this.map.get("null:") != null) {
            return this.map.get("null:");
        }
        return null;
    }

    public String getTheCookie() {
        System.out.println("Getting the cookie");
        this.printMap();
        if (this.map != null && this.map.containsKey("Cookie")) {
            System.out.println("The cookie value is " + this.map.get("Cookie"));
            return this.map.get("Cookie");
        }
        return null;
    }

    public String extractWebSocketKey() {
        if (this.map != null && this.map.containsKey("Sec-WebSocket-Key:")) {
            System.out.println("Websocket: " + this.map.get("Sec-WebSocket-Key:"));
            return this.map.get("Sec-WebSocket-Key:");
        }
        return null;
    }

    public String generateSecWebSocketAccept() throws NoSuchAlgorithmException {
        String key = this.extractWebSocketKey().trim();
        String concatenatedKey = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = md.digest(concatenatedKey.getBytes(StandardCharsets.UTF_8));
        String base64Encoded = Base64.getEncoder().encodeToString(hashBytes);
        return base64Encoded.trim();
    }

    public byte[] readWebSocketFrame() throws IOException {
        String line;
        StringBuilder frameBuilder = new StringBuilder();
        System.out.println("Here reading websocket content");
        while ((line = this.br.readLine()) != null && !line.isEmpty()) {
            System.out.println(line);
            System.out.println("populating the frameBuilder...");
            frameBuilder.append(line).append("\r\n");
        }
        System.out.println("Populated the FrameBuilder");
        String frame = frameBuilder.toString().trim();
        if (frame.isEmpty()) {
            System.out.println("The frame is empty");
            return null;
        }
        return frame.getBytes(StandardCharsets.UTF_8);
    }

    public void populateTheMap() throws IOException {
        String rB;
        String line;
        StringBuilder requestBody = new StringBuilder();
        String method = this.getTheMethod();
        while ((line = this.br.readLine()) != null && !line.isEmpty()) {
            requestBody.append(line).append("\r\n");
        }
        if (requestBody.toString() != null) {
            String[] lines = requestBody.toString().split("\r\n");
            for (int i = 1; i < lines.length; ++i) {
                String[] header;
                System.out.println("Rget:" + lines[i]);
                if (lines[i] == null || lines[i].isEmpty() || (header = lines[i].split(": ")).length != 2) continue;
                System.out.println("Line: " + header[0] + ":" + header[1]);
                this.map.put(header[0], header[1]);
            }
        }
        if (!method.equals("GET")) {
            int contentLength = requestManager.getContentLength(requestBody.toString());
            char[] body = new char[contentLength];
            this.br.read(body);
            rB = new String(body);
            System.out.println("Content: " + rB);
            this.map.put("Content", rB);
        } else {
            int contentLength = requestManager.getContentLength(requestBody.toString());
            System.out.println("The contentlength is " + contentLength);
            char[] body = new char[contentLength];
            this.br.read(body);
            rB = new String(body);
            System.out.println("Rget: " + rB);
            String[] rs = rB.split("\n");
            String nameValue = null;
            String message = null;
            String offer = null;
            String answer = null;
            String ice = null;
            String Nullname = null;
            String ready = null;
            for (int n = 0; n < rs.length; ++n) {
                String[] sline = rs[n].split(" ");
                if (sline.length >= 2) {
                    int i;
                    if (sline[0].equals("Name:")) {
                        StringBuilder nameBuilder = new StringBuilder(sline[1]);
                        for (i = 2; i < sline.length; ++i) {
                            nameBuilder.append(" ").append(sline[i]);
                        }
                        nameValue = nameBuilder.toString();
                        continue;
                    }
                    if (sline[0].equals("Message:")) {
                        StringBuilder messageBuilder = new StringBuilder(sline[1]);
                        for (i = 2; i < sline.length; ++i) {
                            messageBuilder.append(" ").append(sline[i]);
                        }
                        message = messageBuilder.toString();
                        continue;
                    }
                    if (sline[0].equals("Offer:")) {
                        StringBuilder offerBuilder = new StringBuilder(sline[1]);
                        for (i = 2; i < sline.length; ++i) {
                            System.out.println("adding for length: " + i);
                            offerBuilder.append(" ").append(sline[i]);
                        }
                        offer = offerBuilder.toString();
                        System.out.println("final offer: " + offer);
                        continue;
                    }
                    if (sline[0].equals("Answer:")) {
                        StringBuilder ansBuilder = new StringBuilder(sline[1]);
                        for (i = 2; i < sline.length; ++i) {
                            ansBuilder.append(" ").append(sline[i]);
                        }
                        answer = ansBuilder.toString();
                        continue;
                    }
                    if (sline[0].equals("ICE:")) {
                        StringBuilder iceBuilder = new StringBuilder(sline[1]);
                        for (i = 2; i < sline.length; ++i) {
                            iceBuilder.append(" ").append(sline[i]);
                        }
                        ice = iceBuilder.toString();
                        continue;
                    }
                    if (sline[0].equals("null:")) {
                        StringBuilder nullBuilder = new StringBuilder(sline[1]);
                        for (i = 2; i < sline.length; ++i) {
                            nullBuilder.append(" ").append(sline[i]);
                        }
                        Nullname = nullBuilder.toString();
                        continue;
                    }
                    if (sline[0].equals("ready:")) {
                        StringBuilder readyBuilder = new StringBuilder(sline[1]);
                        for (i = 2; i < sline.length; ++i) {
                            readyBuilder.append(" ").append(sline[i]);
                        }
                        ready = readyBuilder.toString();
                        continue;
                    }
                    this.map.put(sline[0], sline[1]);
                    continue;
                }
                System.out.println("The content is " + sline[0]);
                this.map.put("Content", sline[0]);
            }
            if (nameValue != null) {
                this.map.put("Name:", nameValue);
            } else if (message != null) {
                this.map.put("Message:", message);
            } else if (offer != null) {
                this.map.put("Offer:", offer);
            } else if (answer != null) {
                this.map.put("Answer:", answer);
            } else if (ice != null) {
                this.map.put("ICE:", ice);
            } else if (Nullname != null) {
                this.map.put("null:", Nullname);
            } else if (ready != null) {
                this.map.put("ready:", ready);
            }
        }
    }

    public void printMap() {
        System.out.println("Printing session map:");
        for (Map.Entry<String, String> entry : this.map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key + " : " + value);
        }
    }

    private static int getContentLength(String request) {
        String[] lines;
        for (String line : lines = request.split("\r\n")) {
            if (!line.startsWith("Content-Length:")) continue;
            return Integer.parseInt(line.substring("Content-Length:".length()).trim());
        }
        return 0;
    }
}

