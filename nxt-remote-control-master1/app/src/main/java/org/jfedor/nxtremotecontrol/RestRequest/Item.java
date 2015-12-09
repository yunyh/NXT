package org.jfedor.nxtremotecontrol.RestRequest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YoungHyup on 2015-12-02.
 */
public class Item {
    @SerializedName("alert")
    private String alert;
    private String id;

    public String getId(){return id;}
    public void setString(String id){
        this.id = id;
    }
    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;

    }
}
