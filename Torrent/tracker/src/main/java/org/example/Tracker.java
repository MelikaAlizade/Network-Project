package org.example;

import java.net.*;

public class Tracker {
    private final int port;

    public Tracker(int port) {
        this.port = port;
        start();
    }

    private void start() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Tracker started on port " + port);

            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                Connection connection = new Connection(socket,receivePacket);
                connection.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
