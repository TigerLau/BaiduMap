package com.tigerlau.baidumap.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.tigerlau.baidumap.R;

public class ZoomControlView extends RelativeLayout implements OnClickListener {
	// UIԪ��
	private Button mButtonZoomin;
	private Button mButtonZoomout;
	// �������
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
		mButtonZoomin = (Button) view.findViewById(R.id.zoomin);
		mButtonZoomout = (Button) view.findViewById(R.id.zoomout);
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
	 * ��MapView���ù���
	 * 
	 * @param mapView
	 */
	public void setBaiduMap(BaiduMap baiduMap) {
		this.daiduMap = baiduMap;

		// ��ȡ�������ż���
		maxZoomLevel = daiduMap.getMaxZoomLevel();
		// ��ȡ�������ż���
		minZoomLevel = daiduMap.getMinZoomLevel();
	}

	/**
	 * ����MapView�����ż���������Ű�ť��״̬�����ﵽ������ż�������mButtonZoomin
	 * Ϊ���ܵ������֮����mButtonZoomout
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
