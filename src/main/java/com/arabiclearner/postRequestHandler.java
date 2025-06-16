/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import com.arabiclearner.classManipulation;
import com.arabiclearner.sessionManager;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class postRequestHandler {
    DataOutputStream out;
    BufferedReader rd;
    sessionManager sm;

    postRequestHandler(BufferedReader rd, DataOutputStream out, sessionManager sm) throws IOException {
        this.out = out;
        this.rd = rd;
        this.sm = sm;
        HashMap<String, String> mp = this.mapMaker(rd);
    }

    private synchronized HashMap<String, String> mapMaker(BufferedReader rd) throws IOException {
        String line;
        PrintWriter pw = new PrintWriter(this.out, true);
        HashMap<String, String> map = new HashMap<String, String>();
        while ((line = rd.readLine()) != null) {
            HashMap<String, String> mp;
            String[] rs;
            int bytesRead;
            StringBuilder requestBody = new StringBuilder();
            char[] buffer = new char[1024];
            int contentLength = postRequestHandler.getContentLength(rd);
            for (int totalRead = 0; totalRead < contentLength && (bytesRead = rd.read(buffer, 0, Math.max(buffer.length, contentLength - totalRead))) != -1; totalRead += bytesRead) {
                requestBody.append(buffer, 0, bytesRead);
            }
            String rB = requestBody.toString();
            System.out.println("RB: " + rB);
            for (String nline : rs = rB.split("\n")) {
                String[] sline = nline.split(" ");
                if (sline.length >= 2) {
                    map.put(sline[0], sline[1]);
                    continue;
                }
                map.put("Content", sline[0]);
            }
            if (!map.containsKey("Content") || !(mp = postRequestHandler.getContent(map.get("Content"))).containsKey("startClass")) continue;
            String md = this.sm.getSessionId();
            if (md == null) break;
            classManipulation cm = new classManipulation("class.html");
            this.sm.addSession(md, cm.setTheHeader("Welcome Lecturer"));
            pw.println("HTTP/1.1 302 Found\r\nContent-Type: text/html\r\nLocation: /class.html\r\nSet-Cookie:" + md);
            pw.println(this.sm.getSessionContent(md));
            System.out.println("SessionContent" + this.sm.getSessionContent(md));
            System.out.println("I Reached Here line 59 postRequestHandler...");
            break;
        }
        rd.close();
        return map;
    }

    private static int getContentLength(BufferedReader rd) throws IOException {
        String line;
        while ((line = rd.readLine()) != null) {
            if (!line.startsWith("Content-Length:")) continue;
            return Integer.parseInt(line.split(": ")[1]);
        }
        return -1;
    }

    private static HashMap<String, String> getContent(String map) {
        HashMap<String, String> cntent = new HashMap<String, String>();
        String[] svalue = map.split("&");
        for (int i = 0; i < svalue.length; ++i) {
            String[] tvalue = svalue[i].split("=");
            cntent.put(tvalue[0], tvalue[1]);
        }
        return cntent;
    }

    private synchronized void ReadtoOut(String srcF, PrintWriter pw) throws IOException {
        int bytesRead;
        File newFile = new File(srcF);
        FileInputStream newFileIn = new FileInputStream(newFile);
        byte[] buffer = new byte[(int)newFile.length()];
        while ((bytesRead = newFileIn.read(buffer)) != -1) {
            System.out.write(buffer, 0, bytesRead);
            String st = new String(buffer);
            pw.println(st);
        }
        pw.close();
        newFileIn.close();
    }
}

