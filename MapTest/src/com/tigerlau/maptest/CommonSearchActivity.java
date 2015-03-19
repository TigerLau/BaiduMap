package com.tigerlau.maptest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class CommonSearchActivity extends Activity implements OnClickListener {

	private ImageButton backBtn;
	private ImageButton voiceBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common_search);
		initView();
	}

	private void initView() {
		backBtn = (ImageButton) findViewById(R.id.back_btn);
		voiceBtn = (ImageButton) findViewById(R.id.common_speak_btn);
		backBtn.setOnClickListener(this);
		voiceBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			CommonSearchActivity.this.finish();
			break;
		case R.id.common_speak_btn:
			Toast.makeText(CommonSearchActivity.this, "voice",
					Toast.LENGTH_SHORT).show();
			break;

		}
	}
}
