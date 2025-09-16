package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpEchoClient {
    private static final Logger logger = LoggerFactory.getLogger(TcpEchoClient.class);

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8007;
        String message = "Hello, World!\n";

        if (args.length >= 1) host = args[0];
        if (args.length >= 2) port = Integer.parseInt(args[1]);
        if (args.length >= 3) message = args[2];

        int connectTimeout = 5000;
        int readTimeout = 5000;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), connectTimeout);
            socket.setSoTimeout(readTimeout);

            logger.info("Connected to {}:{} at {}", host, port, LocalDateTime.now());

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            out.write(bytes);
            out.flush();

            byte[] buffer = new byte[4096];
            try {
                int read = in.read(buffer);
                if (read > 0) {
                    String received = new String(buffer, 0, read, StandardCharsets.UTF_8);
                    logger.info("Received: {}", received);
                } else {
                    logger.warn("No response or connection closed.");
                }
            } catch (IOException e) {
                logger.error("Read timed out after {} ms", readTimeout);
            }
        } catch (IOException e) {
            logger.error("Connection failed: {}", e.getMessage(), e);
        }
    }
}