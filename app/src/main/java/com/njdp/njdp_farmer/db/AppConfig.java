package com.njdp.njdp_farmer.db;

/**
 * Created by USER-PC on 2016/4/13.
 */
public class AppConfig {
    //服务器地址
    //public static String URL_IP="http://211.68.180.9:88/";
    public static String URL_IP="http://218.12.43.229:81/";
    // 登录 url
    public static String URL_LOGIN = URL_IP+"appLogin";
    // 注册 url
    public static String URL_REGISTER = URL_IP+"db_xskq/register1.php";
    // 找回密码 url
    public static String URL_GETPASSWORD1= URL_IP+"db_xskq/forget_password_isAccess.php";
    //重设密码
    public static String URL_GETPASSWORD2= URL_IP+"db_xskq/forget_password_finish.php";
    //农田发布
    public static String URL_FARMLAND_RELEASE = URL_IP+"app/farmlands/store";
    //农田发查询
    public static  String URL_FARMLAND_GET = URL_IP + "app/farmlands/index";
    //农机查询
    public static  String URL_MACHINE_GET = URL_IP + "app/farmlands/searchMachine";
}
