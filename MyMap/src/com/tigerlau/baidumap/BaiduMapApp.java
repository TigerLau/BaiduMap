package com.tigerlau.baidumap;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;

public class BaiduMapApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(this);
	}

}
