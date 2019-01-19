package com.example.localmusicplayer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.localmusicplayer.adapter.MusicAdapter;
import com.example.localmusicplayer.base.MyApplication;
import com.example.localmusicplayer.bean.Media;

public class SearchActivity extends Activity  implements OnItemClickListener {
	private ImageView back,sure;
	private EditText key;
	private List<Media> medias_search = new ArrayList<Media>();;
	private ListView lv_search;
	private CustomAsyncQueryHandler asyncQueryHandler;
	private MusicAdapter listViewAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.activity_search);
	    MyApplication.getInstance().addActivity(this);
	    initView();
	}
	private void initView() {
		// TODO �Զ����ɵķ������
		lv_search = (ListView) findViewById(R.id.search_list);
		back = (ImageView) findViewById(R.id.back);
		sure = (ImageView) findViewById(R.id.sure);
		key = (EditText) findViewById(R.id.key);
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO �Զ����ɵķ������
				finish();
			}
		});
		sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO �Զ����ɵķ������
				String str = key.getText().toString();
				if(!TextUtils.isEmpty(str)){
					doSearch(str);
				}
			}
		});
	}
	private void doSearch(String key) {
		asyncQueryHandler = new CustomAsyncQueryHandler(getContentResolver());
		asyncQueryHandler.startQuery(0, lv_search, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, "title like ?",
				new String[] { "%" + key + "%" }, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		lv_search.setOnItemClickListener(this);
	}
	/*************************************************
	 * 
	 * �첽��ѯ������
	 * 
	 * */
	private class CustomAsyncQueryHandler extends AsyncQueryHandler {

		public CustomAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToNext();
				System.out.println(i);
				
				// �����ǰý�����ļ�����500Kb�����������ļ�

				if (cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)) == 1) {

					// cursor.getColumnIndex:�õ�ָ��������������,����˵����ֶ��ǵڼ���
					// cursor.getString(columnIndex) ���Եõ���ǰ�еĵڼ��е�ֵ
					
					Media media = new Media();// ʵ����ý�����
					media.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));// ���ø������
					media.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));// ���õõ���������
					media.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));// ���õõ�����ʱ��
					media.setSinger(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));// ���õõ�������
					media.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));// ���õõ�����·��
					media.setAlbum_id(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
					medias_search.add(media);

				}
			}

			listViewAdapter = new MusicAdapter(SearchActivity.this, medias_search);
			lv_search.setAdapter(listViewAdapter);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String urlStr = medias_search.get(position).getUri();
		String nameStr = medias_search.get(position).getName();
		String singStr = medias_search.get(position).getSinger();
		int duration = medias_search.get(position).getDuration();
		String idStr  = medias_search.get(position).getId();
		Intent intent = getIntent();
		intent.putExtra("id", idStr);
		setResult(102, intent);
		finish();
	}
	
}
