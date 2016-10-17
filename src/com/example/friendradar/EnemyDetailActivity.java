package com.example.friendradar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class EnemyDetailActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.enemy_detail);
	}
}
