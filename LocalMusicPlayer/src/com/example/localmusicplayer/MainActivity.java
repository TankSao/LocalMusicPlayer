package com.example.localmusicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.localmusicplayer.adapter.MyListViewAdapter;
import com.example.localmusicplayer.adapter.MyViewPagerAdapter;
import com.example.localmusicplayer.base.MyApplication;
import com.example.localmusicplayer.bean.Media;
import com.example.localmusicplayer.util.CharacterParser;
import com.example.localmusicplayer.util.Config;
import com.example.localmusicplayer.util.MusicService;
import com.example.localmusicplayer.util.PinyinComparator;
import com.example.localmusicplayer.view.SideBar;
import com.example.localmusicplayer.view.SideBar.OnTouchingLetterChangedListener;


public class MainActivity extends ActionBarActivity  implements OnSeekBarChangeListener, OnItemClickListener {

	/**
	 * ��ȡ�ֻ���Ļ���
	 */
	private int screenHeight, screenWidth;
	/**
	 * flag ��ͣ���Ǽ����ı�־
	 */
	private boolean flag = true;

	/**
	 * ý���ļ�����ļ���
	 */
	private List<Media> medias;

	/**
	 * ViewPager��view����
	 */
	private List<View> views; //
	/**
	 * ���ذ�ť
	 */
	private ImageView back;

	/**
	 * �������ڵ��б�
	 */
	private ListView listView;

	/**
	 * viewPager�е�view
	 */
	private View view1,view2;
	/**
	 * ����/��ͣ�����ư�ť
	 */
	private ImageView control;

	/**
	 * ������ť
	 */
	private ImageView search;

	/**
	 * �������
	 */
	private int position;
	/**
	 * viewPager�Ķ���
	 */
	private ViewPager viewPager;

	/**
	 * ���������
	 */
	private LayoutInflater mInflater;

	/**
	 * service������ broadcast���㲥��ͼ����
	 */
	private Intent service, broadcast;
	/**
	 * ��ĸ����view����
	 */
	private SideBar sideBarView;
	private MyListViewAdapter adapter;
	private CharacterParser characterParser;
	private PinyinComparator pinyinComparator;
	private MyBroadcastReceiver broadcastReceiver;// �㲥
	private IntentFilter filter;// �㲥������
	private TextView play_Title, play_Artist, text_select, music_current_time, music_always_time;
	private ImageView iv_special, Iv_select_bg;
	private TextView dialog;
	/**
	 * ��Ƶ�ļ�����
	 */
	private Media media;// ��Ƶ�ļ�����

	/**
	 * ���Ž�����
	 */
	private SeekBar sb_music;

