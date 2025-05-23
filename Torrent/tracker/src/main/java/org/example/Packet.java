package org.example;

import com.google.gson.Gson;
import java.io.Serializable;

public class Packet implements Serializable {
    String command;
    String topic;
    String value;
    String extra;

    public Packet(String command, String topic, String value, String extra) {
        this.command = command;
        this.topic = topic;
        this.value = value;
        this.extra = extra;
    }

    public String toJson(){
        return new Gson().toJson(this);
    }
}
