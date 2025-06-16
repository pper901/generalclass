/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import org.json.JSONObject;


public class clientHandler
extends Thread {
    Socket s;
    DataInputStream inp;
    DataOutputStream outp;
    PrintWriter pw;
    sessionManager sm;
    classHandler cH;
    longPollingManager w;
    requestManager rM;
    responseManager resM;
    HashMap<String, Object> website;
    // Start timer
    long startTime;
    boolean isClosed = false;

    clientHandler(Socket s, DataInputStream inp, DataOutputStream outp, classHandler cH, HashMap<String, Object> websites, long startTime) throws IOException {
        this.s = s;
        this.inp = inp;
        this.outp = outp;
        this.sm = new sessionManager();
        this.cH = cH;
        this.rM = new requestManager(inp);
        this.resM = new responseManager(outp);
        this.website = websites; 
        this.startTime = startTime;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        block60: {
            try {

                String meth = this.rM.getTheMethod();
                String address = this.rM.getTheAddress();
                System.out.println(address);
                if (!address.equals("/upload")) {
                    if (meth.equals("GET")) {
                        if (address.equals("/index.html")) {
                            this.resM.sendAsHtml((String)this.website.get("index"));
                        } else if (address.equals("/")) {
                            this.resM.sendAsHtml((String)this.website.get("home"));
                        } else if (address.equals("/google4e383da5d2ff81b8.html")) {
                            this.resM.sendAsHtml((String)this.website.get("google"));
                        } else if (address.equals("/ads.txt")) {
                            this.resM.sendAsHtml((String)this.website.get("ads"));
                        } else if (address.equals("/stu-index.html")) {
                            this.resM.sendAsHtml((String)this.website.get("stu-index"));
                        } else if (address.equals("/style.css")) {
                            this.resM.sendAsCss((String)this.website.get("style"));
                        } else if (address.equals("/engine.js")) {
                            this.resM.sendAsJavascript((String)this.website.get("engine"));
                        } else if (address.equals("/adapter-latest.js")) {
                            this.resM.sendAsJavascript((String)this.website.get("adapter"));
                        } else if (address.equals("/lengine.js")) {
                            this.resM.sendAsJavascript((String)this.website.get("lengine"));
                        } else if(address.equals("/getAllClasses")){
                            System.out.println("Getting all classes");
                            JSONObject classes = new JSONObject();
                            String [] classnames = this.cH.getAllClassNames();
                            if(classnames.length >= 1){
                                classes.put("allclasses", this.cH.getAllClassNames());
                            }else{
                                classes.put("noclasses", this.cH.getAllClassNames());
                            }
                            this.resM.sendAsJson(classes.toString());
                        }else if (address.equals("/class.html")) {
                            String cookie = this.rM.getTheCookie();
                            if (cookie != null) {
                                System.out.println("Cookie is not null");
                                this.sm.readMapFromFile();
                                String sessionName = this.sm.getSessionContent(cookie);
                                String[] sessionItem = sessionName.split(",");
                                classManipulation c = this.cH.getTheClass(sessionItem[1]);
                                if (sessionItem[0].equals(c.getTheLecturer())) {
                                    this.resM.sendAsHtml(c.getTheLecClass());
                                } else if (sessionName.equals("Session Not Found")) {
                                    this.resM.sendRedirectCode(null, "/index.html");
                                } else {
                                    this.resM.sendAsHtml(c.setTheHeader(sessionItem[0]));
                                }
                            } else {
                                System.out.println("Cookie is null");
                                this.resM.sendRedirectCode(null, "/stu-index.html");
                            }
                        } else if (this.website.containsKey(address.substring(1))) {
                            this.resM.sendAsByteFile((byte[])this.website.get(address.substring(1)), address);
                        } else {
                            // Decode the URL-encoded address
                            String decodedAddress = URLDecoder.decode(address.substring(1), StandardCharsets.UTF_8.name());

                            File file = new File(decodedAddress);
                            if (file.exists()) {
                                System.out.println("The file " + address+ " exists in the home directory.");
                                String exten = address.split("\\.")[1].toLowerCase();
                                System.out.println("value for extenstion is"+exten);
                                switch (exten) {
                                    case "html":
                                        this.resM.sendAsHtml(readFileToString(decodedAddress));
                                        break;
                                    case "css":
                                        this.resM.sendAsCss(readFileToString(decodedAddress));
                                        break;
                                    case "js":
                                        this.resM.sendAsJavascript(readFileToString(decodedAddress));
                                        break;
                                    default:
                                        this.resM.sendAsByteFile(readFileToByteArray(decodedAddress), address);
                                        break;
                                }
                            }else{
                                System.out.println("The file " +address+ " doeas not exists in the home directory.");
                                this.outp.writeBytes("HTTP/1.1 404 Not Found");
                                this.outp.flush();
                            }
                        }
                        // try {
                        //     System.out.print("Closing Connection for " + this.rM.getTheAddress());
                        //     // End timer
                        //     long endTime = System.nanoTime();
                        //     long duration = endTime - this.startTime; // duration in nanoseconds
                        //     double durationInMillis = duration / 1_000_000.0; // convert to milliseconds
            
                        //     System.out.println("Request processing time for "+this.rM.getTheAddress()+" : " + durationInMillis + " ms");
                        //     this.s.close();
                        // }
                        // catch (IOException e) {
                        //     e.printStackTrace();
                        // }
                        break block60;
                    }
                    if (!meth.equals("POST")) break block60;
                    System.out.println("The content of POST method is: " + this.rM.getTheContent());
                    HashMap<String, String> mp = clientHandler.getContent(this.rM.getTheContent());
                    if (mp.containsKey("startClass") || mp.containsKey("appStartClass")) {
                        System.out.println("inside Class Get the name of lecturer: " + mp.get("lecName"));
                        System.out.println("inside Class Get the name of class: " + mp.get("className"));
                        String lecName = mp.get("lecName");
                        String className = mp.get("className");
                        this.cH.setTheClass(className, new classManipulation("class.html"));
                        classManipulation c = this.cH.getTheClass(className);
                        c.setTheClassName(className);
                        c.setTheLecturer(lecName);
                        String id = this.sm.getSessionId();
                        this.sm.addSession(id, lecName + "," + className);
                        this.sm.writeMapToFile();
                        if(mp.containsKey("startClass")){
                            this.resM.sendRedirectCode(c.getTheLecClass(), "/class.html", id);
                        }else{
                            this.resM.sendAsMessage("StartClass:Granted");
                        }
                        break block60;
                    }
                    if (mp.containsKey("joinClass") || mp.containsKey("appJoinClass")) {
                        System.out.println("inside Class Get the name of student:" + mp.get("stuName"));
                        String stuName = mp.get("stuName");
                        String className = mp.get("className");
                        classManipulation c1 = this.cH.getTheClass(className);
                        if (stuName != null & className != null) {
                            c1.setStudent(stuName);
                            String id = this.sm.getSessionId();
                            this.sm.addSession(id, stuName + "," + className);
                            this.sm.writeMapToFile();
                            if(mp.containsKey("joinClass")){
                                this.resM.sendRedirectCode(c1.setTheHeader(stuName), "/class.html", id);
                            }else{
                                this.resM.sendAsMessage("joinClass:Granted");
                            }
                        } else {
                            this.resM.sendRedirectCode("Some fields are missing or class hasnt started", "stu-index.html");
                        }
                        break block60;
                    }
                    if (!mp.containsKey("chatMessage")) break block60;
                    System.out.println("Found a message");
                    try {
                        this.w.acquireLock();
                        this.w.sendMessageToAllClient("Message: " + mp.get("chatMessage"));
                        break block60;
                    }
                    finally {
                        this.w.unlock();
                    }
                }
                System.out.println("The content of POST method is: " + this.rM.getTheContent());
                HashMap<String, String> jParser = clientHandler.jsonParser(this.rM.getTheContent());
                String base64Image = jParser.get("data");
                System.out.println("Content of fileData is : " + base64Image);
                byte[] decodedData = Base64.getDecoder().decode(base64Image);
                String fileName = jParser.get("fileName");
                System.out.println("The filenme is " + fileName);
                try (FileOutputStream fos = new FileOutputStream(fileName);){
                    fos.write(decodedData);
                    fos.flush();
                    System.out.println("File recieved and saved: " + fileName);
                    this.resM.sendAsMessage("{\"file\":\"" + fileName + "\", \"filetype\": \"" + jParser.get("fileType") + "\"}");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                 // End timer
                long endTime = System.nanoTime();
                long duration = endTime - this.startTime; // duration in nanoseconds
                double durationInMillis = duration / 1_000_000.0; // convert to milliseconds

                System.out.println("Request processing time for "+address+" : " + durationInMillis + " ms");

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (!this.rM.getTheAddress().equals("/")) {
                    try {
                        System.out.print("Closing Connection for " + this.rM.getTheAddress());
                        // End timer
                        long endTime = System.nanoTime();
                        long duration = endTime - this.startTime; // duration in nanoseconds
                        double durationInMillis = duration / 1_000_000.0; // convert to milliseconds
        
                        System.out.println("Request processing time for "+this.rM.getTheAddress()+" : " + durationInMillis + " ms");
                        this.s.close();
                        this.isClosed = true;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean clientClosed(){
        return isClosed;
    }
    public static byte[] readFileToByteArray(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }
    public static String readFileToString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
    private static HashMap<String, String> getContent(String map) {
        System.out.println("The map value is " + map);
        HashMap<String, String> content = new HashMap<>();

        String[] svalue = map.split("&");
        for (String pair : svalue) {
            String[] tvalue = pair.split("=", 2);

            if (tvalue.length == 2) {
                String key = tvalue[0];
                String value = tvalue[1].replace("+", " ");
                content.put(key, value);
            } else {
                // Handle cases where there's no '=' in the pair
                content.put(tvalue[0], "");
            }
        }
        return content;
    }

    private static HashMap<String, String> jsonParser(String input) {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if (input != null) {
            String[] firstResult = input.substring(1, input.length() - 1).split(",");
            for (int j = 0; j < firstResult.length; ++j) {
                String[] dataFirst = firstResult[j].split(":");
                if (dataFirst.length < 2) continue;
                String key = dataFirst[0].substring(1, dataFirst[0].length() - 1).trim();
                StringBuilder databuilder = new StringBuilder(dataFirst[1].trim());
                for (int i = 2; i < dataFirst.length; ++i) {
                    databuilder.append(":").append(dataFirst[i].trim());
                }
                if (key.equals("fileData")) {
                    databuilder.append(",").append(firstResult[j + 1]);
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
}

