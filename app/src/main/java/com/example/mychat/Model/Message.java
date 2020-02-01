package com.example.mychat.Model;

public class Message {

    private String sender;
    private String receiver;
    private String message;
    private boolean seen;
    private String time;
    private String date;
    private String imageUrl;

    public Message(String sender, String receiver, String message, boolean seen, String time, String date, String imageUrl) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    public Message() {

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
