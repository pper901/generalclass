/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jetty.websocket.api.Session;

public class classManipulation {
    String theClassString;
    String theLecClassString;
    String theHeader;
    String lecturer;
    private String lectId;
    Map<String, Object>          attendance = new HashMap<String, Object>();
    Map<String, Session> sessMan = new HashMap<String, Session>();
    ArrayList<String> chatlist = new ArrayList();
    ArrayList<String> students = new ArrayList();
    private ArrayList<String> resources = new ArrayList();
    private String className;

    classManipulation(String resourceName) throws IOException {
        try (InputStream inputStream = classManipulation.class.getResourceAsStream(resourceName);
             Scanner scanner = new Scanner(inputStream);){
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
                stringBuilder.append(line).append("\n");
            }
            this.theClassString = stringBuilder.toString();
        }
    }

    public void setTheGenClassString(String s) {
        this.theClassString = s;
    }

    public void setStudent(String s) {
        this.students.add(s);
    }

    public String getTheGenClassString() {
        return this.theClassString;
    }

    public void setTheLecturer(String s) {
        System.out.println("Setting the lecturer's name: " + s);
        this.lecturer = s;
    }

    public String getTheLecturer() {
        return this.lecturer;
    }

    public void setTheLecClass() throws IOException {
        String resourceName = "lclass.html";
        try (InputStream inputStream = this.getClass().getResourceAsStream(resourceName);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
            if (inputStream != null) {
                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println(outputStream.toString("UTF-8"));
                this.theLecClassString = outputStream.toString("UTF-8");
            } else {
                this.createResource(resourceName);
            }
        }
    }

    private void createResource(String resourceName) throws IOException {
        String directoryPath = "/com/arabiclearner/";
        String absolutePath = directoryPath + resourceName;
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created successfully.");
                try (FileOutputStream outputStream = new FileOutputStream(new File(absolutePath));){
                    String defaultContent = "<!doctype html>\r\n<html>\r\n<head>\r\n<meta charset=\"utf-8\">\r\n<title>Class Page</title>\r\n<link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\">\r\n</head>\r\n\r\n<body>\r\n\t<header></header>\r\n<div class=\"main-panel\">\r\n        \t<div class=\"f-panel\">\r\n            \t<div class=\"f-item\" id=\"preview\"></div>\r\n                <div class=\"s-item\" id=\"preview-control\"></div>\r\n            </div>\r\n            <div class=\"s-panel\" id=\"targetDiv1\">\r\n            \t<div class=\"t-item\"></div>\r\n            </div>\r\n            <div class=\"t-panel\">\r\n            \t<div class=\"fo-item\" id=\"chat\">\r\n                </div>\r\n                <div class=\"fi-item\">\r\n                \t<!-- <form action=\"/class.html\" method=\"post\"> -->\r\n                    \t<input type=\"text\" class=\"chat\" name=\"chatMessage\" id=\"message\">\r\n                        <input type=\"submit\" class=\"chat-btn\" name=\"chatBtn\" value=\"Send\" onclick=\"setMessage()\">\r\n                    <!-- </form> -->\r\n                </div>\r\n            </div>\r\n        </div>\r\n    <footer>&copy; arabiclearner.net</footer>\r\n</body>\r\n</html>\r\n<script src=\"lengine.js\"></script>\r\n<script src=\"adapter-latest.js\"></script>\r\n";
                    ((OutputStream)outputStream).write(defaultContent.getBytes("UTF-8"));
                    this.theLecClassString = defaultContent;
                }
            } else {
                System.out.println("Failed to create directory.");
                return;
            }
        }
    }

    public String getTheLecClass() throws IOException {
        return this.setTheLecHeader(this.getTheLecturer());
    }

    public String setTheHeader(String s) {
        this.theHeader = s;
        String headerClass = "<h4 id=\"name\">" + this.theHeader + "</h4>";
        String classN = "<h1 id=\"classTitle\">" + this.className + "</h1>";
        StringBuilder theBuilder = new StringBuilder(this.getTheGenClassString());
        String headerStartTag = "<header>";
        String headerEndTag = "</header>";
        int headerIndex = theBuilder.indexOf(headerStartTag);
        int endHeaderIndex = theBuilder.indexOf(headerEndTag) + headerEndTag.length();
        if (headerIndex != -1 && endHeaderIndex != -1) {
            theBuilder.replace(headerIndex, endHeaderIndex, headerStartTag + headerClass + classN + headerEndTag);
        }
        return theBuilder.toString();
    }

    public String setTheLecHeader(String s) throws IOException {
        this.theHeader = s;
        String headerClass = "<h4 id=\"name\">" + this.theHeader + "</h4>";
        String classN = "<h1 id=\"classTitle\">" + this.className + "</h1>";
        this.setTheLecClass();
        StringBuilder theBuilder = new StringBuilder(this.theLecClassString);
        String headerStartTag = "<header>";
        String headerEndTag = "</header>";
        int headerIndex = theBuilder.indexOf(headerStartTag);
        int endHeaderIndex = theBuilder.indexOf(headerEndTag) + headerEndTag.length();
        if (headerIndex != -1 && endHeaderIndex != -1) {
            theBuilder.replace(headerIndex, endHeaderIndex, headerStartTag + headerClass + classN + headerEndTag);
        }
        return theBuilder.toString();
    }

    public String getTheHeader(String s) {
        String content = classManipulation.extractContent(s, "<header>", "</header>");
        return content;
    }

    public static String extractContent(String input, String startTag, String endTag) {
        String patternString = Pattern.quote(startTag) + "(.*?)" + Pattern.quote(endTag);
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public synchronized void addStudent(String s) {
        StringBuilder theBuilder = new StringBuilder(this.getTheGenClassString());
        String divStartTag = "<div class=\"s-panel\">";
        String divEndTag = "<div class=\"t-item\">";
        int divIndex = theBuilder.indexOf(divStartTag);
        int endDivIndex = theBuilder.indexOf(divEndTag) + divEndTag.length();
        if (divIndex != -1 && endDivIndex != -1) {
            theBuilder.replace(divIndex, endDivIndex, divStartTag + divEndTag + s + "</div>" + divEndTag);
        }
        this.setTheGenClassString(theBuilder.toString());
    }

    public synchronized void removeStudent(String s) {
        StringBuilder theBuilder = new StringBuilder(this.getTheGenClassString());
        String divETag = "<div class=\"t-item\">";
        String divStartTag = divETag + s;
        String divEndTag = "</div>";
        int divIndex = theBuilder.indexOf(divStartTag);
        int endDivIndex = theBuilder.indexOf(divEndTag, divIndex) + divEndTag.length();
        if (divIndex != -1 && endDivIndex != -1) {
            theBuilder.replace(divIndex, endDivIndex, "");
            this.setTheGenClassString(theBuilder.toString());
        }
    }

    public synchronized void sendChatMessage(String s, String name) {
        String divStTag;
        StringBuilder theBuilder = new StringBuilder(this.getTheGenClassString());
        int divIndex = theBuilder.indexOf(divStTag = "</div><div class=\"fi-item\">");
        if (divIndex != -1) {
            theBuilder.insert(divIndex, "<p>" + name + ":" + s + "</p>");
            this.setTheGenClassString(theBuilder.toString());
        }
    }

    public void setLecturerDetails(String id, HashMap<String, String> jContent) {
        this.lectId = id;
        this.attendance.put(id, jContent);
    }

    public String getTheLecId() {
        return this.lectId;
    }

    public Object getTheContentOfId(String id) {
        return this.attendance.get(id);
    }

    public void updateContentOfId(String id, String key, String value) {
        Object contentObject = this.attendance.get(id);
        if (contentObject != null) {
            if (contentObject instanceof HashMap) {
                HashMap contentMap = (HashMap)contentObject;
                contentMap.put(key, value);
            } else {
                System.out.println("Content for ID " + id + " is not a HashMap<String, String>");
            }
        }
    }

    public void setStudentDetails(String id, HashMap<String, String> jContent) {
        this.attendance.put(id, jContent);
    }

    public String getMessage(String id) {
        System.out.println("Getting message for: " + id);
        Object contentObject = this.attendance.get(id);
        if (contentObject != null && contentObject instanceof HashMap) {
            HashMap contentMap = (HashMap)contentObject;
            int i = 1;
            if (contentMap.containsKey("message")) {
                System.out.println("The message is " + (String)contentMap.get("message"));
                StringBuilder sb = new StringBuilder((String)contentMap.get("message"));
                while (contentMap.containsKey("message" + i)) {
                    sb.append(",").append((String)contentMap.get("message" + i));
                    ++i;
                }
                System.out.println("The message before sending is:" + sb.toString());
                return sb.toString();
            }
        }
        return null;
    }

    public void setMessage(String id, String mess) {
        Object contentObject = this.attendance.get(id);
        if (contentObject != null) {
            System.out.println("contentObject is not null");
            if (contentObject instanceof HashMap) {
                HashMap contentMap = (HashMap)contentObject;
                if (!contentMap.containsKey("message")) {
                    System.out.println("Sending message" + mess + " to " + id);
                    contentMap.put("message", mess);
                } else {
                    int i = 1;
                    while (contentMap.containsKey("message" + i)) {
                        ++i;
                    }
                    contentMap.put("message" + i, mess);
                }
            }
        }
    }

    public void removeMessage(String id) {
        Object contentObject = this.attendance.get(id);
        if (contentObject != null && contentObject instanceof HashMap) {
            HashMap contentMap = (HashMap)contentObject;
            int i = 1;
            if (contentMap.containsKey("message")) {
                contentMap.remove("message");
                while (contentMap.containsKey("message" + i)) {
                    contentMap.remove("message" + i);
                    ++i;
                }
            }
        }
    }

    public void setLecOffer(String offer) {
        HashMap contentMap;
        Object contentObject = this.attendance.get(this.getTheLecId());
        if (contentObject != null && contentObject instanceof HashMap && !(contentMap = (HashMap)contentObject).containsKey("offer")) {
            contentMap.put("offer", offer);
        }
    }

    public String getLecOffer() {
        HashMap contentMap;
        Object contentObject = this.attendance.get(this.getTheLecId());
        if (contentObject != null && contentObject instanceof HashMap && (contentMap = (HashMap)contentObject).containsKey("offer")) {
            System.out.println("Getting the lecturer's offer");
            return (String)contentMap.get("offer");
        }
        return null;
    }

    public void setTheIce(String id, String ice) {
        Object contentObject = this.attendance.get(id);
        if (contentObject != null && contentObject instanceof HashMap) {
            HashMap contentMap = (HashMap)contentObject;
            int i = 1;
            if (contentMap.containsKey("ice")) {
                while (contentMap.containsKey("ice" + i)) {
                    ++i;
                }
                contentMap.put("ice" + i, ice);
            } else {
                contentMap.put("ice", ice);
            }
        }
    }

    public String getTheIce(String id) {
        System.out.println("Getting the ice");
        Object contentObject = this.attendance.get(id);
        if (contentObject != null && contentObject instanceof HashMap) {
            HashMap contentMap = (HashMap)contentObject;
            int i = 1;
            if (contentMap.containsKey("ice")) {
                StringBuilder sb = new StringBuilder((String)contentMap.get("ice"));
                while (contentMap.containsKey("ice" + i)) {
                    sb.append("},{").append((String)contentMap.get("ice" + i));
                    ++i;
                }
                return "{" + sb.toString() + "}";
            }
        }
        return null;
    }

    public String getTheAttendanceList() {
        if (this.attendance != null) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : this.attendance.entrySet()) {
                HashMap value;
                String name;
                Object valueObject = entry.getValue();
                if (!(valueObject instanceof HashMap) || (name = (String)(value = (HashMap)valueObject).get("name")) == null) continue;
                sb.append(name).append(",");
            }
            if (sb.length() > 0) {
                return sb.substring(0, sb.length() - 1);
            }
        }
        return null;
    }

    public String getTheId(String name) {
        if (this.attendance != null) {
            for (Map.Entry<String, Object> entry : this.attendance.entrySet()) {
                HashMap value;
                String name1;
                Object valueObject = entry.getValue();
                String valueKey = entry.getKey();
                if (!(valueObject instanceof HashMap) || (name1 = (String)(value = (HashMap)valueObject).get("name")) == null || !name1.equalsIgnoreCase(name)) continue;
                return valueKey;
            }
        }
        return null;
    }

    public void sendMessageToEveryone(String mess) {
        if (this.attendance != null) {
            System.out.println("Sending message to everyone" + mess);
            for (Map.Entry<String, Object> entry : this.attendance.entrySet()) {
                String key = entry.getKey();
                System.out.println("Sending message to " + key);
                this.setMessage(key, mess);
            }
        }
    }

    public void sendMessageToEveryoneExcept(String id, String mess) {
        if (this.attendance != null) {
            System.out.println("Sending message to everyone except" + id);
            for (Map.Entry<String, Object> entry : this.attendance.entrySet()) {
                String key = entry.getKey();
                System.out.println("Sending message to " + key);
                if (key.equals(id)) continue;
                this.setMessage(key, mess);
            }
        }
        System.out.println("The messae is " + mess);
    }

    public void setChatMessages(String id, String message) {
        this.chatlist.add(message);
    }

    public String getAllChatMessages() {
        if (this.chatlist != null) {
            StringBuilder sb = new StringBuilder();
            for (String chat : this.chatlist) {
                sb.append(chat).append(",");
            }
            if (sb.length() > 0) {
                return sb.toString().substring(0, sb.length() - 1);
            }
        }
        return null;
    }

    public void setTheSession(String id, Session session) {
        this.sessMan.put(id, session);
    }

    public Session getTheSession(String id) {
        return this.sessMan.get(id);
    }

    public String getTheIdFromSession(Session session) {
        for (Map.Entry<String, Session> entry : this.sessMan.entrySet()) {
            if (!entry.getValue().equals(session)) continue;
            return entry.getKey();
        }
        return null;
    }

    public void removeTheSession(Session id) {
        for (Map.Entry<String, Session> entry : this.sessMan.entrySet()) {
            if (!entry.getValue().equals(id)) continue;
            this.sessMan.remove(entry.getKey());
            this.attendance.remove(entry.getKey());
        }
    }

    public Set<Session> getEverySession() {
        HashSet<Session> sessions = new HashSet<Session>();
        if (this.sessMan != null) {
            for (Map.Entry<String, Session> entry : this.sessMan.entrySet()) {
                sessions.add(entry.getValue());
            }
        }
        return sessions;
    }

    public void setTheClassName(String className) {
        this.className = className;
    }

    public void addNewResources(String fileName) {
        this.resources.add(fileName);
    }

    public void removeAllResources() {
        for (String resourceName : this.resources) {
            File resourceFile = new File(resourceName);
            if (resourceFile.exists() && resourceFile.isFile()) {
                boolean deleted = resourceFile.delete();
                if (deleted) {
                    System.out.println("File deleted: " + resourceFile.getName());
                    continue;
                }
                System.err.println("Failed to delete file: " + resourceFile.getName());
                continue;
            }
            System.err.println("Resource file not found: " + resourceFile.getName());
        }
    }
}

