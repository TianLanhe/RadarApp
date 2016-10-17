package com.example.friendradar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.friendradar.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	// UI相关
	private Button friends;
	private Button enemies;
	private Button button_location;
	private Button refresh;

	// 百度地图相关
	private MapView mapview;
	private BaiduMap baidumap;
	private LocationClient locationclient;

	// 数据相关
	private List<People> list_friends;
	private List<People> list_enemies;
	private RadarApplication radarapplication;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mapview = (MapView) findViewById(R.id.mapView);
		baidumap = mapview.getMap();
		locationclient = new LocationClient(this);

		friends = (Button) findViewById(R.id.btn_friends);
		enemies = (Button) findViewById(R.id.btn_enemies);
		button_location = (Button) findViewById(R.id.btn_locate);
		refresh = (Button) findViewById(R.id.btn_refresh);

		radarapplication = (RadarApplication) getApplication();
		list_friends = radarapplication.getFriends();
		list_enemies = radarapplication.getEnemies();

		// 设置两个按钮长度为屏幕的一半长
		WindowManager windowmanager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int width = windowmanager.getDefaultDisplay().getWidth();
		friends.setWidth(width / 2);
		enemies.setWidth(width / 2);

		// 禁止地图所有手势操作
		baidumap.getUiSettings().setAllGesturesEnabled(false);

		// 左下角朋友按钮，进入朋友列表界面
		friends.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this,
						FriendsListActivity.class);
				startActivity(intent);
			}
		});

		// 右下角敌人按钮，进入敌人列表界面
		enemies.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this,
						EnemiesListActivity.class);
				startActivity(intent);
			}
		});

		// 百度地图开启定位，在中心显示自己的位置
		baidumap.setMyLocationEnabled(true);
		locationclient.registerLocationListener(new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation location) {
				if (location == null)
					Toast.makeText(MainActivity.this, "Location is null",
							Toast.LENGTH_SHORT).show();
				else {
					LatLng ll = new LatLng(location.getLatitude(), location
							.getLongitude());
					baidumap.animateMapStatus(MapStatusUpdateFactory
							.newLatLng(ll));
					MyLocationData.Builder builder = new MyLocationData.Builder();
					builder.accuracy(location.getRadius());
					builder.latitude(location.getLatitude());
					builder.longitude(location.getLongitude());
					MyLocationData data = builder.build();
					baidumap.setMyLocationData(data);
				}
			}

			@Override
			public void onReceivePoi(BDLocation arg0) {
			}

		});
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开GPS
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(0); // 时间设置为0，关闭定时定位
		locationclient.setLocOption(option);
		locationclient.start();

		button_location.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				locationclient.requestLocation();//要start后才能请求位置
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		locationclient.stop();
		baidumap.setMyLocationEnabled(false);
		mapview.onDestroy();
		
		// 保存朋友列表，若列表为空，则删除文件，否则将列表对象写入文件
		if (list_friends.size() != 0) {
			FileOutputStream out;
			ObjectOutputStream objectout;
			try {
				out = openFileOutput("RadarFriendsListDate",
						Context.MODE_PRIVATE);
				objectout = new ObjectOutputStream(out);
				objectout.writeObject(list_friends);
				objectout.close();
				out.close();
			} catch (Exception exp) {
				Toast.makeText(this, "friends saved failed", Toast.LENGTH_LONG)
						.show();
			}
		} else {
			File file = new File("RadarFriendsListDate");
			if (file.exists())
				file.delete();
		}
		// 保存敌人列表，若列表为空，则删除文件，否则将列表对象写入文件
		if (list_enemies.size() != 0) {
			FileOutputStream out;
			ObjectOutputStream objectout;
			try {
				out = openFileOutput("RadarEnemiesListDate",
						Context.MODE_PRIVATE);
				objectout = new ObjectOutputStream(out);
				objectout.writeObject(list_enemies);
				objectout.close();
				out.close();
			} catch (Exception exp) {
				Toast.makeText(this, "enemies saved failed", Toast.LENGTH_LONG)
						.show();
			}
		} else {
			File file = new File("RadarEnemiesListDate");
			if (file.exists())
				file.delete();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapview.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapview.onResume();
	}
}
