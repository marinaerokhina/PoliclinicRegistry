package com.server;

import com.common.network.OperationType;
import com.common.network.Request;
import com.common.network.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * Headless integration smoke test using the real client-server protocol.
 * Run only against the local demo server with synthetic records.
 */
public final class ConnectionSmokeTest {
    private ConnectionSmokeTest() {}

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 12345), 5000);
            socket.setSoTimeout(5000);

            try (ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                output.writeObject(new Request(OperationType.GET_ALL_DOCTORS, null));
                output.flush();

                Object received = input.readObject();
                if (!(received instanceof Response response) || !response.isSuccess()) {
                    throw new AssertionError("Expected a successful GET_ALL_DOCTORS response");
                }
                if (!(response.getData() instanceof List<?> doctors) || doctors.isEmpty()) {
                    throw new AssertionError("Expected fictional sample doctors from H2");
                }
                System.out.println("Client/server protocol OK; sample doctor count: " + doctors.size());
            }
        }
    }
}
