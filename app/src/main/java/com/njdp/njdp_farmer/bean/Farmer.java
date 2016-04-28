package com.njdp.njdp_farmer.bean;

import java.io.Serializable;

/**
 * Created by USER-PC on 2016/4/13.
 */
public class Farmer implements Serializable {
    private int id;
    private String name;
    private String telephone;
    private String password;
    private String imageUrl;
    private String qq;
    private String weixin;
    private String address;
    private boolean isLogined;

    public Farmer() {
        id = 0;
        name = "";
        telephone = "";
        password = "";
        imageUrl = "";
        qq = "";
        weixin = "";
        address = "";
        isLogined = false;
    }

    public int getId(){return id;}

    public void setId(int id){this.id = id;}

    public  String getName()
    {
        return name;
    }

    public  void setName(String name)
    {
        this.name=name;
    }

    public  String getTelephone()
    {
        return telephone;
    }

    public  void setTelephone(String telephone)
    {
        this.telephone=telephone;
    }

    public  String getImageUrl()
    {
        return imageUrl;
    }

    public  void setImageUrl(String imageUrl)
    {
        this.imageUrl=imageUrl;
    }

    public  String getPassword()
    {
        return password;
    }

    public  void setPassword(String password)
    {
        this.password=password;
    }

    public String getQQ(){ return qq;}

    public void setQQ(String qq){this.qq = qq;}

    public String getWeixin(){return weixin;}

    public void setWeixin(String weixin){this.weixin = weixin;}

    public String getAddress(){return address;}

    public void setAddress(String address){this.address = address;}

    public boolean getIsLogined()
    {
        return isLogined;
    }

    public void setIsLogined(boolean isLogined)
    {
        this.isLogined=isLogined;
    }
}
