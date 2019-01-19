package com.example.localmusicplayer.adapter;

import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.localmusicplayer.R;
import com.example.localmusicplayer.bean.Media;

public class MusicAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Media> medias;
	private Context context;
	private ContentResolver mResolver;

	public MusicAdapter(Context context, List<Media> medias) {
		this.medias = medias;
		this.context = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return medias == null ? 0 : medias.size();
	}

	@Override
	public Object getItem(int position) {
		return medias.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.music_item, null);
			holder = new ViewHolder();
			holder.songId = (TextView) convertView.findViewById(R.id.music_item_id);
			holder.songName = (TextView) convertView.findViewById(R.id.music_item_songName);
			holder.singer = (TextView) convertView.findViewById(R.id.music_item_singer);
			holder.songTime = (TextView) convertView.findViewById(R.id.music_item_time);
			holder.login = (ImageView) convertView.findViewById(R.id.music_login);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		int Album = medias.get(position).getAlbum_id();
		String albumArt = getAlbumArt(Album);
		Bitmap bm = null;
		
		if (albumArt == null) {
			holder.login.setImageResource(R.drawable.name_login);
		} else {
			bm = BitmapFactory.decodeFile(albumArt);
			if (bm != null) {
				holder.login.setImageBitmap(bm);
			}else {
				holder.login.setImageResource(R.drawable.name_login);
			}
		}
		holder.songName.setText(medias.get(position).getName());
		holder.songId.setText("" + (position + 1) + ".");
		holder.singer.setText(medias.get(position).getSinger());
		holder.songTime.setText(timeconvert(medias.get(position).getDuration()));
		System.out.println(medias.get(position).getDuration() + "ss");
		return convertView;
	}

	private class ViewHolder {
		public TextView songName, songId, singer, songTime, catalog;
		public ImageView login;
	}

	// 歌曲时间格式转换
	public String timeconvert(int time) {
		int min = 0, hour = 0;
		time /= 1000;
		min = time / 60;
		time %= 60;
		return min + ":" + time;
	}

	/**
	 * 拿到专辑图片的路径
	 */
	private String getAlbumArt(int album_id) {
		mResolver = context.getContentResolver();
		String mUriAlbums = "content://media/external/audio/albums";
		String[] projection = new String[] { "album_art" };
		Cursor cur = this.mResolver.query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null,
				null, null);
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
