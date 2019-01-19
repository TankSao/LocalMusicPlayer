/**
 * 
 */
package com.example.localmusicplayer.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 媒体文件的实体类，包含媒体文件name，id, url, duration；
 */
public class Media implements Parcelable, Comparable<Media> {
	private String name;
	private String id;
	private String uri;
	private int duration;
	private String singer;
	private int album_id;
	private String key;

	public Media() {

	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getAlbum_id() {
		return album_id;
	}

	public void setAlbum_id(int album_id) {
		this.album_id = album_id;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "Media [name=" + name + ", id=" + id + ", uri=" + uri + ", duration=" + duration + ", singer=" + singer
				+ ", album_id=" + album_id + ", key=" + key + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(uri);
		dest.writeInt(duration);
		dest.writeString(singer);
		dest.writeInt(album_id);
		dest.writeString(key);

	}

	public static final Parcelable.Creator<Media> CREATOR = new Creator<Media>() {
		@Override
		public Media[] newArray(int size) {
			return new Media[size];
		}

		@Override
		public Media createFromParcel(Parcel in) {
			return new Media(in);
		}
	};

	public Media(Parcel in) {
		id = in.readString();
		name = in.readString();
		uri = in.readString();
		duration = in.readInt();
		singer = in.readString();
		album_id = in.readInt();
		key = in.readString();
	}

	@Override
	public int compareTo(Media another) {

		/*
		 * 如果当前对象比传入大于另外一个对象 返回 正数1 
		 * 如果当前对象比传入等于另外一个对象 返回 正数0 
		 * 如果当前对象比传入小于另外一个对象
		 * 返回 正数-1 字符串之间能够比较吗？可以！ 因为系统已经在String类中帮我们实现了Comparable return
		 * this.age>anther.age? 1: this.age==anther.age? 0:-1;
		 * 
		 * if (this.age > anthor.age) return 1; else if (this.age==anthor.age)
		 * return 0; else return -1;
		 */
		// 可以把这句话理解为比较两个联系人的大小，简化为两个key之间的比较
		int result = this.key.compareTo(another.key);
		if (result == 0) {
			return this.name.compareTo(another.name);
		}
		return result;
	}

}
