/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import com.arabiclearner.classManipulation2;
import com.arabiclearner.longPollingManager;
import com.arabiclearner.requestManager;
import com.arabiclearner.responseManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class longPollingHandler
extends Thread {
    private responseManager resM;
    private requestManager rm;
    private classManipulation2 c;

    longPollingHandler(requestManager rm, responseManager resM, longPollingManager l, classManipulation2 c) throws IOException, InterruptedException {
        this.resM = resM;
        this.rm = rm;
        this.c = c;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        try {
            String id;
            String content = this.rm.getTheContent();
            System.out.println("The content of the longpolling connection is " + content);
            HashMap<String, String> jContent = longPollingHandler.jsonParser(content);
            for (Map.Entry<String, String> entry : jContent.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                System.out.println(key + " : " + value);
            }
            System.out.println("The name is: " + jContent.get("name"));
            if (!jContent.containsKey("id")) {
                id = UUID.randomUUID().toString();
                if (jContent.get("title").equals("Lecturer")) {
                    this.c.setLecturerDetails(id, jContent);
                    if (this.c.getMessage(id) == null) {
                        this.resM.sendAsMessage("{\"id\":\"" + id + "\", \"noMessage\":\"noMessage\"}");
                    } else {
                        this.resM.sendAsMessage("{\"id\":\"" + id + "\", " + this.c.getMessage(id) + "}");
                        this.c.removeMessage(id);
                    }
                } else {
                    String offer = this.c.getLecOffer();
                    String ice = this.c.getTheIce(this.c.getTheLecId());
                    String attendancelist = this.c.getTheAttendanceList();
                    String chatMessageList = this.c.getAllChatMessages();
                    this.c.sendMessageToEveryone("\"newUser\":\"" + jContent.get("name") + "\"");
                    this.c.setStudentDetails(id, jContent);
                    if (offer != null) {
                        if (ice != null) {
                            this.resM.sendAsMessage("{\"id\":\"" + id + "\", " + offer + ", \"icelist\":[" + ice + "], \"list\":\"" + attendancelist + "\"}");
                        } else {
                            this.resM.sendAsMessage("{\"id\":\"" + id + "\", " + offer + ", \"list\":\"" + attendancelist + "\"}");
                        }
                    } else if (chatMessageList != null) {
                        this.resM.sendAsMessage("{\"id\":\"" + id + "\", \"list\":\"" + attendancelist + "\", \"chatlist\":\"" + chatMessageList + "\"}");
                    } else {
                        this.resM.sendAsMessage("{\"id\":\"" + id + "\", \"list\":\"" + attendancelist + "\"}");
                    }
                }
            } else {
                id = jContent.get("id");
                for (Map.Entry<String, String> entry : jContent.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    switch (key) {
                        case "getMessage": {
                            System.out.println("recieved a getMessage");
                            if (this.c.getMessage(id) == null) {
                                this.resM.sendAsMessage("{\"noMessage\":\"noMessage\"}");
                                break;
                            }
                            this.resM.sendAsMessage("{" + this.c.getMessage(id) + "}");
                            this.c.removeMessage(id);
                            break;
                        }
                        case "type": {
                            String details;
                            if (value.equals("offer")) {
                                details = "\"type\":\"" + value + "\",\"sdp\":\"" + jContent.get("sdp") + "\"";
                                this.c.setMessage(jContent.get("sender"), details);
                                this.resM.sendAsMessage("{\"response\":\"added offer to database\"}");
                                break;
                            }
                            if (value.equals("candidate")) {
                                details = "\"icelist\":[{\"type\":\"" + value + "\",\"candidate\":\"" + jContent.get("candidate") + "\",\"sdpMid\":\"" + jContent.get("sdpMid") + "\", \"sdpMLineIndex\":\"" + jContent.get("sdpMLineIndex") + "\"}]";
                                if (id.equals(this.c.getTheLecId())) {
                                    this.c.setMessage(jContent.get("sender"), details);
                                    this.resM.sendAsMessage("{\"response\":\"added ice to database\"}");
                                    break;
                                }
                                this.c.setMessage(this.c.getTheLecId(), details);
                                this.resM.sendAsMessage("{\"response\":\"added ice to database for lecturer\"}");
                                break;
                            }
                            if (!value.equals("answer")) break;
                            details = "\"type\":\"" + value + "\",\"sdp\":\"" + jContent.get("sdp") + "\"";
                            this.c.setMessage(this.c.getTheLecId(), details);
                            this.resM.sendAsMessage("{\"response\":\"added answer to database for lecturer\"}");
                            break;
                        }
                        case "chatMessage": {
                            this.c.setChatMessages(id, "\"chatMessage\":\"" + value + "\"");
                            this.resM.sendAsMessage("{\"response\":\"sent message successfully\"}");
                            break;
                        }
                        case "getice": {
                            String iceDetails = this.c.getTheIce(this.c.getTheLecId());
                            this.resM.sendAsMessage("{" + iceDetails + "}");
                            break;
                        }
                        case "ready": {
                            this.c.setMessage(this.c.getTheLecId(), "\"ready\":\"" + value + "\"");
                            this.resM.sendAsMessage("{\"response\":\"sent ready message successfully\"}");
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                this.resM.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static HashMap<String, String> jsonParser(String input) {
        String[] firstResult;
        HashMap<String, String> resultMap = new HashMap<String, String>();
        for (String data : firstResult = input.substring(1, input.length() - 1).split(",")) {
            String[] dataFirst = data.split(":");
            if (dataFirst.length < 2) continue;
            String key = dataFirst[0].substring(1, dataFirst[0].length() - 1).trim();
            StringBuilder databuilder = new StringBuilder(dataFirst[1].trim());
            for (int i = 2; i < dataFirst.length; ++i) {
                databuilder.append(":").append(dataFirst[i].trim());
            }
            if (databuilder.toString().length() >= 2) {
                resultMap.put(key, databuilder.toString().substring(1, databuilder.toString().length() - 1));
                continue;
            }
            resultMap.put(key, dataFirst[1]);
        }
        return resultMap;
    }
}

