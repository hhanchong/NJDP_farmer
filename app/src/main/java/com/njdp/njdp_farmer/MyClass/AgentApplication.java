package com.njdp.njdp_farmer.MyClass;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/26.
 */
public class AgentApplication extends Application {
    private static List<Activity> activities = new ArrayList<Activity>();

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
