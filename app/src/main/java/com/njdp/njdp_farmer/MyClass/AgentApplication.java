package com.njdp.njdp_farmer.MyClass;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/26.
 * 公用数据缓存类
 */
public class AgentApplication extends Application {
    private static List<Activity> activities = new ArrayList<>();      //页面缓存
    public static ArrayList<FarmlandInfo> farmlandInfos = new ArrayList<>();   //农田数据缓存
    public static List<MachineInfo> machinesToShow = new ArrayList<>();        //需要显示的农机
    private static int gcCount = 0;    //手动释放内存计数

    public static void addActivity(Activity activity) {
        if(activities.indexOf(activity) < 0)
            activities.add(activity);
    }

    public static void removeActivity(Activity activity){
        activities.remove(activity);
        activity.finish();
        if(gcCount > 5) {
            System.gc();
            System.runFinalization();
            gcCount = 0;
        }
        gcCount++;
    }

    //退出应用，销毁加载的页面
    public static void ExitApp() {

        for (Activity activity : activities) {
            activity.finish();
        }

        System.exit(0);
    }
}
