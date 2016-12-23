package com.carpenter.dean.pokemontinder;

/**
 * Created by deanc on 12/13/2016.
 */

public class Message {

    private String id;
    private String name;
    private String message;
    private String photoUrl;

    public Message() {}

    public Message(String name, String message, String photoUrl) {
        this.name = name;
        this.message = message;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
