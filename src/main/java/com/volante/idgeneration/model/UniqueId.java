package com.volante.idgeneration.model;

import org.springframework.data.annotation.Id;

public class UniqueId {

    private String snowflake;
    private String volpayid; // New property for ID before hashing

    public UniqueId() {
        // No argument constructor
    }

    public UniqueId(String snowflake, String volpayid) {  // Optional constructor to set both IDs
        this.snowflake = snowflake;
        this.volpayid = volpayid;
    }

    public String getId() {
        return snowflake;
    }

    public void setId(String snowflake) {
        this.snowflake = snowflake;
    }

    public String getPreHashedId() {
        return volpayid ;
    }

    public void setPreHashedId(String volpayid) {
        this.volpayid = volpayid;
    }
}
