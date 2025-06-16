package com.arabiclearner;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class responseManager {
    DataOutputStream out;
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    responseManager(DataOutputStream out) {
        this.out = out;
    }

    public void sendAsHtml(String s) throws IOException {
        System.out.println("Output here");
        try {
            long startTime = System.nanoTime();

            this.out.writeBytes("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n" + s);
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for HTML: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void sendAsCss(String s) throws IOException {
        System.out.println("Output CSS here...");
        try {
            long startTime = System.nanoTime();

            this.out.writeBytes("HTTP/1.1 200 OK\r\nContent-Type: text/css\r\n\r\n" + s);
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for CSS: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void sendAsJavascript(String s) throws IOException {
        System.out.println("Output JS here");
        try {
            long startTime = System.nanoTime();

            this.out.writeBytes("HTTP/1.1 200 OK\r\nContent-Type: text/javascript\r\n\r\n" + s);
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for JS: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void sendAsJson(String jsonString) throws IOException {
        System.out.println("Output JSON here");
        try {
            long startTime = System.nanoTime();

            this.out.writeBytes("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + jsonString);
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for JSON: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void setAndSendCookie(String file, String cookie) throws IOException {
        System.out.println("Output Cookie here");
        try {
            long startTime = System.nanoTime();

            this.out.writeBytes("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nSet-Cookie:" + cookie + "; Path=/; Secure;Expires=Thu, 01 Jan 1970 00:00:00 GMT;HttpOnly;\r\n\r\n" + file + "\r\n");
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for Cookie: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void sendRedirectCode(String file, String address, String cookie) throws IOException {
        System.out.println("Output Redirect here");
        try {
            long startTime = System.nanoTime();

            System.out.println("Rendering content and navigating to " + address);
            System.out.println("Setting the cookie to: " + cookie);
            System.out.println("Setting the file content to: " + file);
            String response = "HTTP/1.1 302 Found\r\nContent-Type: text/html\r\nLocation:" + address + "\r\nSet-Cookie:" + cookie + "\r\n\r\n" + file + "\r\n";
            this.out.writeBytes(response);
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for Redirect: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void sendRedirectCode(String file, String address) throws IOException {
        System.out.println("Output Redirect here");
        try {
            long startTime = System.nanoTime();

            System.out.println("Redirecting to this address " + address);
            this.out.writeBytes("HTTP/1.1 302 Found\r\nContent-Type: text/html\r\nLocation: " + address + "\r\n\r\n" + file + "\r\n");
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for Redirect: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void doWebSocketHandShake(String key) throws IOException {
        System.out.println("Output Handshaking here");
        try {
            long startTime = System.nanoTime();

            this.out.writeBytes("HTTP/1.1 101 Switching Protocols\r\nConnection: Upgrade\r\nUpgrade: websocket\r\nSec-WebSocket-Accept: " + key + "\r\n\r\n");
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for WebSocket Handshake: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void sendAsMessage(String s) throws IOException {
        System.out.println("Output Message here");
        try {
            long startTime = System.nanoTime();

            this.out.writeBytes("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n" + s + "\r\n");
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for Message: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void sendAsText(String s) throws IOException {
        System.out.println("Output Text here");
        try {
            long startTime = System.nanoTime();

            this.out.writeBytes(s);
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for Text: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void close() throws IOException {
        this.out.close();
    }

    public void sendAsFile(String filename) throws IOException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            int bytesRead;
            String contentType = responseManager.getContentType(filename);
            System.out.println("The content type is: " + contentType);

            long startTime = System.nanoTime();

            // Write the HTTP response headers
            this.out.writeBytes("HTTP/1.1 200 OK\r\n");
            this.out.writeBytes("Content-Type: " + contentType + "\r\n");
            this.out.writeBytes("\r\n");

            long headersWrittenTime = System.nanoTime();
            System.out.println("Time to write headers: " + (headersWrittenTime - startTime) / 1_000_000.0 + " ms");

            // Write the actual file bytes
            byte[] buffer = new byte[4096];
            while ((bytesRead = fis.read(buffer)) != -1) {
                this.out.write(buffer, 0, bytesRead);
            }
            this.out.flush();

            long fileWrittenTime = System.nanoTime();
            System.out.println("Time to write file: " + (fileWrittenTime - headersWrittenTime) / 1_000_000.0 + " ms");

            long endTime = System.nanoTime();
            System.out.println("Total send time: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public void sendAsByteFile(byte[] bfile, String name) throws IOException {
        String contentType = responseManager.getContentType(name);
        System.out.println("The content type is: " + contentType);

        try {
            long startTime = System.nanoTime();

            // Write the HTTP response headers
            this.out.writeBytes("HTTP/1.1 200 OK\r\n");
            this.out.writeBytes("Content-Type: " + contentType + "\r\n");
            this.out.writeBytes("Content-Length: " + bfile.length + "\r\n");
            this.out.writeBytes("\r\n");

            long headersWrittenTime = System.nanoTime();
            System.out.println("Time to write headers: " + (headersWrittenTime - startTime) / 1_000_000.0 + " ms");

            // Write the actual file bytes
            this.out.write(bfile);
            // this.out.flush();

            long fileWrittenTime = System.nanoTime();
            System.out.println("Time to write file: " + (fileWrittenTime - headersWrittenTime) / 1_000_000.0 + " ms");

            long endTime = System.nanoTime();
            System.out.println("Total send time: " + (endTime - startTime) / 1_000_000.0 + " ms");

        } finally {
            closeOutputStream();
        }
    }

    public void sendAsInputStream(InputStream in, String filename) throws IOException {
        int bytesRead;
        String contentType = responseManager.getContentType(filename);
        System.out.println("The content type is: " + contentType);

        try {
            long startTime = System.nanoTime();

            // Write the HTTP response headers
            this.out.writeBytes("HTTP/1.1 200 OK\r\n");
            this.out.writeBytes("Content-Type: " + contentType + "\r\n");
            this.out.writeBytes("\r\n");

            long headersWrittenTime = System.nanoTime();
            System.out.println("Time to write headers: " + (headersWrittenTime - startTime) / 1_000_000.0 + " ms");

            // Write the actual file bytes
            byte[] buffer = new byte[4096];
            while ((bytesRead = in.read(buffer)) != -1) {
                this.out.write(buffer, 0, bytesRead);
            }
            this.out.flush();

            long fileWrittenTime = System.nanoTime();
            System.out.println("Time to write file: " + (fileWrittenTime - headersWrittenTime) / 1_000_000.0 + " ms");

            long endTime = System.nanoTime();
            System.out.println("Total send time: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    public static String readFileToBase64(String filePath) throws IOException {
        byte[] fileData = responseManager.readFileToByteArray(filePath);
        return Base64.getEncoder().encodeToString(fileData);
    }

    public static byte[] readFileToByteArray(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileData = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
        }
        return fileData;
    }

    public static String getContentType(String fileName) {
        String extension = responseManager.getFileExtension(fileName);
        return MIME_TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }

    public void sendRedirectCode(String address) throws IOException {
        System.out.println("Output Redirect here");
        try {
            long startTime = System.nanoTime();

            System.out.println("Redirecting to this address " + address);
            this.out.writeBytes("HTTP/1.1 301 Moved Permanently\r\nContent-Type: text/html\r\nLocation: " + address + "\r\n\r\n");
            this.out.flush();

            long endTime = System.nanoTime();
            System.out.println("Total send time for Redirect: " + (endTime - startTime) / 1_000_000.0 + " ms");
        } finally {
            closeOutputStream();
        }
    }

    private void closeOutputStream() {
        if (this.out != null) {
            try {
                long startCloseTime = System.nanoTime();
                this.out.close();
                long endCloseTime = System.nanoTime();
                System.out.println("Time to close output stream: " + (endCloseTime - startCloseTime) / 1_000_000.0 + " ms");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static {
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("ico", "image/x-icon");
    }
}
