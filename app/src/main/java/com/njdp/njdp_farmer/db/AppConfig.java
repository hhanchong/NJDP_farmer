package com.njdp.njdp_farmer.db;


/**
 * Created by USER-PC on 2016/4/13.
 * 各个URL请求地址
 */
public class AppConfig {
    //服务器地址
    public static String URL_IP="http://211.68.180.9:88/"; //BaoDing
    //public static String URL_IP="http://218.12.43.229:81/"; //ShiJiaZhuang
    // 登录 url
    public static String URL_LOGIN = URL_IP+"appLogin";
    // 注册 url
    public static String URL_REGISTER = URL_IP+"farmerRegister";
    //获取验证码
    public static String URL_GET_REGISTERCODE = URL_IP+"sendMessage";
    // 找回密码 url
    public static String URL_GETPASSWORD1= URL_IP+"/forget_password_isAccess.php";
    //重设密码
    public static String URL_GETPASSWORD2= URL_IP+"/forget_password_finish.php";
    //农田发布
    public static String URL_FARMLAND_RELEASE = URL_IP+"app/farmlands/store";
    //农田查询
    public static  String URL_FARMLAND_GET = URL_IP + "app/farmlands/index";
    //删除单块农田
    public static  String URL_FARMLAND_DEL = URL_IP + "app/farmlands/destroy";
    //删除全部农田
    public static  String URL_FARMLAND_DEL_ALL = URL_IP + "app/farmlands/delAll";
    //编辑农田信息
    public static  String URL_FARMLAND_EDIT = URL_IP + "app/farmlands/update";
    //农机查询
    public static  String URL_MACHINE_GET = URL_IP + "app/farmlands/searchMachine";
    //获取个人信息
    public static String URL_GETUSERINFO = URL_IP + "app/getUserInfo";
    //个人信息修改
    public static  String URL_USERINFO_EDIT = URL_IP + "app/userInfo";
}
