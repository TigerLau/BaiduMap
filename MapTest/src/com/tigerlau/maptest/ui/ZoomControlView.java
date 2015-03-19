package com.tigerlau.maptest.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.tigerlau.maptest.R;

public class ZoomControlView extends RelativeLayout implements OnClickListener {
	// UI元素
	private ImageButton mButtonZoomin;
	private ImageButton mButtonZoomout;
	// 操作相关
	private BaiduMap daiduMap;
	private static MapStatusUpdate msu;
	private float maxZoomLevel;
	private float minZoomLevel;

	public ZoomControlView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ZoomControlView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) {
			return;
		}
		init();
	}

	@SuppressLint("InflateParams")
	private void init() {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.zoom_controls_layout, null);
		mButtonZoomin = (ImageButton) view.findViewById(R.id.zoomin);
		mButtonZoomout = (ImageButton) view.findViewById(R.id.zoomout);
		mButtonZoomin.setOnClickListener(this);
		mButtonZoomout.setOnClickListener(this);
		addView(view);
	}

	@Override
	public void onClick(View v) {
		if (daiduMap == null) {
			throw new NullPointerException(
					"you can call setBaiduMap(BaiduMap baiduMap) at first");
		}
		switch (v.getId()) {
		case R.id.zoomin: {
			msu = MapStatusUpdateFactory.zoomIn();
			daiduMap.animateMapStatus(msu);
			break;
		}
		case R.id.zoomout: {
			msu = MapStatusUpdateFactory.zoomOut();
			daiduMap.animateMapStatus(msu);
			break;
		}
		}
	}

	/**
	 * 与MapView设置关联
	 * 
	 * @param mapView
	 */
	public void setBaiduMap(BaiduMap baiduMap) {
		this.daiduMap = baiduMap;

		// 获取最大的缩放级别
		maxZoomLevel = daiduMap.getMaxZoomLevel();
		// 获取最大的缩放级别
		minZoomLevel = daiduMap.getMinZoomLevel();
	}

	/**
	 * 根据MapView的缩放级别更新缩放按钮的状态，当达到最大缩放级别，设置mButtonZoomin
	 * 为不能点击，反之设置mButtonZoomout
	 * 
	 * @param level
	 */
	public void refreshZoomButtonStatus(float level) {
		if (daiduMap == null) {
			throw new NullPointerException(
					"you can call setBaiduMap(BaiduMap baiduMap) at first");
		}
		if (level > minZoomLevel && level < maxZoomLevel) {
			if (!mButtonZoomout.isEnabled()) {
				mButtonZoomout.setEnabled(true);
			}
			if (!mButtonZoomin.isEnabled()) {
				mButtonZoomin.setEnabled(true);
			}
		} else if (level == minZoomLevel) {
			mButtonZoomout.setEnabled(false);
		} else if (level == maxZoomLevel) {
			mButtonZoomin.setEnabled(false);
		}
	}

}
