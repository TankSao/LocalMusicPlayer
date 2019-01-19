/**
 * 
 */
package com.example.localmusicplayer.base;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

/**
 * ��ȫ�˳�����
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

	// �������Activity�ķ���
	public void addActivity(Activity activity) {
		lists.add(activity);
	}

	// ���Ⱪ¶�ر�Activity�ķ���
	public void finishApp() {
		for (Activity activity : lists) {
			activity.finish();
		}
	}
}
