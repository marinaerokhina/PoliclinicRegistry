package com.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Local development server for the Polyclinic Registry demo.
 * Binds to loopback so the unauthenticated serialization protocol is not
 * accidentally exposed to other machines.
 */
public class Server {
    public static final int DEFAULT_PORT = 12345;

    private final int port;
    private final DatabaseManager dbManager;

    public Server(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.port = port;
        this.dbManager = new DatabaseManager();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(
                port, 50, InetAddress.getLoopbackAddress())) {
            System.out.println("Polyclinic Registry demo server listening on 127.0.0.1:" + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread handlerThread = new Thread(new ClientHandler(clientSocket, dbManager));
                handlerThread.setDaemon(true);
                handlerThread.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 1) {
            System.err.println("Usage: java -jar server.jar [port]");
            System.exit(2);
        }
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port must be an integer between 1 and 65535.");
                System.exit(2);
            }
        }
        new Server(port).start();
    }
}
