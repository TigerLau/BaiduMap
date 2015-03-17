package com.tigerlau.baidumap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
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
import com.tigerlau.baidumap.MyOrientationListener.OnOrientationListener;
import com.tigerlau.baidumap.ui.ZoomControlView;

public class MainActivity extends FragmentActivity {

	// flag定位标志
	private final static int FLAG_UPDATE_LOCATION = 1;
	private final static int FALG_UPDATE_DIRECTION = 2;
	// 角度渐变幅度
	// private final static float DIRECTION_DURATION_SCALE = 1.0F;

	// View元素相关
	private SupportMapFragment smf;
	private final FragmentManager manager = getSupportFragmentManager();

	// Controler相关
	private ZoomControlView mZoomControl;
	private ImageButton mLocateBtn;
	private int[] mLocateDrawable = { R.drawable.main_icon_location,
			R.drawable.main_icon_follow, R.drawable.main_icon_compass };

	// 地图显示相关
	private final BaiduMapOptions bmo = new BaiduMapOptions().compassEnabled(
			false).zoomControlsEnabled(false);
	private BaiduMap mBaiduMap;
	// 默认地图中心点为北京故宫
	private static final LatLng DEFAULT_LATLNG = new LatLng(39.914884,
			116.403883);
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case FLAG_UPDATE_LOCATION:
				MyLocationData data = new MyLocationData.Builder()//
						.direction(mCurrentX)//
						.accuracy(mAccuracy)//
						.latitude(mLatitude)//
						.longitude(mLongitude)//
						.build();
				mBaiduMap.setMyLocationData(data);
				break;
			case FALG_UPDATE_DIRECTION:
				if (mLocationMode == LocationMode.COMPASS) {
					mBaiduMap.setMapStatus(MapStatusUpdateFactory
							.newMapStatus(new MapStatus.Builder().rotate(
									mCurrentX).build()));
				}
				// 渐变操作
				// while (Math.round(Math.abs(mTargetX - mCurrentX)) > 0) {
				// if (Math.sin(Math.toRadians(mTargetX - mCurrentX)) >= 0) {
				// mCurrentX += DIRECTION_DURATION_SCALE;
				// } else {
				// mCurrentX -= DIRECTION_DURATION_SCALE;
				// }
				// mCurrentX %= 360.0;
				// }
				data = new MyLocationData.Builder()//
						.direction(mCurrentX)//
						.accuracy(mAccuracy)//
						.latitude(mLatitude)//
						.longitude(mLongitude)//
						.build();
				mBaiduMap.setMyLocationData(data);
				break;
			default:
				break;
			}
		}
	};

	// 定位相关
	private LocationClient mLocationClient = null;
	private MyLocationListener mLocationListener;
	private boolean isFirstIn = true;
	private float mAccuracy;
	private double mLatitude;
	private double mLongitude;
	private float mZoom;
	private float mOverlook;
	private float mRotate;

	// 方向相关
	private MyOrientationListener myOrientationListener;
	private float mCurrentX;
	// private float mTargetX;
	private LocationMode mLocationMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState != null) {
			smf = (SupportMapFragment) manager
					.findFragmentByTag("map_fragment");
			Log.i("recycle", "NotNullsavedInstanceState");
		}
		// 初始化MapView相关元素
		initMapView();
		// 初始化其它View元素
		initOtherViews();
		// 初始化定位服务
		initLocation();
		// 初始化方向传感服务
		initSensor();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mBaiduMap = smf.getBaiduMap();
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
		// 开启定位服务
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}
		// 开启方向传感器
		myOrientationListener.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		// 停止方向传感器
		myOrientationListener.stop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mLatitude = mBaiduMap.getMapStatus().target.latitude;
		mLongitude = mBaiduMap.getMapStatus().target.longitude;
		mOverlook = mBaiduMap.getMapStatus().overlook;
		mRotate = mBaiduMap.getMapStatus().rotate;
		mZoom = mBaiduMap.getMapStatus().zoom;
		outState.putBoolean("isFirstIn", isFirstIn);
		outState.putDouble("latitude", mLatitude);
		outState.putDouble("longitude", mLongitude);
		outState.putFloat("overlook", mOverlook);
		outState.putFloat("rotate", mRotate);
		outState.putFloat("zoom", mZoom);
		outState.putSerializable("locationMode", mLocationMode);
		Log.i("recycle", "onSaveInstanceState");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		isFirstIn = savedInstanceState.getBoolean("isFirstIn");
		mLatitude = savedInstanceState.getDouble("latitude");
		mLongitude = savedInstanceState.getDouble("longitude");
		mOverlook = savedInstanceState.getFloat("overlook");
		mRotate = savedInstanceState.getFloat("rotate");
		mZoom = savedInstanceState.getFloat("zoom");
		mLocationMode = (LocationMode) savedInstanceState
				.getSerializable("locationMode");
		updateLocationMode(mLocationMode);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder()
						.target(new LatLng(mLatitude, mLongitude))
						.overlook(mOverlook).rotate(mRotate).zoom(mZoom)
						.build()));
		Log.i("recycle", "onRestoreInstanceState");
	}

	private void initMapView() {
		if (smf == null) {
			SharedPreferences sp = getSharedPreferences("userinfo",
					MODE_PRIVATE);
			mLatitude = sp
					.getFloat("latitude", (float) DEFAULT_LATLNG.latitude);
			mLongitude = sp.getFloat("longitude",
					(float) DEFAULT_LATLNG.longitude);
			MapStatus ms = new MapStatus.Builder()
					.target(new LatLng(mLatitude, mLongitude)).zoom(18).build();
			BaiduMapOptions bo = new BaiduMapOptions().mapStatus(ms)
					.compassEnabled(false).zoomControlsEnabled(false);
			smf = SupportMapFragment.newInstance(bo);
			FragmentManager manager = getSupportFragmentManager();
			manager.beginTransaction().add(R.id.bmapview, smf, "map_fragment")
					.commit();
		} else if (smf.isAdded()) {
			smf = SupportMapFragment.newInstance(bmo);
			Log.i("recycle", "smfisreplace");
			manager.beginTransaction()
					.replace(R.id.bmapview, smf, "map_fragment").commit();
		}
	}
	
	private void initOtherViews(){
		mZoomControl = (ZoomControlView) findViewById(R.id.zoomcontrol);
		mLocateBtn = (ImageButton) findViewById(R.id.locatebtn);
		mLocateBtn.setOnClickListener(onLocateBtnClickListener);
		
	}

	private void initLocation() {
		mLocationClient = new LocationClient(MainActivity.this);
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener);
		LocationClientOption option = new LocationClientOption();
		// option.setLocationMode(com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy);
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(1000);
		mLocationClient.setLocOption(option);
		mLocationMode = LocationMode.FOLLOWING;
	}

	private void initSensor() {
		myOrientationListener = new MyOrientationListener(MainActivity.this);
		myOrientationListener
				.setOnOrientationListener(new OnOrientationListener() {

					@Override
					public void OnOrientationChanged(float x) {
						mCurrentX = x;
						// 发送方向显示更新请求
						mHandler.sendEmptyMessage(FALG_UPDATE_DIRECTION);
					}
				});
	}

	public void clickToSearch(View view) {
		final Intent intent = new Intent();
		intent.setClass(MainActivity.this, CommonSearchActivity.class);
		this.startActivity(intent);
	}

	/** 根据当前LocationMode更新UI和定位模式设置
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
				mLocationMode, true, null));
	}

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
				Toast.makeText(MainActivity.this, "FirstIn", Toast.LENGTH_SHORT)
						.show();
				isFirstIn = false;
			}
		}
	}

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

}
