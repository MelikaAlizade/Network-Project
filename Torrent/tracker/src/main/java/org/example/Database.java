package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    public static ArrayList<Peer> foundPeers = new ArrayList<>();
    private static ArrayList<Peer> allPeers = new ArrayList<>();


    public static void addPeer(Peer peer) {
        allPeers.add(peer);
    }

    public static void removePeer(Peer peer) {
        for (Peer p : allPeers) {
            if (p.getPort() == peer.getPort()) {
                allPeers.remove(peer);
                return;
            }
        }
    }

    public static boolean checkPeerExists(Peer peer) {
        for (Peer p : allPeers) {
            if (p.getPort() == peer.getPort()) {
                return true;
            }
        }
        return false;
    }

    public static void updatePeer(String topic, String extra) {
        for (Peer p : allPeers) {
            if (p.getPort() == Integer.parseInt(topic)) {
                p.addFile(new File(extra, Integer.parseInt(topic)));
                return;
            }
        }
    }

    public static ArrayList<Peer> getFilePort(String extra) {
        foundPeers.clear();
        for (Peer p : allPeers) {
            for (File f : p.getFiles()) {
                if (f.getName().equals(extra)) {
                    foundPeers.add(p);
                }
            }
        }
        return foundPeers;
    }

    public static int findFilePort(int port, String extra) {
        for (Peer p : allPeers) {
            if (p.getPort() == port) {
                for (File f : p.getFiles()) {
                    if (f.getName().equals(extra)) {
                        return f.getPort();
                    }
                }
            }
        }
        return 0;
    }
}
