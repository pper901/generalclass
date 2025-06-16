/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import com.arabiclearner.responseManager;
import java.io.IOException;
import java.util.HashMap;

public class wSocketManager {
    private HashMap<String, responseManager> clientOutputStreams = new HashMap();

    public void addClient(String clientId, responseManager outputStream) {
        this.clientOutputStreams.put(clientId, outputStream);
    }

    public void removeClient(String clientId) {
        this.clientOutputStreams.remove(clientId);
    }

    public void sendMessageToClient(String clientId, String message) throws IOException {
        responseManager outputStream = this.clientOutputStreams.get(clientId);
        if (outputStream != null) {
            outputStream.sendAsMessage(message);
        }
    }

    public void sendMessageToAllClient(String message) {
        for (responseManager outputStream : this.clientOutputStreams.values()) {
            try {
                outputStream.sendAsMessage(message);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getKeysAsString() {
        StringBuilder sb = new StringBuilder();
        for (String key : this.clientOutputStreams.keySet()) {
            sb.append(key).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}

