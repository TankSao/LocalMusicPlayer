package com.example.localmusicplayer.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * 
 * 通知栏播放，广播接收器
 * */
public class TrackPlayReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent newIntent = new Intent();
        newIntent.setAction(Config.ACTION_PLAY);
        context.sendBroadcast(newIntent);
	}

}
