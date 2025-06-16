/*
 * Decompiled with CFR 0.152.
 */
package com.arabiclearner;

import com.arabiclearner.requestManager;
import com.arabiclearner.responseManager;
import com.arabiclearner.wSocketManager;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class wSocketHandler {
    private requestManager rm;
    private responseManager resM;
    wSocketManager w;

    wSocketHandler(requestManager rm, responseManager resM) throws IOException, NoSuchAlgorithmException {
        this.rm = rm;
        this.resM = resM;
        rm.printMap();
        String key = rm.generateSecWebSocketAccept();
        System.out.println("Sec-Accept: " + key);
        resM.doWebSocketHandShake(key);
    }
}

