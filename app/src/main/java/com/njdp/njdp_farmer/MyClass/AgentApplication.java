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

    public static void addActivity(Activity activity) {
        if(activities.indexOf(activity) < 0)
            activities.add(activity);
    }

    public static void removeActivity(Activity activity){
        activities.remove(activity);
        activity.finish();
        System.gc();
        System.runFinalization();
    }

    public static void ExitApp() {

        for (Activity activity : activities) {
            activity.finish();
        }

        System.exit(0);
    }
}
