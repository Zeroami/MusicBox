package com.lzb.musicbox.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class ActivityUtils {

	public static List<Activity> activitiyList = new ArrayList<Activity>();
	
	public static void addActivity(Activity activity){
		activitiyList.add(activity);
	}
	
	public static void removeActivity(Activity activity){
		activitiyList.remove(activity);
	}
	
	public static void finisiAllActivity(){
		for(Activity activity : activitiyList){
			if (!activity.isFinishing()) {
				activity.finish();
			}
		}
	}
}
