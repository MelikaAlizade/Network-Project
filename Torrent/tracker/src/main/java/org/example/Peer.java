package org.example;

import java.net.InetAddress;
import java.util.ArrayList;

public class Peer {
    private String id;
    private int port;
    private InetAddress ip;
    private int trackerPort;
    private ArrayList<File> files;

    public Peer(String id, int port, InetAddress ip, int trackerPort) {
        this.id = id;
        this.port = port;
        this.ip = ip;
        this.trackerPort = trackerPort;
        this.files = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getTrackerPort() {
        return trackerPort;
    }

    public void setTrackerPort(int trackerPort) {
        this.trackerPort = trackerPort;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public void addFile(File file) {
        files.add(file);
    }
}
