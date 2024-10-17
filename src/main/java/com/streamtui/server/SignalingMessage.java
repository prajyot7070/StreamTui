package com.streamtui.server;


public class SignalingMessage {
    public String type;
    public String payload;

    public SignalingMessage(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }
}