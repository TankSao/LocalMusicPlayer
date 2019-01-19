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
	 * 获取手机屏幕宽高
	 */
	private int screenHeight, screenWidth;
	/**
	 * flag 暂停还是继续的标志
	 */
	private boolean flag = true;

	/**
	 * 媒体文件对象的集合
	 */
	private List<Media> medias;

	/**
	 * ViewPager的view集合
	 */
	private List<View> views; //
	/**
	 * 返回按钮
	 */
	private ImageView back;

	/**
	 * 歌曲所在的列表
	 */
	private ListView listView;

	/**
	 * viewPager中的view
	 */
	private View view1,view2;
	/**
	 * 播放/暂停，控制按钮
	 */
	private ImageView control;

	/**
	 * 搜索按钮
	 */
	private ImageView search;

	/**
	 * 歌曲标记
	 */
	private int position;
	/**
	 * viewPager的对象
	 */
	private ViewPager viewPager;

	/**
	 * 布局填充器
	 */
	private LayoutInflater mInflater;

	/**
	 * service：服务 broadcast：广播意图对象
	 */
	private Intent service, broadcast;
	/**
	 * 字母索引view对象
	 */
	private SideBar sideBarView;
	private MyListViewAdapter adapter;
	private CharacterParser characterParser;
	private PinyinComparator pinyinComparator;
	private MyBroadcastReceiver broadcastReceiver;// 广播
	private IntentFilter filter;// 广播过滤器
	private TextView play_Title, play_Artist, text_select, music_current_time, music_always_time;
	private ImageView iv_special, Iv_select_bg;
	private TextView dialog;
	/**
	 * 音频文件对象
	 */
	private Media media;// 音频文件对象

	/**
	 * 播放进度条
	 */
	private SeekBar sb_music;

	/**
	 * time第一次按back时间；
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
		// TODO 自动生成的方法存根
    	back = (ImageView) findViewById(R.id.back);
    	back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
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
				// TODO 自动生成的方法存根
				Intent intent = new Intent(MainActivity.this,SearchActivity.class);
				startActivityForResult(intent, 101);
			}
		});
		// 创建字母索引工具
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();

		// 创建一个广播
		broadcastReceiver = new MyBroadcastReceiver();

		// 创建广播过滤器
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
				// 判断字母索引0显示和1隐藏
				Config.viewPage = 1;
			}
		});

		/**********************************************************************
		 * 
		 * 第一次进入，点击播放按钮，直接播放第一首歌曲 启动服务
		 */
		Intent service = new Intent(this, MusicService.class);
		service.putParcelableArrayListExtra("medias", (ArrayList<? extends Parcelable>) medias);
		startService(service);
	}


	private void initView() {
		// TODO 自动生成的方法存根
		view1 = mInflater.inflate(R.layout.view_pager1, null);
		toPage2 = (ImageView) view1.findViewById(R.id.toPage2); 
		toPage2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
				viewPager.setCurrentItem(1);
			}
		});
		sideBarView = (SideBar) view1.findViewById(R.id.SideBarView);// 拿到alphaView所在的id
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
				// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
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

		listView.setOnItemClickListener(this);// 添加ListView单击触控事件

		/**
		 * 歌曲播放界面
		 */
		view2 = mInflater.inflate(R.layout.view_pager2, null);
		toPage1 = (ImageView) view2.findViewById(R.id.toPage1); 
		toPage1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
				viewPager.setCurrentItem(0);
			}
		});
		music_current_time = (TextView) view2.findViewById(R.id.music_current_time);// 歌曲的播放到当前的进度
		music_always_time = (TextView) view2.findViewById(R.id.music_always_time);// 歌曲的总长度
		sb_music = (SeekBar) view2.findViewById(R.id.sb_music);// 进度条
		sb_music.setOnSeekBarChangeListener(this);

		/**
		 * 实例化播放界面控件
		 */
		iv_special = (ImageView) view2.findViewById(R.id.infoOperating);// 专辑图片
		play_Title = (TextView) view2.findViewById(R.id.play_Title);// 播放界面的歌曲标题
		play_Artist = (TextView) view2.findViewById(R.id.play_Artist);// 播放界面的歌曲艺术家

		views.add(view2);

		setPlayText(0);// 设置默认播放为第一首歌
	}


	private void loadData() {
		// TODO 自动生成的方法存根
		List<Media> mList = new ArrayList<Media>();

		ContentResolver resolver = this.getContentResolver();

		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		if (cursor != null && cursor.getCount() > 0) {
		}
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext();

			// 如果当前媒体库的文件大于500Kb，则是音乐文件
			if (cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)) >= 500 * 1024
					&& cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)) == 1) {

				// cursor.getColumnIndex:得到指定列名的索引号,就是说这个字段是第几列
				// cursor.getString(columnIndex) 可以得到当前行的第几列的值

				// 设置歌曲编号
				String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
				// 设置得到歌曲时长
				int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
				// 设置得到艺术家
				String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				// 设置得到歌曲路径
				String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
				// 设置得到专辑图片ID
				int album_id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				// 设置得到歌曲标题
				String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));

				// 汉字转换成拼音
				Media m = new Media();
				m.setId(id);
				m.setDuration(duration);
				m.setSinger(singer);
				m.setUri(uri);
				m.setAlbum_id(album_id);
				m.setName(name);
				medias.add(m);

				Log.v("TAG", "拿到的音乐信息：" + m.toString());

				String key = characterParser.getSelling(name);
				Log.v("TAG", "拼音：" + key);
				String sortString = key.substring(0, 1).toUpperCase();

				// 正则表达式，判断首字母是否是英文字母
				if (sortString.matches("[A-Z]")) {
					m.setKey(sortString.toUpperCase());
				} else {
					m.setKey("#");
				}
				mList.add(m);
			}
		}

		// 拿到屏幕高宽
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
	 * 上一首
	 */
	public void click_last(View view) {
		// 如果当前播放歌曲是第一首，就跳到最后一首
		if (position == 0) {
			position = medias.size() - 1;

		} else {
			position -= 1;
		}
		music_play(position, Config.ACTION_LAST);
	}

	/**************************************************************
	 * 
	 * 播放/暂停
	 */
	public void click_pause(View view) {
		if (flag) {
			// 发送播放广播
			sendBroadcastToService(Config.ACTION_PLAY, 0, null);
			control.setImageResource(R.drawable.apollo_holo_dark_pause);
		} else {
			// 发送暂停广播
			sendBroadcastToService(Config.ACTION_PAUSE, 0, null);
			control.setImageResource(R.drawable.apollo_holo_dark_play);
		}
		flag = !flag;
	}

	/**************************************************************
	 * 
	 * 下一首
	 */
	public void click_next(View view) {
		// 如果当前播放歌曲的位置是最后一首，就跳到第一首
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
	 *            歌曲的索引下标
	 * @param action
	 *            按键动作
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
	 * ListView单击触控事件
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
	 * 播放界面广播接收者
	 */
	public class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Config.ACTION_PlAYING_STATE.equals(intent.getAction())) {

				// 收取服务开始播放的广播
				int index = intent.getIntExtra("media", 0);// 拿到服务发过来播放当前歌曲下标
				position = index;

				// 设置下标信息
				setPlayText(index);

				// 收到播放信息，改变按钮标识
				control.setImageResource(R.drawable.apollo_holo_dark_pause);

				// 设置当前音乐总时长
				music_always_time.setText(timeconvert(medias.get(index).getDuration()));

				// 设置当前歌曲进度条最大值
				sb_music.setMax(medias.get(index).getDuration());

				// 通过下标拿到当前服务播放歌曲的专辑图片所在资源中的id
				int album_id = medias.get(index).getAlbum_id();

				Log.v("TAG", "album_id有没有:" + album_id + "");

				// 再通过资源id拿到专辑的实际路径
				String albumArt = getAlbumArt(album_id);
				Log.v("TAG", "专辑实际路径有没有:" + albumArt);

				// 如果专辑路径是空，就设置默认图片
				if (albumArt == null) {
					iv_special.setImageResource(R.drawable.me);
				} else {
					// 用BitmapFactory.decodeFile拿到具体位图

					Bitmap btm = BitmapFactory.decodeFile(albumArt);
					Log.v("TAG", "bm有没有:" + btm);
					if (btm != null) {
						// 设置图片格式
						BitmapDrawable bmpDraw = new BitmapDrawable(btm);
						Log.v("TAG", "bmpDraw有没有:" + bmpDraw);
						// 设置专辑图片
						iv_special.setImageDrawable(bmpDraw);
					} else {
						iv_special.setImageResource(R.drawable.me);
					}

				}
				// 当服务开始播放音乐后，将专辑图片添加旋转动画效果
				Animation operatingAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.tip);
				LinearInterpolator lin = new LinearInterpolator();
				operatingAnim.setInterpolator(lin);

				if (operatingAnim != null) {
					iv_special.startAnimation(operatingAnim);
				}

			} else if (Config.ACTION_SERVICR_PUASE.equals(intent.getAction())) {

				// 收歌曲暂停广播，移除专辑imageView的动画
				iv_special.clearAnimation();

			} else if (Config.ACTION_MUSIC_PLAN.equals(intent.getAction())) {

				// 收到服务发送的播放歌曲进度的意图
				int playerPosition = intent.getIntExtra("playerPosition", 0);
				String playerTime = timeconvert(playerPosition);
				Log.v("jindu", "服务发来的进度条值:" + playerTime);
				sb_music.setProgress(playerPosition);// 设置进度条进度
				music_current_time.setText(playerTime);// 设置进度条进度时间
				sb_music.invalidate();// 自动刷屏（动起来）

			} else if (Config.ACTION_PLAY.equals(intent.getAction())) {
				Log.v("TAG", "播放有没有:" + intent.getAction());
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
		 * 获取专辑图片实际地址方法
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
	 * 歌曲时间格式转换
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
	 * 给服务发送广播方法
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
	 * 手动调节进度条
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
	 * 双击退出
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - time > 1000)) {
				Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
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
		// TODO 自动生成的方法存根
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
