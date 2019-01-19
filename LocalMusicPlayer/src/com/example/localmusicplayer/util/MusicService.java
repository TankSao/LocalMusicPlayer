package com.example.localmusicplayer.util;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.localmusicplayer.R;
import com.example.localmusicplayer.bean.Media;

/**
 * 
 * ��̨����
 */
public class MusicService extends Service implements OnPreparedListener, OnCompletionListener {

	/**
	 * 
	 * listener �绰��������
	 */
	private MyPhoneStateListener listener;

	/**
	 * tm �绰����������
	 */
	private TelephonyManager tm;

	/**
	 * mPlayer ý�岥��������
	 */
	private MediaPlayer mPlayer;

	/**
	 * mbr �Զ���Ĺ㲥������
	 */
	private MyBroadcastReceiver mbr;

	/**
	 * intentFilter ��ͼ����������
	 */
	private IntentFilter intentFilter;

	/**
	 * isFirst_play ��Ĭ��true ����ǵ�һ�ξͻ��������һ�׸����������ִ����ͣ���߲����߼� restart��
	 * �жϵ��绰����֮ǰ�Ƿ��ڲ������ֵı�־�����true�����绰�ҶϺ�ִ�м������ţ����򲻲���
	 */
	private boolean isFirst_play = true, restart = false;

	private boolean isPlay_No = true;
	/**
	 * medias ����ý���ļ��ļ��϶���
	 */
	public static List<Media> medias;

	/**
	 * 
	 * position ��ǰ����·������Դ�����е��±�
	 */
	public static int position;

	/**
	 * MILLISECONDS �߳���ͣʱ��
	 */
	private static final int MILLISECONDS = 500;
	private static int time = 0;

	/**
	 * �����㲥֪ͨ�����ж�ֵ
	 */
	private Intent playIntent, nextIntent;

	private PendingIntent playPendingIntent, nextPendingIntent;

	private RemoteViews mRemoteView;

	/**
	 * notification
	 */
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private int NOTI_ID = 1;

	@Override
	public void onCreate() {
		// ���񴴽�ʱ
		super.onCreate();

		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		// �绰������
		listener = new MyPhoneStateListener();

		// ��ȡ�绰����״̬
		tm.listen(listener, MyPhoneStateListener.LISTEN_CALL_STATE);

		// �����㲥������
		intentFilter = new IntentFilter();

		// �����е�״̬���뵽��������
		intentFilter.addAction(Config.ACTION_PLAY);// ����
		intentFilter.addAction(Config.ACTION_PAUSE);// ��ͣ
		intentFilter.addAction(Config.ACTION_NEXT);// ��һ��
		intentFilter.addAction(Config.ACTION_LAST);// ��һ��
		intentFilter.addAction(Config.ACTION_LIST);// ���listView
		intentFilter.addAction(Config.ACTION_LIST_SEARCH);// �������������listView��Ŀ
		intentFilter.addAction(Config.ACTION_PLAN_CURRENT);// �������ͽ�����λ�ø��������ͼ

		mbr = new MyBroadcastReceiver();// ����һ���㲥������
		registerReceiver(mbr, intentFilter);// ע��㲥�����߲���ӹ���

	}

	/*********************************************************************
	 * 
	 * ��������
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mPlayer = new MediaPlayer();
		medias = intent.getParcelableArrayListExtra("medias");
		initNotification();
		return super.onStartCommand(intent, flags, startId);

	}

	/********************************************************************
	 * 
	 * ��������
	 */
	@Override
	public void onDestroy() {
		isPlay_No = false;
		tm.listen(listener, MyPhoneStateListener.LISTEN_NONE);
		mPlayer.release();
		mPlayer = null;
		cancelNoti();
		super.onDestroy();
	}

