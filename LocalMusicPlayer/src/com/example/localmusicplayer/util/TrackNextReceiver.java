package com.example.localmusicplayer.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 * 通知栏下一曲，广播接收器
 * */
public class TrackNextReceiver extends BroadcastReceiver {
	
	private int position;
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (MusicService.position == MusicService.medias.size() - 1) {
			MusicService.position = 0;
		} else {
			position = MusicService.position++;
		}
		
		Intent newIntent = new Intent();
		newIntent.setAction(Config.ACTION_NEXT);
		newIntent.getIntExtra("index", position);
		context.sendBroadcast(newIntent);
		
	}

}
