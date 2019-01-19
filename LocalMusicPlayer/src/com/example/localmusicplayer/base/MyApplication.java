/**
 * 
 */
package com.example.localmusicplayer.base;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

/**
 * 安全退出程序
 *
 */
public class MyApplication extends Application {
	private List<Activity> lists = new ArrayList<Activity>();
	private static MyApplication instance;

	private MyApplication() {

	}

	public static MyApplication getInstance() {
		if (instance == null) {
			instance = new MyApplication();
		}
		return instance;
	}

	// 对外添加Activity的方法
	public void addActivity(Activity activity) {
		lists.add(activity);
	}

	// 对外暴露关闭Activity的方法
	public void finishApp() {
		for (Activity activity : lists) {
			activity.finish();
		}
	}
}
