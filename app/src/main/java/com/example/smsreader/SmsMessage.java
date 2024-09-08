package com.example.smsreader;
public class SmsMessage {
    private final String id;
    private final String body;
    private final String date;

    public SmsMessage(String id, String body, String date) {
        this.id = id;
        this.body = body;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }
}