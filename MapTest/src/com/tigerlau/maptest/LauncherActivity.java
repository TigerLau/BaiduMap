package com.tigerlau.maptest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMapTouchListener;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.tigerlau.maptest.ui.ZoomControlView;

public class LauncherActivity extends FragmentActivity {

	// 定位更新标志
	private final static int FLAG_UPDATE_LOCATION = 1;
	// 方向更新标志
	private final static int FALG_UPDATE_DIRECTION = 2;

	// 默认地图中心点为北京故宫
	private static final LatLng DEFAULT_LATLNG = new LatLng(39.914884,
			116.403883);
	private final static int[] mLocateDrawable = {
			R.drawable.main_icon_location, R.drawable.main_icon_follow,
			R.drawable.main_icon_compass };

	private SupportMapFragment smf;
	private BaiduMapOptions bmo;
	private BaiduMap mBaiduMap;
	private ZoomControlView mZoomControl;
	private ImageButton mLocateBtn;

	private LocationClient mLocationClient;
	private MyLocationListener mLocationListener;
	private LocationMode mLocationMode = LocationMode.FOLLOWING;

	private double mLatitude;
	private double mLongitude;
	private float mZoom;
	private float mRotate;
	private float mOverlook;
	private float mAccuracy;
	private boolean isFirstIn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);

		// 初始化或再初始化地图
		if (savedInstanceState != null) {
			mLatitude = savedInstanceState.getDouble("Latitude");
			mLongitude = savedInstanceState.getDouble("Longitude");
			mZoom = savedInstanceState.getFloat("Zoom");
			mRotate = savedInstanceState.getFloat("Rotate");
			mOverlook = savedInstanceState.getFloat("Overlook");
			mLocationMode = (LocationMode) savedInstanceState
					.getSerializable("locationMode");
			bmo = new BaiduMapOptions()
					.compassEnabled(false)
					.zoomControlsEnabled(false)
					.mapStatus(
							new MapStatus.Builder().overlook(mOverlook)
									.target(new LatLng(mLatitude, mLongitude))
									.rotate(mRotate).zoom(mZoom).build());
			smf = SupportMapFragment.newInstance(bmo);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.bmapview_container, smf).commit();
		} else {
			SharedPreferences sp = getSharedPreferences("userinfo",
					MODE_PRIVATE);
			mLatitude = sp
					.getFloat("latitude", (float) DEFAULT_LATLNG.latitude);
			mLongitude = sp.getFloat("longitude",
					(float) DEFAULT_LATLNG.longitude);
			bmo = new BaiduMapOptions()
					.compassEnabled(false)
					.zoomControlsEnabled(false)
					.mapStatus(
							new MapStatus.Builder()
									.target(new LatLng(mLatitude, mLongitude))
									.zoom(17.0f).build());
			smf = SupportMapFragment.newInstance(bmo);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.bmapview_container, smf).commit();
		}
		mZoomControl = (ZoomControlView) findViewById(R.id.zoomcontol);
		mLocateBtn = (ImageButton) findViewById(R.id.locatebtn);
		mLocateBtn.setOnClickListener(onLocateBtnClickListener);
		// 初始化定位服务
		initLocation();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mBaiduMap = smf.getBaiduMap();
		mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {

			@Override
			public void onMapLoaded() {
				if (isFirstIn) {
					mLocateBtn.setVisibility(View.VISIBLE);
					mZoomControl.setVisibility(View.VISIBLE);
					// 首次地图加载完成后，开启定位服务
					mBaiduMap.setMyLocationEnabled(true);
					if (!mLocationClient.isStarted()) {
						mLocationClient.start();
					}
				}
			}
		});
		mZoomControl.setBaiduMap(mBaiduMap);
		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {

			@Override
			public void onMapStatusChangeStart(MapStatus mapStatus) {

			}

			@Override
			public void onMapStatusChangeFinish(MapStatus mapStatus) {

			}

			@Override
			public void onMapStatusChange(MapStatus mapStatus) {
				mZoomControl.refreshZoomButtonStatus(Math.round(mapStatus.zoom));
			}
		});
		mBaiduMap.setOnMapTouchListener(new OnMapTouchListener() {

			@Override
			public void onTouch(MotionEvent event) {
				if ((mLocationMode == LocationMode.FOLLOWING && event
						.getAction() == MotionEvent.ACTION_DOWN)
						|| (mLocationMode == LocationMode.COMPASS && event
								.getAction() == MotionEvent.ACTION_MOVE)) {
					mLocationMode = LocationMode.NORMAL;
					 updateLocationMode(mLocationMode);
				}
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
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
		outState.putSerializable("locationMode", mLocationMode);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// 可选择在OnCreate中实现Re-initialized
		// mLatitude = savedInstanceState.getDouble("Latitude");
		// mLongitude = savedInstanceState.getDouble("Longitude");
		// mZoom = savedInstanceState.getFloat("Zoom");
		// mRotate = savedInstanceState.getFloat("Rotate");
		// mOverlook = savedInstanceState.getFloat("Overlook");
		// mBaiduMap.setMapStatus(MapStatusUpdateFactory
		// .newMapStatus(new MapStatus.Builder().overlook(mOverlook)
		// .rotate(mRotate).zoom(mZoom)
		// .target(new LatLng(mLatitude, mLongitude)).build()));
	}

	private void initLocation() {
		mLocationClient = new LocationClient(LauncherActivity.this);
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener);
		LocationClientOption option = new LocationClientOption();
		// option.setLocationMode(com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy);
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(1000);
		mLocationClient.setLocOption(option);

	}

	/**
	 * 根据当前LocationMode更新UI和定位模式设置
	 * 
	 * @param mLocationMode
	 */
	private void updateLocationMode(LocationMode mLocationMode) {
		if (mLocationMode == LocationMode.COMPASS) {
			mLocateBtn.setImageResource(mLocateDrawable[2]);
		} else if (mLocationMode == LocationMode.FOLLOWING) {
			mLocateBtn.setImageResource(mLocateDrawable[1]);
		} else if (mLocationMode == LocationMode.NORMAL) {
			mLocateBtn.setImageResource(mLocateDrawable[0]);
		}
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				mLocationMode, false, null));
	}

	public void clickToSearch(View view) {
		final Intent intent = new Intent(LauncherActivity.this,
				CommonSearchActivity.class);
		startActivity(intent);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case FLAG_UPDATE_LOCATION:
				MyLocationData data = new MyLocationData.Builder()//
						.accuracy(mAccuracy)//
						.latitude(mLatitude)//
						.longitude(mLongitude)//
						.build();
				mBaiduMap.setMyLocationData(data);
				break;
			case FALG_UPDATE_DIRECTION:

				break;

			default:
				break;
			}
		}
	};
	
	OnClickListener onLocateBtnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (mLocationMode) {
			case NORMAL:
				mLocationMode = LocationMode.FOLLOWING;
				updateLocationMode(mLocationMode);
				break;
			case FOLLOWING:
				mLocationMode = LocationMode.COMPASS;
				updateLocationMode(mLocationMode);
				break;
			case COMPASS:
				mLocationMode = LocationMode.FOLLOWING;
				updateLocationMode(mLocationMode);
				mBaiduMap.animateMapStatus(MapStatusUpdateFactory
						.newMapStatus(new MapStatus.Builder().rotate(0)
								.overlook(0).build()), 500);
				break;
			}
		}
	};

	private class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// 更新定位信息
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			mAccuracy = location.getRadius();

			// 发送位置显示更新请求
			mHandler.sendEmptyMessage(FLAG_UPDATE_LOCATION);

			if (isFirstIn) {
				updateLocationMode(mLocationMode);
				LatLng latLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
				mBaiduMap.animateMapStatus(msu);

				SharedPreferences.Editor editor = getSharedPreferences(
						"userinfo", MODE_PRIVATE).edit();
				editor.putFloat("latitude", (float) mLatitude);
				editor.putFloat("longitude", (float) mLongitude);
				editor.commit();
				isFirstIn = false;
			}

		}

	}
}
