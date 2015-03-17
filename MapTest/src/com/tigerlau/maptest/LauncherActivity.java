package com.tigerlau.maptest;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;

public class LauncherActivity extends FragmentActivity {

	private SupportMapFragment smf;
	private final BaiduMapOptions bmo = new BaiduMapOptions().compassEnabled(
			false).zoomControlsEnabled(false);
	private BaiduMap mBaiduMap;

	private double mLatitude;
	private double mLongitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		if (findViewById(R.id.bmapview_container) != null) {
			if (savedInstanceState != null) {
				smf = SupportMapFragment.newInstance(bmo);
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.bmapview_container, smf).commit();
				return;
			}
			smf = SupportMapFragment.newInstance(bmo);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.bmapview_container, smf).commit();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mBaiduMap = smf.getBaiduMap();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mLatitude = mBaiduMap.getMapStatus().target.latitude;
		mLongitude = mBaiduMap.getMapStatus().target.longitude;
		outState.putDouble("Latitude", mLatitude);
		outState.putDouble("Longitude", mLongitude);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mLatitude = savedInstanceState.getDouble("Latitude");
		mLongitude = savedInstanceState.getDouble("Longitude");
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(
				mLatitude, mLongitude)));
	}
}
