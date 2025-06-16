/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class turnServer
extends Thread {
    InputStream inp;
    OutputStream outp;

    public turnServer(Socket clientSocket) throws IOException {
        this.inp = clientSocket.getInputStream();
        this.outp = clientSocket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            byte[] requestBuffer = new byte[512];
            int bytesRead = this.inp.read(requestBuffer);
            if (bytesRead > 0 && this.isStunBindingRequest(requestBuffer)) {
                int attributeLength;
                block6: for (int attributeOffset = 20; attributeOffset < requestBuffer.length; attributeOffset += 4 + attributeLength) {
                    int attributeType = (requestBuffer[attributeOffset] & 0xFF) << 8 | requestBuffer[attributeOffset + 1] & 0xFF;
                    attributeLength = (requestBuffer[attributeOffset + 2] & 0xFF) << 8 | requestBuffer[attributeOffset + 3] & 0xFF;
                    switch (attributeType) {
                        case 6: {
                            byte[] username = Arrays.copyOfRange(requestBuffer, attributeOffset + 4, attributeOffset + 4 + attributeLength);
                            this.processUsernameAttribute(username);
                            continue block6;
                        }
                        case 8: {
                            continue block6;
                        }
                    }
                }
                this.sendStunBindingResponse(this.outp);
            }
        }
        catch (Exception e) {
            System.out.println("Error is: " + e);
        }
    }

    private boolean isStunBindingRequest(byte[] buffer) {
        return buffer[0] == 0 && buffer[1] == 1;
    }

    private byte[] getThelength(byte[] rqbuffer) {
        byte[] contentLength = new byte[]{rqbuffer[2], rqbuffer[3]};
        return contentLength;
    }

    private byte[] getTheMagicCookie(byte[] rqbuffer) {
        byte[] magicCookie = new byte[4];
        if (rqbuffer.length >= 8) {
            for (int i = 4; i < 8; ++i) {
                magicCookie[i - 4] = rqbuffer[i];
            }
        }
        return magicCookie;
    }

    private byte[] getTheTransactionId(byte[] rqbuffer) {
        byte[] tid = new byte[12];
        if (rqbuffer.length >= 20) {
            System.arraycopy(rqbuffer, 8, tid, 0, 12);
        } else {
            System.out.println("Error: Invalid STUN Binding Request. Buffer size is less than expected.");
        }
        return tid;
    }

    private void processUsernameAttribute(byte[] username) {
        String usernameString = new String(username, StandardCharsets.UTF_8);
        System.out.println("Received username: " + usernameString);
    }

    private void sendStunBindingResponse(OutputStream outputStream) throws IOException {
        byte[] response = new byte[]{1, 1, 0, 8, 33, 18, -92, 66};
        outputStream.write(response);
        outputStream.flush();
    }
}

