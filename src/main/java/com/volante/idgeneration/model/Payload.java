package com.volante.idgeneration.model;

public class Payload {

    private String name;
    private String type;

    public Payload() {
        // No argument constructor
    }

    public Payload(String name, String type) {
        this.type = type;
        this.name = name;

    }

    // Getters and Setters for all attributes
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
