/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import com.arabiclearner.sessionManager;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class getRequestHandler {
    DataOutputStream out;
    BufferedReader rd;
    sessionManager sm;
    PrintWriter pw;

    getRequestHandler(BufferedReader rd, DataOutputStream out, sessionManager sm) throws IOException {
        this.out = out;
        this.rd = rd;
        this.sm = sm;
        HashMap<String, String> mp = this.getmapMaker(rd);
        this.printCookieContent(mp);
    }

    private synchronized HashMap<String, String> getmapMaker(BufferedReader rd) throws IOException {
        this.pw = new PrintWriter(this.out, true);
        HashMap<String, String> map = new HashMap<String, String>();
        int contentLength = 40;
        String line = rd.readLine();
        if (line != null) {
            String[] rs;
            int bytesRead;
            StringBuilder requestBody = new StringBuilder();
            int totalRead = 0;
            char[] buffer = new char[1024];
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
            }
            while (totalRead < contentLength && (bytesRead = rd.read(buffer, 0, Math.max(buffer.length, contentLength - totalRead))) != -1) {
                requestBody.append(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }
            String rB = requestBody.toString();
            System.out.println("Rget: " + rB);
            for (String nline : rs = rB.split("\n")) {
                String[] sline = nline.split(" ");
                if (sline.length >= 2) {
                    map.put(sline[0], sline[1]);
                    continue;
                }
                map.put("Content", sline[0]);
            }
        }
        rd.close();
        return map;
    }

    public void printCookieContent(HashMap<String, String> map) {
        if (map.containsKey("Cookie:")) {
            String mp = map.get("Cookie:");
            System.out.println("mp Here is:" + mp);
            if (mp != null) {
                this.pw.println("HTTP/1.1 200 Ok\r\nContent-Type: text/html\r\n");
                this.pw.println(this.sm.getSessionContent(mp));
                System.out.println("I Reached Here line 58 getRequestHandler...");
                System.out.println("mp below is: " + mp);
                System.out.println("SessionContentGet" + this.sm.getSessionContent(mp));
            } else {
                System.out.println("Cookie is Empty!");
            }
        }
    }
}