	/*****************************************************************
	 * 
	 * ���񱻰�
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/*********************************************************************
	 * 
	 * �Զ���ĵ绰������ ���������绰״̬����������Ӧ����
	 */
	private class MyPhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				// �绰���û��߹Ҷ�ʱ
				if (restart) {
					mPlayer.start();
					updateNotification();
					sendPlayingWord(position);
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// �绰��ͨ��
				updateNotification();
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				// �绰����ʱ
				if (mPlayer.isPlaying()) {
					mPlayer.pause();
					updateNotification();
					sendPause();
					restart = true;
				}
				break;
			}
		}
	}

	/****************************************************************
	 * 
	 * �Զ���㲥�����ߡ������������Ӧ�Ĺ㲥��ִ����Զ���
	 */
	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Config.ACTION_PLAY.equals(intent.getAction())) {

				// �����������ǰû�в�������
				if (!mPlayer.isPlaying()) {
					if (isFirst_play) {
						position = intent.getIntExtra("index", 0);
						prepareMusic(position);
						isFirst_play = false;
					} else {
						mPlayer.start();
						sendCurrentPosition();
						updateNotification();
						sendPlayingWord(position);// ���Ͳ�����Ϣ
					}
				} else {
					mPlayer.pause();
					updateNotification();
					sendPause();
				}
			} else if (Config.ACTION_PAUSE.equals(intent.getAction())) {
				// ��ͣ
				if (mPlayer.isPlaying()) {
					mPlayer.pause();
					updateNotification();
					sendPause();
				}
			} else if (Config.ACTION_NEXT.equals(intent.getAction())) {
				// ��һ��
				if (isFirst_play) {
					position = intent.getIntExtra("index", 0);
					updateNotification();
					prepareMusic(position);
					isFirst_play = false;
				} else {
					position = intent.getIntExtra("index", position);
					updateNotification();
					prepareMusic(position);
				}
			} else if (Config.ACTION_LAST.equals(intent.getAction())) {
				// ��һ��
				if (isFirst_play) {
					position = intent.getIntExtra("index", 0);
					updateNotification();
					prepareMusic(position);
					isFirst_play = false;
				} else {
					position = intent.getIntExtra("index", position);
					updateNotification();
					prepareMusic(position);
				}
			} else if (Config.ACTION_LIST.equals(intent.getAction())) {
				// ������Ŀ
				if (isFirst_play) {
					position = intent.getIntExtra("index", 0);
					updateNotification();
					prepareMusic(position);
					isFirst_play = false;
				} else {
					position = intent.getIntExtra("index", position);
					updateNotification();
					prepareMusic(position);
				}
			} else if (Config.ACTION_LIST_SEARCH.equals(intent.getAction())) {
				// ���������������Ŀ
				String id = intent.getStringExtra("id");
				for (int i = 0; i < medias.size(); i++) {
					if (id.equals(medias.get(i).getId())) {
						position = i;
						prepareMusic(position);
						break;
					}
				}

			} else if (Config.ACTION_PLAN_CURRENT.equals(intent.getAction())) {
				// �϶�������
				mPlayer.seekTo(intent.getIntExtra("index", 0));
			}
			System.out.println("�ҽ��ܵ���ʲô��" + intent.getAction().toString());
			System.out.println("�ҵķ���״̬��" + playIntent + "//" + nextIntent + "====");
		}
	}

	/******************************************************************
	 * 
	 * ׼������ index ��Ҫ׼�����ļ��±�
	 */
	private void prepareMusic(int index) {
		mPlayer.reset();
		String music_uri = medias.get(index).getUri();
		Uri songUri = Uri.parse(music_uri);
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mPlayer.setDataSource(getApplicationContext(), songUri);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnCompletionListener(this);
		mPlayer.prepareAsync();
		updateNotification();
	}

	/******************************************************************
	 * 
	 * ����׼����ɵĻص�����
	 */
	@Override
	public void onPrepared(MediaPlayer mp) {
		mPlayer.start();
		isFirst_play = false;
		sendPlayingWord(position);
		sendCurrentPosition();
		updateNotification();
	}

	/***********************************************************************
	 * 
	 * ��ǰ����������ϵļ����ص�
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mp != null) {
			if (position == medias.size() - 1) {
				position = 0;
			} else {
				position = position + 1;
			}
			prepareMusic(position);
		}
	}

	/*****************************************************************************
	 * 
	 * position
	 * 
	 * ��ǰ������λ�ã� ÿ������ʼ�������ֵ�ʱ��ͽ���ǰ���ŵĸ�������activity�� �������ò��Ÿ����������Ϣ
	 */
	public void sendPlayingWord(int position) {
		Intent intent = new Intent();
		intent.setAction(Config.ACTION_PlAYING_STATE);
		intent.putExtra("media", position);
		sendBroadcast(intent);
	}

	/***************************************************************************
	 * 
	 * ���͹㲥��activity�����Ѿ���ͣ
	 */
	public void sendPause() {
		Intent intent = new Intent();
		intent.setAction(Config.ACTION_SERVICR_PUASE);
		sendBroadcast(intent);
		updateNotification();
	}

	/*****************************************************************************
	 * 
	 * ѭ����ȡ��ǰ�������ŵĽ��Ȳ�ͨ���㲥���͸�activity
	 */
	public void sendCurrentPosition() {
		new Thread() {
			public void run() {
				Intent intent = new Intent();
				while (isPlay_No) {
					int playerPosition = mPlayer.getCurrentPosition();
					intent.setAction(Config.ACTION_MUSIC_PLAN);
					intent.putExtra("playerPosition", playerPosition);
					sendBroadcast(intent);
					Log.v("jindu", "��������Ľ�����ֵ1:" + playerPosition);
					try {
						sleep(MILLISECONDS);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();

	}

	/***********************************************************************
	 * 
	 * �Զ���֪ͨ��
	 */
	public void initNotification() {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = new Notification();
		mNotification.icon = R.drawable.icon;
		mNotification.tickerText = "��ӭʹ�ù������֣�";
		// �����Զ���֪ͨ��ͼ
		mRemoteView = new RemoteViews(getPackageName(), R.layout.layout_notification);
		mRemoteView.setImageViewResource(R.id.iv_art_noti, R.drawable.stat_notify);
		mRemoteView.setTextViewText(R.id.noti_title, medias.get(position).getName());
		mRemoteView.setTextViewText(R.id.noti_small_title, medias.get(position).getSinger());
		mRemoteView.setImageViewResource(R.id.btn_noti_pause, R.drawable.notification_play);
		mRemoteView.setImageViewResource(R.id.btn_noti_next, R.drawable.notification_next);
		mNotification.contentView = mRemoteView;

		if (playIntent == null) {
			playIntent = new Intent(this, TrackPlayReceiver.class);
		}
		if (nextIntent == null) {
			nextIntent = new Intent(this, TrackNextReceiver.class);
		}
		if (playPendingIntent == null) {
			playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0);
		}
		if (nextPendingIntent == null) {
			nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
		}

		// �����¼�
		mRemoteView.setOnClickPendingIntent(R.id.btn_noti_next, nextPendingIntent);
		mRemoteView.setOnClickPendingIntent(R.id.btn_noti_pause, playPendingIntent);

		// �����ת��Ӧ�ó���
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(new ComponentName(getPackageName(), "com.lanren.music.MainActivity"));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 100, intent, 0);
		// ������¼�
		mNotification.contentIntent = contentIntent;
		// ���֪֮ͨ����ʧ
		mNotification.flags = Notification.FLAG_NO_CLEAR;
		mNotificationManager.notify(NOTI_ID, mNotification);
	}

	/********************************************************************
	 * 
	 * ����֪ͨ����
	 */
	public void updateNotification() {
		Log.i("TAG", "update notification");
		mRemoteView.setTextViewText(R.id.noti_title, medias.get(position).getName());

		System.out.println("isPlay:" + mPlayer.isPlaying());

		if (mPlayer.isPlaying()) {
			mRemoteView.setImageViewResource(R.id.btn_noti_pause, R.drawable.notification_pause);

		} else {

			mRemoteView.setImageViewResource(R.id.btn_noti_pause, R.drawable.notification_play);

		}

		// ��ȡר��
		int Album = medias.get(position).getAlbum_id();
		String img = getAlbumArt(Album);
		Bitmap bm = null;
		System.out.println("Album:" + img);
		if (img != null) {
			bm = BitmapFactory.decodeFile(img);
			if (bm != null) {
				// ����ͼƬ��ʽ
				BitmapDrawable bmpDraw = new BitmapDrawable(bm);
				Log.v("TAG", "bmpDraw��û��:" + bmpDraw);
				// ����ר��ͼƬ
				mRemoteView.setImageViewBitmap(R.id.iv_art_noti, bm);
			} else {
				mRemoteView.setImageViewResource(R.id.iv_art_noti, R.drawable.me);
			}
		} else {
			mRemoteView.setImageViewResource(R.id.iv_art_noti, R.drawable.me);
		}
		mNotificationManager.notify(NOTI_ID, mNotification);
	}

	public void cancelNoti() {
		mNotificationManager.cancel(NOTI_ID);
	}

	/**********************************************************************
	 * 
	 * �õ�ר��ͼƬ��·��
	 */
	private String getAlbumArt(int album_id) {
		String mUriAlbums = "content://media/external/audio/albums";
		String[] projection = new String[] { "album_art" };
		Cursor cur = this.getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
				projection, null, null, null);
		String album_art = null;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_art = cur.getString(0);
		}
		cur.close();
		cur = null;
		return album_art;
	}

}