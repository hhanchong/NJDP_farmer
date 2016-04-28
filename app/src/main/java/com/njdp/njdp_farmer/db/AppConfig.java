package com.njdp.njdp_farmer.db;

/**
 * Created by USER-PC on 2016/4/13.
 */
public class AppConfig {
    //服务器地址
    public static String URL_IP="http://211.68.180.9:88/";

    // 登录 url
    public static String URL_LOGIN = URL_IP+"appLogin";

    // 注册 url
    public static String URL_REGISTER = URL_IP+"db_xskq/register1.php";

    // 找回密码 url
    public static String URL_GETPASSWORD1= URL_IP+"db_xskq/forget_password_isAccess.php";

    public static String URL_GETPASSWORD2= URL_IP+"db_xskq/forget_password_finish.php";

    public static String URL_FARMLAND_RELEASE = URL_IP+"app/farmlands/store";

    public static  String URL_FARMLAND_GET = URL_IP + "app/farmlands/index";
}
