package com.njdp.njdp_farmer.bean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/4/26.
 */
public class FarmlandInfo implements Serializable {
    public final DateFormat yyyymmdd_DateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private int id;
    private int fm_id;
    private float area;
    private String crops_kind;
    private float unit_price;
    private String block_type;
    private String province;
    private String city;
    private String county;
    private String town;
    private String village;
    private String longitude;
    private String latitude;
    private String street_view;
    private Date start_time;
    private Date end_time;
    private String status;
    private String remark;

    public FarmlandInfo()
    {
        id = 0;
        fm_id = 0;
        area = 0;
        unit_price = 0;
        crops_kind = "";
        block_type = "";
        province = "";
        city= "";
        county = "";
        town = "";
        village = "";
        longitude = "";
        latitude = "";
        street_view = "";
        try {
            start_time = yyyymmdd_DateFormat.parse("1900-01-01");
            end_time = yyyymmdd_DateFormat.parse("1900-01-01");
        }
        catch (Exception ex)
        {
            start_time = null;
            end_time = null;
        }
        status = "";
        remark = "";
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFm_id() {
        return fm_id;
    }

    public void setFm_id(int fm_id) {
        this.fm_id = fm_id;
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public String getCrops_kind() {
        return crops_kind;
    }

    public void setCrops_kind(String crops_kind) {
        this.crops_kind = crops_kind;
    }

    public float getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(float unit_price) {
        this.unit_price = unit_price;
    }

    public String getBlock_type() {
        return block_type;
    }

    public void setBlock_type(String block_type) {
        this.block_type = block_type;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getStreet_view() {
        return street_view;
    }

    public void setStreet_view(String street_view) {
        this.street_view = street_view;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
