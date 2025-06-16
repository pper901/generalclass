/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import com.arabiclearner.requestManager;
import com.arabiclearner.responseManager;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class httpClientHandler
extends Thread {
    Socket s;
    DataInputStream inp;
    DataOutputStream outp;
    PrintWriter pw;
    requestManager rM;
    responseManager resM;
    String hostname;
    Boolean isClosed;

    httpClientHandler(Socket s, DataInputStream inp, DataOutputStream outp) throws IOException {
        this.s = s;
        this.inp = inp;
        this.outp = outp;
        this.rM = new requestManager(inp);
        this.resM = new responseManager(outp);
        this.hostname = "generalclass.net";
    }

    @Override
    public void run() {
        try {
            System.out.println("Handling plain text http connection>>");
            String meth = this.rM.getTheMethod();
            String address = this.rM.getTheAddress();
            System.out.println(address);
            if (meth.equals("GET")) {
                if (address.equals("/index.html")) {
                    this.resM.sendRedirectCode("https://" + this.hostname + "/index.html");
                    this.s.close();
                } else if (address.equals("/")) {
                    this.resM.sendRedirectCode("https://" + this.hostname);
                    this.s.close();
                } else if (address.equals("/google4e383da5d2ff81b8.html")) {
                    this.resM.sendRedirectCode("https://" + this.hostname + "/google4e383da5d2ff81b8.html");
                    this.s.close();
                } else if (address.equals("/ads.txt")) {
                    this.resM.sendRedirectCode("https://" + this.hostname + "/ads.txt");
                    this.s.close();
                } else if (address.equals("/stu-index.html")) {
                    this.resM.sendRedirectCode("https://" + this.hostname + "/stu-index.html");
                    this.s.close();
                } else if (address.equals("/class.html")) {
                    this.resM.sendRedirectCode("https://" + this.hostname);
                    this.s.close();
                }
                
                this.isClosed = true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                this.s.close();
                this.isClosed = true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

