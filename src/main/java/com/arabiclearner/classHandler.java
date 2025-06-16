/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.util.HashMap;
import java.util.Set;

public class classHandler {
    HashMap<String, classManipulation> classes = new HashMap<>();
    HashMap<String, startWebSocket2> websocketServers = new HashMap<>();

    public void setTheClass(String classname, classManipulation cM) {
        System.out.println("Setting class with name: " + classname);
        this.classes.put(classname, cM);
        startWebSocket2 sw = new startWebSocket2(this);
        sw.startWebS();
        this.websocketServers.put(classname, sw);
    }

    public classManipulation getTheClass(String classname) {
        System.out.println("getting class with name: " + classname);
        if (this.classes.containsKey(classname)) {
            System.out.println("Found class with name: " + classname);
            return this.classes.get(classname);
        }
        return null;
    }

    public void removeTheClass(String classname) {
        System.out.println("removing the class with name " + classname);
        if (this.classes.containsKey(classname)) {
            this.classes.remove(classname);
            System.out.println("Successfully removed the class");
            if (this.websocketServers.containsKey(classname)) {
                startWebSocket2 sw = this.websocketServers.get(classname);
                sw.stopWebS();
                this.websocketServers.remove(classname);
                System.out.println("Successfully stopped the websocket for class" + classname);
            }
        }
    }

    // Method to get all class names
    public String[] getAllClassNames() {
        Set<String> classNamesSet = this.classes.keySet();
        return classNamesSet.toArray(new String[0]);
    }
}

