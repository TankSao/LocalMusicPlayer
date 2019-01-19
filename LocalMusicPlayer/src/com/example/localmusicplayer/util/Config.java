package com.example.localmusicplayer.util;

public class Config {
	public static final String ACTION_PLAY = "com.example.localmusicplayer.play"; // 播放
	public static final String ACTION_PAUSE = "com.example.localmusicplayer.pause";// 暂停
	public static final String ACTION_LAST = "com.example.localmusicplayer.last";// 上一个
	public static final String ACTION_NEXT = "com.example.localmusicplayer.next";// 下一个
	public static final String ACTION_LIST = "com.example.localmusicplayer.list";// 单击listView条目
	public static final String ACTION_LIST_SEARCH = "com.example.localmusicplayer.list_search";// 单机搜索界面的listView条目
	public static final String ACTION_PlAYING_STATE = "com.example.localmusicplayer.playing";// 服务发给activity的播放意图
	public static final String ACTION_SERVICR_PUASE = "com.example.localmusicplayer.service.puase";// 服务发给activity的播放意图
	public static final String ACTION_MUSIC_PLAN = "com.example.localmusicplayer.music.plan";// 用来发送歌曲当前播放位置的意图
	public static final String ACTION_PLAN_CURRENT = "com.example.localmusicplayer.plan_current";// 用来发送进度条位置给服务的意图

	public static int viewPage = 0;//当前页
}