	/**
	 * time��һ�ΰ�backʱ�䣻
	 */
	private long time = 0; //
	private ImageView toPage1,toPage2;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        MyApplication.getInstance().addActivity(this);
        initAct();
    }


    private void initAct() {
		// TODO �Զ����ɵķ������
    	back = (ImageView) findViewById(R.id.back);
    	back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO �Զ����ɵķ������
				MyApplication.getInstance().finishApp();
			}
		});
    	viewPager = (ViewPager) findViewById(R.id.vp);
		medias = new ArrayList<Media>();
		views = new ArrayList<View>();
		mInflater = LayoutInflater.from(this);
		control = (ImageView) findViewById(R.id.control);
		search = (ImageView) findViewById(R.id.search);
		search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO �Զ����ɵķ������
				Intent intent = new Intent(MainActivity.this,SearchActivity.class);
				startActivityForResult(intent, 101);
			}
		});
		// ������ĸ��������
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();

		// ����һ���㲥
		broadcastReceiver = new MyBroadcastReceiver();

		// �����㲥������
		filter = new IntentFilter();
		filter.addAction(Config.ACTION_PlAYING_STATE);
		filter.addAction(Config.ACTION_SERVICR_PUASE);
		filter.addAction(Config.ACTION_MUSIC_PLAN);
		filter.addAction(Config.ACTION_PLAY);
		registerReceiver(broadcastReceiver, filter);
		loadData();
		initView();
		MyViewPagerAdapter mvp = new MyViewPagerAdapter(views);
		viewPager.setAdapter(mvp);

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// �ж���ĸ����0��ʾ��1����
				Config.viewPage = 1;
			}
		});

		/**********************************************************************
		 * 
		 * ��һ�ν��룬������Ű�ť��ֱ�Ӳ��ŵ�һ�׸��� ��������
		 */
		Intent service = new Intent(this, MusicService.class);
		service.putParcelableArrayListExtra("medias", (ArrayList<? extends Parcelable>) medias);
		startService(service);
	}


	private void initView() {
		// TODO �Զ����ɵķ������
		view1 = mInflater.inflate(R.layout.view_pager1, null);
		toPage2 = (ImageView) view1.findViewById(R.id.toPage2); 
		toPage2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO �Զ����ɵķ������
				viewPager.setCurrentItem(1);
			}
		});
		sideBarView = (SideBar) view1.findViewById(R.id.SideBarView);// �õ�alphaView���ڵ�id
		dialog = (TextView) view1.findViewById(R.id.dialog);
		sideBarView.setTextView(dialog);

		sideBarView.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					listView.setSelection(position);
				}
			}
		});

		listView = (ListView) view1.findViewById(R.id.lv);
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// ����Ҫ����adapter.getItem(position)����ȡ��ǰposition����Ӧ�Ķ���
				Toast.makeText(getApplication(), ((Media) adapter.getItem(position)).getName(), Toast.LENGTH_SHORT)
						.show();
				return false;
			}
		});

		Collections.sort(medias, pinyinComparator);
		adapter = new MyListViewAdapter(this, medias);
		listView.setAdapter(adapter);
		views.add(view1);

		/*********************************************************
		 * */

		listView.setOnItemClickListener(this);// ���ListView���������¼�

		/**
		 * �������Ž���
		 */
		view2 = mInflater.inflate(R.layout.view_pager2, null);
		toPage1 = (ImageView) view2.findViewById(R.id.toPage1); 
		toPage1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO �Զ����ɵķ������
				viewPager.setCurrentItem(0);
			}
		});
		music_current_time = (TextView) view2.findViewById(R.id.music_current_time);// �����Ĳ��ŵ���ǰ�Ľ���
		music_always_time = (TextView) view2.findViewById(R.id.music_always_time);// �������ܳ���
		sb_music = (SeekBar) view2.findViewById(R.id.sb_music);// ������
		sb_music.setOnSeekBarChangeListener(this);

		/**
		 * ʵ�������Ž���ؼ�
		 */
		iv_special = (ImageView) view2.findViewById(R.id.infoOperating);// ר��ͼƬ
		play_Title = (TextView) view2.findViewById(R.id.play_Title);// ���Ž���ĸ�������
		play_Artist = (TextView) view2.findViewById(R.id.play_Artist);// ���Ž���ĸ���������

		views.add(view2);

		setPlayText(0);// ����Ĭ�ϲ���Ϊ��һ�׸�
	}


	private void loadData() {
		// TODO �Զ����ɵķ������
		List<Media> mList = new ArrayList<Media>();

		ContentResolver resolver = this.getContentResolver();

		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		if (cursor != null && cursor.getCount() > 0) {
		}
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext();

			// �����ǰý�����ļ�����500Kb�����������ļ�
			if (cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)) >= 500 * 1024
					&& cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)) == 1) {

				// cursor.getColumnIndex:�õ�ָ��������������,����˵����ֶ��ǵڼ���
				// cursor.getString(columnIndex) ���Եõ���ǰ�еĵڼ��е�ֵ

				// ���ø������
				String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
				// ���õõ�����ʱ��
				int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
				// ���õõ�������
				String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				// ���õõ�����·��
				String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
				// ���õõ�ר��ͼƬID
				int album_id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				// ���õõ���������
				String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));

				// ����ת����ƴ��
				Media m = new Media();
				m.setId(id);
				m.setDuration(duration);
				m.setSinger(singer);
				m.setUri(uri);
				m.setAlbum_id(album_id);
				m.setName(name);
				medias.add(m);

				Log.v("TAG", "�õ���������Ϣ��" + m.toString());

				String key = characterParser.getSelling(name);
				Log.v("TAG", "ƴ����" + key);
				String sortString = key.substring(0, 1).toUpperCase();

				// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
				if (sortString.matches("[A-Z]")) {
					m.setKey(sortString.toUpperCase());
				} else {
					m.setKey("#");
				}
				mList.add(m);
			}
		}

		// �õ���Ļ�߿�
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		screenWidth = outMetrics.widthPixels;
		screenHeight = outMetrics.heightPixels;
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /*************************************************************
	 * 
	 * ��һ��
	 */
	public void click_last(View view) {
		// �����ǰ���Ÿ����ǵ�һ�ף����������һ��
		if (position == 0) {
			position = medias.size() - 1;

		} else {
			position -= 1;
		}
		music_play(position, Config.ACTION_LAST);
	}

	/**************************************************************
	 * 
	 * ����/��ͣ
	 */
	public void click_pause(View view) {
		if (flag) {
			// ���Ͳ��Ź㲥
			sendBroadcastToService(Config.ACTION_PLAY, 0, null);
			control.setImageResource(R.drawable.apollo_holo_dark_pause);
		} else {
			// ������ͣ�㲥
			sendBroadcastToService(Config.ACTION_PAUSE, 0, null);
			control.setImageResource(R.drawable.apollo_holo_dark_play);
		}
		flag = !flag;
	}

	/**************************************************************
	 * 
	 * ��һ��
	 */
	public void click_next(View view) {
		// �����ǰ���Ÿ�����λ�������һ�ף���������һ��
		if (position == medias.size() - 1) {
			position = 0;
		} else {
			position += 1;
		}
		music_play(position, Config.ACTION_NEXT);
	}
    /*********************************************************************
	 * 
	 * @param index
	 *            �����������±�
	 * @param action
	 *            ��������
	 */
	public void music_play(int index, String action) {
		broadcast = new Intent();
		broadcast.setAction(action);
		broadcast.putExtra("index", index);
		sendBroadcast(broadcast);
		if (flag) {
			flag = !flag;
		}
	}
    /*************************************************************
	 * 
	 * ListView���������¼�
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		music_play(position, Config.ACTION_LIST);
	}
    public void setPlayText(int index) {
		play_Title.setText(medias.get(index).getName());
		play_Artist.setText(medias.get(index).getSinger());
	}
    /************************************************************
	 * 
	 * ���Ž���㲥������
	 */
	public class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Config.ACTION_PlAYING_STATE.equals(intent.getAction())) {

				// ��ȡ����ʼ���ŵĹ㲥
				int index = intent.getIntExtra("media", 0);// �õ����񷢹������ŵ�ǰ�����±�
				position = index;

				// �����±���Ϣ
				setPlayText(index);

				// �յ�������Ϣ���ı䰴ť��ʶ
				control.setImageResource(R.drawable.apollo_holo_dark_pause);

				// ���õ�ǰ������ʱ��
				music_always_time.setText(timeconvert(medias.get(index).getDuration()));

				// ���õ�ǰ�������������ֵ
				sb_music.setMax(medias.get(index).getDuration());

				// ͨ���±��õ���ǰ���񲥷Ÿ�����ר��ͼƬ������Դ�е�id
				int album_id = medias.get(index).getAlbum_id();

				Log.v("TAG", "album_id��û��:" + album_id + "");

				// ��ͨ����Դid�õ�ר����ʵ��·��
				String albumArt = getAlbumArt(album_id);
				Log.v("TAG", "ר��ʵ��·����û��:" + albumArt);

				// ���ר��·���ǿգ�������Ĭ��ͼƬ
				if (albumArt == null) {
					iv_special.setImageResource(R.drawable.me);
				} else {
					// ��BitmapFactory.decodeFile�õ�����λͼ

					Bitmap btm = BitmapFactory.decodeFile(albumArt);
					Log.v("TAG", "bm��û��:" + btm);
					if (btm != null) {
						// ����ͼƬ��ʽ
						BitmapDrawable bmpDraw = new BitmapDrawable(btm);
						Log.v("TAG", "bmpDraw��û��:" + bmpDraw);
						// ����ר��ͼƬ
						iv_special.setImageDrawable(bmpDraw);
					} else {
						iv_special.setImageResource(R.drawable.me);
					}

				}
				// ������ʼ�������ֺ󣬽�ר��ͼƬ�����ת����Ч��
				Animation operatingAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.tip);
				LinearInterpolator lin = new LinearInterpolator();
				operatingAnim.setInterpolator(lin);

				if (operatingAnim != null) {
					iv_special.startAnimation(operatingAnim);
				}

			} else if (Config.ACTION_SERVICR_PUASE.equals(intent.getAction())) {

				// �ո�����ͣ�㲥���Ƴ�ר��imageView�Ķ���
				iv_special.clearAnimation();

			} else if (Config.ACTION_MUSIC_PLAN.equals(intent.getAction())) {

				// �յ������͵Ĳ��Ÿ������ȵ���ͼ
				int playerPosition = intent.getIntExtra("playerPosition", 0);
				String playerTime = timeconvert(playerPosition);
				Log.v("jindu", "�������Ľ�����ֵ:" + playerTime);
				sb_music.setProgress(playerPosition);// ���ý���������
				music_current_time.setText(playerTime);// ���ý���������ʱ��
				sb_music.invalidate();// �Զ�ˢ������������

			} else if (Config.ACTION_PLAY.equals(intent.getAction())) {
				Log.v("TAG", "������û��:" + intent.getAction());
				if (flag) {
					control.setImageResource(R.drawable.apollo_holo_dark_pause);
					
				}else {
					control.setImageResource(R.drawable.apollo_holo_dark_play);
				}
				flag = !flag;
				
			} 
		}

		/*********************************************************
		 * 
		 * ��ȡר��ͼƬʵ�ʵ�ַ����
		 */
		private String getAlbumArt(int album_id) {
			String mUriAlbums = "content://media/external/audio/albums";
			String[] projection = new String[] { "album_art" };
			Cursor cur = MainActivity.this.getContentResolver()
					.query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);
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
    
	/*************************************************************
	 * 
	 * ����ʱ���ʽת��
	 */
	public String timeconvert(int time) {
		int min = 0, hour = 0;
		time /= 1000;
		min = time / 60;
		time %= 60;
		return min + ":" + time;
	}

	/******************************************************************
	 * 
	 * �������͹㲥����
	 */
	public void sendBroadcastToService(String action, int intExtra, String stringExtra) {
		broadcast = new Intent();
		broadcast.setAction(action);
		broadcast.putExtra("index", intExtra);
		broadcast.putExtra("date", stringExtra);
		sendBroadcast(broadcast);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	/************************************************************
	 * 
	 * �ֶ����ڽ�����
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		int mediaPlayer = (seekBar.getProgress());
		sendBroadcastToService(Config.ACTION_PLAN_CURRENT, mediaPlayer, null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**************************************************************
	 * 
	 * ˫���˳�
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - time > 1000)) {
				Toast.makeText(this, "�ٰ�һ�η�������", Toast.LENGTH_SHORT).show();
				time = System.currentTimeMillis();
			} else {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);

				startActivity(intent);
			}
			return true;
		}else {
			return super.onKeyDown(keyCode, event);
		}

	}
	
	@Override
	protected void onActivityResult(int request, int response, Intent intent) {
		// TODO �Զ����ɵķ������
		super.onActivityResult(request, response, intent);
		if(request == 101 && response == 102){
			String id = intent.getStringExtra("id");
			int index = 0;
			for(int i = 0;i<medias.size();i++){
				if(medias.get(i).getId().equals(id)){
					index = i;
					break;
				}
			}
			music_play(index, Config.ACTION_LIST);
			viewPager.setCurrentItem(1);
		}
	}
    
}
