package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TcpEchoServer {
    public static final int DEFAULT_PORT = 8007;
    private static final int ACCEPT_TIMEOUT_MS = 1000;
    private static final int BUFFER_SIZE = 4096;

    private final int port;
    private volatile boolean running = true;
    private ServerSocket serverSocket;
    private final ExecutorService workers;

    public TcpEchoServer(int port) {
        this.port = port;
        this.workers = Executors.newCachedThreadPool();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(ACCEPT_TIMEOUT_MS);
        log("Server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log("Shutdown hook triggered.");
            try {
                stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        try {
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    log("Accepted connection from " + client.getRemoteSocketAddress());
                    workers.submit(() -> handleClient(client));
                } catch (SocketTimeoutException e) {
                    // таймаут — проверяем флаг running и снова в цикл
                }
            }
        } finally {
            shutdownAndAwaitTermination();
            closeServerSocket();
            log("Server stopped.");
        }
    }

    public void stop() throws IOException {
        running = false;
        closeServerSocket();
        shutdownAndAwaitTermination();
    }

    private void closeServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log("Error closing server socket: " + e.getMessage());
            }
        }
    }

    private void shutdownAndAwaitTermination() {
        workers.shutdown();
        try {
            if (!workers.awaitTermination(5, TimeUnit.SECONDS)) {
                workers.shutdownNow();
                if (!workers.awaitTermination(5, TimeUnit.SECONDS)) {
                    log("Worker pool did not terminate.");
                }
            }
        } catch (InterruptedException ie) {
            workers.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void handleClient(Socket socket) {
        try (Socket s = socket;
             InputStream in = s.getInputStream();
             OutputStream out = s.getOutputStream()) {

             s.setSoTimeout(30_000);

            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                out.flush();
            }
            log("Connection closed by client " + s.getRemoteSocketAddress());
        } catch (IOException e) {
            log("I/O error with client " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        }
    }

    private static void log(String message) {
        System.out.printf("%s [TcpEchoServer] %s%n", LocalDateTime.now(), message);
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                System.err.println("Bad port argument, using default " + DEFAULT_PORT);
            }
        }

        TcpEchoServer server = new TcpEchoServer(port);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
