/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import org.json.JSONObject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class MyWebSocketEndpoint {
    classHandler cH;
    classManipulation c;

    public MyWebSocketEndpoint(classHandler cH) {
        this.cH = cH;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + statusCode + ", " + reason);
        this.c.removeTheSession(session);
        this.broadcastMessage("{\"removeUser\":\"" + this.c.getTheIdFromSession(session) + "\"}");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @OnWebSocketMessage

    public void onMessage(Session session, String message) {
        System.out.println("Received message from websocket: " + message);
        try {
            JSONObject jContent = new JSONObject(message);
            String id;
            
            System.out.println("The content of the websocket connection is " + jContent.toString(2));
            
            for (String key : jContent.keySet()) {
                // String value = jContent.getString(key);
                System.out.println(key + " : " + jContent.get(key));
            }
            
            System.out.println("The name is: " + jContent.optString("name"));
            
            if (!jContent.has("id")) {
                id = UUID.randomUUID().toString();
                this.c = this.cH.getTheClass(jContent.getString("className"));
                if (jContent.has("title") && jContent.getString("title").equals("Lecturer")) {
                    this.c.setLecturerDetails(id, convertMap(jContent.toMap()));
                    this.c.setTheSession(id, session);
                    JSONObject jOb = new JSONObject();
                    jOb.put("id", id);
                    this.sendMessage(session, jOb.toString());
                } else {
                    String attendanceList = this.c.getTheAttendanceList();
                    String chatMessageList = this.c.getAllChatMessages();
                    this.broadcastMessage("{\"newUser\":\"" + jContent.getString("name") + "%" + id + "\"}");
                    this.c.setStudentDetails(id, convertMap(jContent.toMap()));
                    this.c.setTheSession(id, session);
                    JSONObject jOb = new JSONObject();
                    jOb.put("id", id);
                    jOb.put("list", attendanceList);
                    if (chatMessageList != null) {
                        jOb.put("chatlist", chatMessageList);
                        this.sendMessage(session, jOb.toString());
                    } else {
                        this.sendMessage(session, jOb.toString());
                    }
                }
            } else {
                id = jContent.getString("id");
                JSONObject newJOb = new JSONObject();
                JSONObject resJob = new JSONObject();
                for (String key : jContent.keySet()) {
                    String value = jContent.optString(key, String.valueOf(jContent.get(key)));
                    switch (key) {
                        case "type": {
                            handleTypeMessage(jContent, session, id, value);
                            break;
                        }
                        case "chatMessage": {
                            this.c.setChatMessages(id, value);
                            newJOb.put("chatMessage", value);
                            resJob.put("response", "sent message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "streamPerson": {
                            newJOb.put("streamPerson", value);
                            resJob.put("response", "sent streamPerson message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "ready": {
                            newJOb.put("id", id);
                            newJOb.put("ready", value);
                            resJob.put("response", "sent ready message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "closeStream": {
                            newJOb.put("closeStream", value);
                            resJob.put("response", "sent closeStream message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "imgsrc": {
                            newJOb.put("imgsrc", value);
                            resJob.put("response", "sent imgsrc message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "backTheVideo": {
                            newJOb.put("backTheVideo", value);
                            resJob.put("response", "sent backTheVideo message successfully");

                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "keepalive": {
                            resJob.put("response", "sent keepalive message successfully");

                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "endTheClass": {
                            newJOb.put("endTheClass", "sent ending the class message");

                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.c.removeAllResources();
                            this.cH.removeTheClass(jContent.getString("className"));
                            break;
                        }
                        case "visitLink": {
                            newJOb.put("visitLink", value);
                            resJob.put("response", "sent visitlink message successfully");

                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "closeLinkFrame": {
                            newJOb.put("closeLinkFrame", value);
                            resJob.put("response", "sent closeLinkFrame message successfully");

                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "scrolling": {
                            newJOb.put("scrollFrame", value);
                            resJob.put("response", "sent scrollFrame message successfully");

                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "editResourceFrame": {
                            newJOb.put(key,value);
                            resJob.put("response", "sent "+key+" message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "editorText": {
                            newJOb.put(key, value);
                            resJob.put("response", "sent "+key+" message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "userEditPerm": {
                            newJOb.put(key, value);
                            resJob.put("response", "sent "+key+" message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "createNewTabMenu": {
                            newJOb.put(key, value);
                            resJob.put("response", "sent "+key+" message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "activeTabName": {
                            newJOb.put(key, value);
                            resJob.put("response", "sent "+key+" message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "changeToTab": {
                            newJOb.put(key, value);
                            resJob.put("response", "sent "+key+" message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "closeTab": {
                            newJOb.put(key, value);
                            resJob.put("response", "sent "+key+" message successfully");
                            this.broadcastMessageExcept(id, newJOb.toString());
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "saveFileData": {
                            String result = saveFile(jContent.getString("fileName"), value);
                            resJob.put("response", result);
                            this.sendMessage(session, resJob.toString());
                            break;
                        }
                        case "getAllFileResource": {
                            getAllFileResource(value, session);
                            break;
                        }
                        case "getFileContent": {
                            String fileContent = readFileToString(searchFile(value));
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("getFileContent", value);
                            jsonObject.put("content", fileContent);
                            this.broadcastMessage(jsonObject.toString());
                            break;
                        }
                        case "giveHistory": {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(key, "get the history");
                            jsonObject.put("tabObject", jContent.getString("tabObject"));
                            jsonObject.put("activeT", jContent.getString("activeT"));

                            resJob.put("response", "sent " + key + " message successfully");

                            this.sendMessage(this.c.getTheSession(value), jsonObject.toString());
                            this.sendMessage(session, "{\"response\":\"sent " + key + " message successfully\"}");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTypeMessage(JSONObject jContent, Session session, String id, String value) throws IOException {
        String details;
        JSONObject jOb = new JSONObject();
        JSONObject resJOb = new JSONObject();
        switch (value) {
            case "offer":
                jOb.put("type", value);
                jOb.put("sdp", jContent.getString("sdp"));
                jOb.put("name", jContent.getString("name"));
                jOb.put("title", jContent.getString("title"));
                jOb.put("id", id);
                details = jOb.toString();

                resJOb.put("response", "added offer to database");

                this.sendMessage(this.c.getTheSession(jContent.getString("sender")), details);
                this.sendMessage(session, resJOb.toString());
                break;
            case "candidate":
                JSONObject iceL = new JSONObject();
                iceL.put("id", jContent.getString("id"));
                iceL.put("type", value);
                iceL.put("candidate", jContent.getString("candidate"));
                iceL.put("sdpMid", jContent.getString("sdpMid"));
                iceL.put("sdpMLineIndex", jContent.getInt("sdpMLineIndex"));

                // jOb.put("icelist", iceL.toString());
                details = iceL.toString();

                resJOb.put("response", "added offer to database");

                this.sendMessage(this.c.getTheSession(jContent.getString("sender")), details);
                this.sendMessage(session, resJOb.toString());
                break;
            case "answer":
                jOb.put("id", jContent.getString("id"));
                jOb.put("type", value);
                jOb.put("sdp", jContent.getString("sdp"));

                resJOb.put("response", "added answer to database for lecturer");

                details = jOb.toString();
                this.sendMessage(this.c.getTheSession(jContent.getString("sender")), details);
                this.sendMessage(session, resJOb.toString());
                break;
            case "file":
                byte[] decodedData = Base64.getDecoder().decode(jContent.getString("fileData"));
                String fileName = jContent.getString("fileName");
                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                    fos.write(decodedData);
                    fos.flush();
                    System.out.println("File received and saved: " + fileName);
                    this.c.addNewResources(fileName);
                    jOb.put("filetype", jContent.getString("fileType"));
                    jOb.put("filesrc", fileName);
                    this.sendMessage(session, jOb.toString());
                }
                break;
        }
    }

    private void getAllFileResource(String value, Session session) throws IOException {
        // Get the current working directory
        Path currentDirectory = Paths.get("").toAbsolutePath();
        if (!"getAll".equals(value.trim())) {
            currentDirectory = Paths.get(currentDirectory.toString(), value.trim());
        }

        // List all files in the current working directory
        List<Path> fileList;
        try (Stream<Path> paths = Files.list(currentDirectory)) {
            fileList = paths.collect(Collectors.toList());
        }

        // Print the list of files
        System.out.println("Files in the current directory:");
        String totalFiles = "";
        for (Path file : fileList) {
            // Remove the current directory part from the absolute path
            String relativePath = currentDirectory.relativize(file.toAbsolutePath()).toString();
            totalFiles += relativePath + ";";
        }
        JSONObject jOb = new JSONObject();
        jOb.put("getAllFileResource", totalFiles);
        this.sendMessage(session, jOb.toString());
    }
    private static HashMap<String, String> convertMap(Map<String, Object> inputMap) {
        HashMap<String, String> convertedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            convertedMap.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return convertedMap;
    }
    // Method to escape special characters in JSON string
    // private static String escapeJson(String input) {
    //     return input.replace("\\", "\\\\")
    //             .replace("\"", "\\\"")
    //             .replace("\b", "\\b")
    //             .replace("\f", "\\f")
    //             .replace("\n", "\\n")
    //             .replace("\r", "\\r")
    //             .replace("\t", "\\t");
    // }
    private static String searchFile(String value) {
        try {
            // Get the current working directory
            Path currentDirectory = Paths.get("").toAbsolutePath();
            System.out.println("Current Directory: " + currentDirectory);

            // Initialize the file path variable
            Path filePath = null;

            // Search for the file
            try (Stream<Path> paths = Files.walk(currentDirectory)) {
                List<Path> result = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().equals(value.trim()))
                        .collect(Collectors.toList());

                if (!result.isEmpty()) {
                    filePath = result.get(0); // Get the first matching file
                }
            }

            // If the file is found, send the file path
            if (filePath != null) {
                return filePath.toString();
            } else {
                return "File not found";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    private static String readFileToString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
    @SuppressWarnings("unused")
    private static HashMap<String, String> jsonParser(String input) {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if (input != null) {
            @SuppressWarnings("unused")
            String[] firstResult;
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
        }
        return resultMap;
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getRemote().sendString(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String message) {
        Set<Session> sessions = this.c.getEverySession();
        for (Session session : sessions) {
            this.sendMessage(session, message);
        }
    }

    private void broadcastMessageExcept(String id, String message) {
        Set<Session> sessions = this.c.getEverySession();
        Session except = this.c.getTheSession(id);
        for (Session session : sessions) {
            if (session == except) continue;
            this.sendMessage(session, message);
        }
    }

    private static String saveFile(String fileName, String content) {
        try {
            // Create the file and write the content to it, truncating if it exists
            Files.write(Paths.get(fileName), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return "File saved successfully: " + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to save the file: " + fileName;
        }
    }
}

