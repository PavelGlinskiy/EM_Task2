package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class TcpEchoClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8007;
        String message = "Hello, World!\n";

        if (args.length >= 1) host = args[0];
        if (args.length >= 2) port = Integer.parseInt(args[1]);
        if (args.length >= 3) message = args[2];

        try (Socket socket = new Socket(host, port)) {
            System.out.println(LocalDateTime.now() + " Connected to " + host + ":" + port);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            out.write(bytes);
            out.flush();

            byte[] buffer = new byte[4096];
            int read = in.read(buffer);
            if (read > 0) {
                String received = new String(buffer, 0, read, StandardCharsets.UTF_8);
                System.out.println("Received: " + received);
            } else {
                System.out.println("No response or connection closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}