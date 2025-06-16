/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import java.io.IOException;

public class Main {
    static classHandler cHandler = new classHandler();

    public static void main(String[] args) throws Exception {
        // new Thread(() -> {
            try {
                new ServerRunClass(cHandler);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        // }).start();
    }
}

