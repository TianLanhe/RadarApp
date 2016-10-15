package com.example.friendrader;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button friends;
	private Button enemies;
	private MapView mapview;
	private BaiduMap baidumap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mapview = (MapView) findViewById(R.id.mapView);
		baidumap = mapview.getMap();
		friends = (Button) findViewById(R.id.btn_friends);
		enemies = (Button) findViewById(R.id.btn_enemies);

		// 设置两个按钮长度为屏幕的一半长
		WindowManager windowmanager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int width = windowmanager.getDefaultDisplay().getWidth();
		friends.setWidth(width / 2);
		enemies.setWidth(width / 2);

		// 禁止地图所有手势操作
		baidumap.getUiSettings().setAllGesturesEnabled(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapview.onDestroy();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		mapview.onPause();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		mapview.onResume();
	}
}
