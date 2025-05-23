package org.example;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Peer {
    private static final String TRACKER_ADDRESS = "127.0.0.1";
    private static final int TRACKER_PORT = 8080;
    private static int PEER_SERVER_PORT;
    private static Set<String> sharedFiles = new HashSet<>();
    private String serverAddress;
    private int serverPort;

    public Peer(String host, int port) throws IOException {
        this.serverAddress = host;
        this.serverPort = port;

        new Thread(Peer::startFileServer).start();

        Scanner scanner = new Scanner(System.in);
        DatagramSocket socket = new DatagramSocket();
        System.out.println("Peer running on port " + socket.getLocalPort());
        while (true) {
            String[] params = scanner.nextLine().split("\\s");
            if (params[0].equals("exit")) {
                endPeer(socket);
                System.exit(0);
            } else if ((params[0] + " " + params[1]).equals("request logs")) {
                requestLogs(socket);
            } else if (params[0].equals("share")) {
                String f = params[3];
                sharedFiles.add(f);
                registerFile(f, socket);
            } else if (params[0].equals("get")) {
                String selectedFile = params[3];
                requestFile(selectedFile, socket);
            }

        }
    }

    private static void requestLogs(DatagramSocket socket) throws IOException {
        String message = new Packet("request logs", "", "", "").toJson();
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length,
                InetAddress.getByName(TRACKER_ADDRESS), TRACKER_PORT);
        socket.send(packet);
        System.out.println(receivePacket(socket));
    }

    private static void endPeer(DatagramSocket socket) throws IOException {
        String message = new Packet("exit", "", "", "").toJson();
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length,
                InetAddress.getByName(TRACKER_ADDRESS), TRACKER_PORT);
        socket.send(packet);
        System.out.println(receivePacket(socket));
    }

    private static void startFileServer() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            PEER_SERVER_PORT = serverSocket.getLocalPort();
            System.out.println("File server running on port " + PEER_SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleFileRequest(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleFileRequest(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();

            String fileName = reader.readLine();
            System.out.println("Received request for file: " + fileName);


            if (sharedFiles.contains(fileName)) {
                System.out.println("File '" + fileName + "' sent to " + clientSocket.getInetAddress().getHostAddress());
            } else {
                out.write("ERROR: File not found\n".getBytes());
                System.out.println("Requested file '" + fileName + "' not found.");
            }

            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerFile(String fileName, DatagramSocket socket) throws IOException {
        String message = new Packet("share", String.valueOf(PEER_SERVER_PORT), "8080", fileName).toJson();
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length,
                InetAddress.getByName(TRACKER_ADDRESS), TRACKER_PORT);
        socket.send(packet);
        System.out.println(receivePacket(socket));
        System.out.println("File registered with tracker: " + fileName);
    }

    private static void requestFile(String fileName, DatagramSocket socket) throws IOException {
        String message = new Packet("get", String.valueOf(PEER_SERVER_PORT), "8080", fileName).toJson();
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length,
                InetAddress.getByName(TRACKER_ADDRESS), TRACKER_PORT);
        socket.send(packet);

        byte[] buffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(responsePacket);
        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());

        if (response.equals("400: No such file exists")) {
            System.out.println("No peers have this file.");
        } else {
            System.out.println("Peers with file: " + response);
            String peerAddress = "127.0.0.1";
            int peerPort = Integer.parseInt(response);
            downloadFile(peerAddress, peerPort, fileName);
        }
    }

    private static void downloadFile(String peerAddress, int peerPort, String fileName) {
        try {
            Socket socket = new Socket(peerAddress, peerPort);
            OutputStream out = socket.getOutputStream();
            out.write((fileName + "\n").getBytes());

            FileOutputStream fos = new FileOutputStream("downloaded_" + fileName);
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            fos.close();
            in.close();
            socket.close();
            System.out.println("File '" + fileName + "' downloaded from " + peerAddress + ":" + peerPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(DatagramSocket clientSocket, String m) throws IOException {
        String message = m;
        byte[] sendData = message.getBytes();
        InetAddress serverIP = InetAddress.getByName(serverAddress);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort);
        clientSocket.send(sendPacket);
        System.out.println("Sent: " + message);
    }

    private static String receivePacket(DatagramSocket clientSocket) throws IOException {
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        clientSocket.receive(receivePacket);
        String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
        return receivedData;
    }
}
