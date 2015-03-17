package com.tigerlau.maptest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
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
	private float mZoom;
	private float mRotate;
	private float mOverlook;

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
		mZoom = mBaiduMap.getMapStatus().zoom;
		mRotate = mBaiduMap.getMapStatus().rotate;
		mOverlook = mBaiduMap.getMapStatus().overlook;
		outState.putDouble("Latitude", mLatitude);
		outState.putDouble("Longitude", mLongitude);
		outState.putFloat("Zoom", mZoom);
		outState.putFloat("Rotate", mRotate);
		outState.putFloat("Overlook", mOverlook);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mLatitude = savedInstanceState.getDouble("Latitude");
		mLongitude = savedInstanceState.getDouble("Longitude");
		mZoom = savedInstanceState.getFloat("Zoom");
		mRotate = savedInstanceState.getFloat("Rotate");
		mOverlook = savedInstanceState.getFloat("Overlook");
		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder().overlook(mOverlook)
						.rotate(mRotate).zoom(mZoom)
						.target(new LatLng(mLatitude, mLongitude)).build()));
	}

	public void clickToSearch(View view) {
		final Intent intent = new Intent(LauncherActivity.this,
				CommonSearchActivity.class);
		startActivity(intent);
	}
}
