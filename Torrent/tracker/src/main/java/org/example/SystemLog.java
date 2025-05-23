package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemLog {
    private static List<String> logRequests = new ArrayList<>();
    private static List<String> allLogs = new ArrayList<>();
    private static Map<String,ArrayList<String>> fileLogs = new HashMap<>();
    private static Map<String,ArrayList<String>> peerReq = new HashMap<>();

    public static List<String> getLogRequests() {
        return logRequests;
    }

    public static void addLogRequest(String req){
        logRequests.add(req);
    }

    public static List<String> getAllLogs() {
        return allLogs;
    }

    public static void addLog(String log){
        allLogs.add(log);
    }

    public static void addFileLog(String file, String text){
        fileLogs.putIfAbsent(file,new ArrayList<>());
        fileLogs.get(file).add(text);
    }

    public static void addPeerReq(String file, String text){
        peerReq.putIfAbsent(file,new ArrayList<>());
        peerReq.get(file).add(text);
    }

    public static void printRequests(){
        logRequests.forEach(System.out::println);
    }

    public static void printLogs(){
        allLogs.forEach(System.out::println);
    }

    public static void printFileLogs(String fileName){
        if (fileLogs.containsKey(fileName)) {
            System.out.println("Logs for file " + fileName + ": ");
            fileLogs.get(fileName).forEach(System.out::println);
        } else {
            System.out.println("Error: File not found.");
        }
    }

    public static Map<String, ArrayList<String>> getPeerReq() {
        return peerReq;
    }
}
