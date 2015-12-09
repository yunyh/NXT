package com.gcm.younghyup.gcm.RestRequest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YoungHyup on 2015-12-02.
 */
public class Item {
    @SerializedName("id")
    private String id;
    @SerializedName("status")
    private boolean status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;

    }

    public void setStatus(boolean s){
        status = s;
    }
}
