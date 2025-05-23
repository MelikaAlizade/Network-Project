package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Connection extends Thread {
    private final DatagramSocket socket;
    private final DatagramPacket packet;

    public Connection(DatagramSocket socket, DatagramPacket packet) {
        this.socket = socket;
        this.packet = packet;
    }

    @Override
    public void run() {
        new Thread(Connection::handleLogs).start();
        try {
            String receivedData = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received from: " + packet.getAddress() + ":" + packet.getPort()+ " -> " + receivedData);
            Peer currentPeer= new Peer(generatePeerId(),packet.getPort(),packet.getAddress(),8080);
            if(!Database.checkPeerExists(currentPeer)){
                Database.addPeer(currentPeer);
            }

            String response = handleRequest(receivedData,currentPeer);
            byte[] responseData = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                    packet.getAddress(), packet.getPort());
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.send(responsePacket);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleLogs(){
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine().trim();

            if (command.equals("logs request")) {
                SystemLog.printRequests();
            }else if (command.equals("all-logs")) {
                SystemLog.printLogs();
            }else if (command.startsWith("file_logs ")) {
                String fileName = command.substring(10).trim();
                SystemLog.printFileLogs(fileName);
            }
        }
    }

    private String handleRequest(String data, Peer currentPeer) throws IOException {
        if (data.isEmpty())
            return null;

        try {
            Packet packet = new Gson().fromJson(data, Packet.class);
            String topic = packet.topic;
            String value = packet.value;
            String command = packet.command;
            String extra = packet.extra;

            switch (command) {
                case "exit" ->{
                    Database.removePeer(currentPeer);
                    System.out.println("Peer" +" Disconnected!");
                    return ("Peer Disconnected");
                }
                case "request logs" -> {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, ArrayList<String>> entry : SystemLog.getPeerReq().entrySet()) {
                        sb.append("File: ").append(entry.getKey()).append("\n");
                        for (String request : entry.getValue()) {
                            sb.append("  → Request: ").append(formatRequest(request)).append("\n");
                        }
                    }
                    return sb.toString();
                }
                case "get" -> {
                    ArrayList<Peer> foundPeers = Database.getFilePort(extra);
                    if (!foundPeers.isEmpty()) {
                        SystemLog.addFileLog(extra, currentPeer.getId() + ": " + command);
                        SystemLog.addLogRequest(currentPeer.getId() + ": " + command + " " + extra + " "
                                + ", Successful" + "\n" + foundPeersToString(foundPeers));
                        Random random = new Random();
                        Peer randomPeer = foundPeers.get(random.nextInt(foundPeers.size()));
                        SystemLog.addPeerReq(extra,currentPeer.getId()+ "get"+extra+"from"+randomPeer.getId());
                        int filePort=Database.findFilePort(randomPeer.getPort(),extra);
                        currentPeer.addFile(new File(extra,Integer.parseInt(topic)));
                        Database.updatePeer(topic,extra);
                        return String.valueOf(filePort);
                    } else {
                        SystemLog.addLogRequest(generatePeerId() + ": " + command + extra +
                                ",Fail" + "\n No Peer found for this file");
                        return "400: No such file exists";
                    }
                }
                case "share" -> {
                    SystemLog.addFileLog(extra, currentPeer.getId() + " shared");
                    SystemLog.addLog(currentPeer.getId() + " shared " + extra);
                    currentPeer.addFile(new File(extra,Integer.parseInt(topic)));
                    Database.updatePeer(topic,extra);
                    return "200: "+extra+" shared successfully!";
                }
                default -> {
                    return ("400: Invalid command.");
                }
            }
        } catch (JsonSyntaxException e) {
            return ("400: Missing topic, value, or command fields.");
        }
    }

    private static String formatRequest(String request) {
        return request.replaceAll("(peer\\d+)(get)(.+)(from)(peer\\d+)", "$1 $2 $3 $4 $5");
    }

    private String foundPeersToString(ArrayList<Peer> foundPeers) {
        StringBuilder sb = new StringBuilder();
        for(Peer p:foundPeers){
            sb.append(p.getId()).append(", ");
        }
        return sb.toString();
    }

    private String generatePeerId() {
        String result = "peer" + packet.getPort();
        return result;
    }
}
