/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class longPollingManager {
    private HashMap<String, String> clientOutputStreams;
    private String updates;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = this.lock.newCondition();
    private ArrayList<String> messageList = new ArrayList();

    public longPollingManager() {
        this.clientOutputStreams = new HashMap();
    }

    public void addClient(String clientId, String outputStream) {
        System.out.println("Adding Client " + clientId);
        this.clientOutputStreams.put(clientId, outputStream);
    }

    public void removeClient(String clientId) {
        this.clientOutputStreams.remove(clientId);
    }

    public synchronized Boolean getUpdate(String clientId) {
        System.out.println("Getting updates");
        if (this.clientOutputStreams.get(clientId) != null) {
            System.out.println("Update available");
            return true;
        }
        return false;
    }

    public String getMessage(String clientId) {
        System.out.println("getting message for " + clientId);
        return this.clientOutputStreams.get(clientId);
    }

    public void setUpdate(String s) {
        this.updates = s;
        System.out.println("Update value: " + this.updates);
        this.condition.signalAll();
    }

    public void addMessageToList(String s) {
        this.messageList.add(s);
    }

    public void sendAllMessage(String clientId) throws IOException {
        if (this.messageList != null) {
            String s = longPollingManager.arrayListToString(this.messageList);
            this.sendMessageToClient(clientId, "MList: " + s);
        }
    }

    private static String arrayListToString(ArrayList<?> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); ++i) {
            sb.append(list.get(i));
            if (i == list.size() - 1) continue;
            sb.append(",");
        }
        return sb.toString();
    }

    public void sendMessageToClient(String clientId, String message) throws IOException {
        if (this.clientOutputStreams.containsKey(clientId)) {
            System.out.println("Sending " + message + " to " + clientId);
            this.clientOutputStreams.put(clientId, message);
        } else {
            System.out.println("Client " + clientId + " doesn't exist");
        }
    }

    public void sendMessageToAllClient(String message) {
        String[] mess = message.split(":");
        System.out.println("Sending message to all client except: " + mess[1]);
        for (String client : this.clientOutputStreams.keySet()) {
            if (client.equals(mess[1].trim())) continue;
            this.clientOutputStreams.put(client, message);
        }
    }

    public void sendIceToAllClient(String message) {
        System.out.println("Sending message to all client except: ");
        for (String client : this.clientOutputStreams.keySet()) {
            this.clientOutputStreams.put(client, message);
        }
    }

    public void sendAllClientAsMessage(String clientId) throws IOException {
        StringBuilder keysString = new StringBuilder();
        for (String key : this.clientOutputStreams.keySet()) {
            if (keysString.length() > 0) {
                keysString.append(", ");
            }
            keysString.append(key);
        }
        if (this.messageList != null) {
            System.out.println("Sending data of AllClient: " + this.messageList);
            this.sendMessageToClient(clientId, "List: " + keysString.toString() + "&MList: " + longPollingManager.arrayListToString(this.messageList));
        } else {
            this.sendMessageToClient(clientId, "List: " + keysString.toString());
        }
    }

    public void acquireLock() {
        this.lock.lock();
    }

    public Condition getCondition() {
        return this.condition;
    }

    public void unlock() {
        this.lock.unlock();
    }
}

