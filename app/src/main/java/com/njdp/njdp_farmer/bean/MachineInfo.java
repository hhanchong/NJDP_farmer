package com.njdp.njdp_farmer.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/4/30.
 */
public class MachineInfo implements Serializable {
    private int id;
    private double longitude;//经度
    private double latitude;//纬度
    private String telephone;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
