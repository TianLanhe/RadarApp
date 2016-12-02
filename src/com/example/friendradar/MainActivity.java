package com.example.friendradar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.friendradar.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	// UI相关
	private Button friends;
	private Button enemies;
	private Button button_location;
	private Button refresh;

	// 百度地图UI相关
	private MapView mapview;
	private BaiduMap baidumap;

	// 百度地图定位与地址相关
	private BitmapDescriptor bd_friends;
	private BitmapDescriptor bd_enemies;
	private LocationClient locationclient;
	private GeoCoder geocoder;

	// 数据相关
	private List<People> list_friends;
	private List<People> list_enemies;
	private RadarApplication radarapplication;
	private String people_index;// 以成员变量为桥梁，储存下标，用于反地址编码设置地址。
	private double mylat;
	private double mylog;
	private final double scale[] = { 10000001, 10000000, 5000000, 2000000,
			1000000, 500000, 200000, 100000, 50000, 25000, 20000, 10000, 5000,
			2000, 1000, 500, 200, 100, 50, 20 };// 百度地图比例尺，用于动态调整比例尺使所有覆盖物均在可视范围内
	private double max_distance;// 记录一次刷新中距离最远的覆盖物，求它能显示的比例尺，保证所有覆盖物均在可视范围内

	// 询问短信常量与广播常量
	private final int FRIEND = 1;
	private final int ENEMY = 0;
	private final String ASK_LOCATION_MESSAGE = "Where are you?";
	private final String SEND_ACTION = "com.example.friendradar.SEND_ACTION";
	private final String DELIVERY_ACTION = "com.example.frinedradar.DELIVERY_ACTION";
	private final String ALARM_ACTION = "com.example.frinedradar.ALARM_ACTION";

	// 广播接收器
	private RadarLocationMsgReceiver msgreceiver = new RadarLocationMsgReceiver();;
	private SendReceiver sendreceiver = new SendReceiver();
	private DeliveryReceiver deliveryreceiver = new DeliveryReceiver();
	private AlarmReceiver alarmreceiver = new AlarmReceiver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mapview = (MapView) findViewById(R.id.mapView);
		baidumap = mapview.getMap();
		locationclient = new LocationClient(this);
		geocoder = GeoCoder.newInstance();
		bd_friends = BitmapDescriptorFactory
				.fromResource(R.drawable.friend_marker);
		bd_enemies = BitmapDescriptorFactory
				.fromResource(R.drawable.enemy_marker);

		friends = (Button) findViewById(R.id.btn_friends);
		enemies = (Button) findViewById(R.id.btn_enemies);
		button_location = (Button) findViewById(R.id.btn_locate);
		refresh = (Button) findViewById(R.id.btn_refresh);

		radarapplication = (RadarApplication) getApplication();
		list_friends = radarapplication.getFriends();
		list_enemies = radarapplication.getEnemies();

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
		baidumap.animateMapStatus(MapStatusUpdateFactory.zoomTo(18));

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开GPS
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(0); // 时间设置为0，关闭定时定位
		option.setIsNeedAddress(true);// 设置携带地理位置信息
		locationclient.setLocOption(option);

		locationclient.registerLocationListener(new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation location) {
				if (location == null)
					Toast.makeText(MainActivity.this, "Location is null",
							Toast.LENGTH_SHORT).show();
				else {
					LatLng ll = new LatLng(location.getLatitude(), location
							.getLongitude());
					mylat = location.getLatitude();
					mylog = location.getLongitude();

					Log.d("mytag", "Latitude:" + location.getLatitude());
					Log.d("mytag", "Longitude:" + location.getLongitude());
					Log.d("mytag", "Address:" + location.getAddrStr());

					baidumap.animateMapStatus(MapStatusUpdateFactory // 将地图定位到该位置
							.newLatLng(ll));

					// 在地图上显示我的位置，一个点
					MyLocationData.Builder builder = new MyLocationData.Builder();
					builder.accuracy(location.getRadius());
					builder.latitude(location.getLatitude());
					builder.longitude(location.getLongitude());
					MyLocationData data = builder.build();
					baidumap.setMyLocationData(data);
				}
			}
		});
		locationclient.start();

		// 左上角定位自己功能的按钮
		button_location.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				baidumap.clear();// 清除地图所有覆盖物
				locationclient.requestLocation();// 要start后才能请求位置
				baidumap.animateMapStatus(MapStatusUpdateFactory.zoomTo(18));// 较精确地显示自己位置
				max_distance = 0;
				initLocation(list_friends, FRIEND);
				initLocation(list_enemies, ENEMY);
			}
		});

		// 右上角刷新按钮，在雷达上显示朋友和敌人的位置
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (list_friends.size() == 0 && list_enemies.size() == 0) {
					Toast.makeText(MainActivity.this,
							"You have no friends or enemies!",
							Toast.LENGTH_LONG).show();
				} else {
					// 注册短信广播接收器
					registerReceiver(msgreceiver, new IntentFilter(
							"android.provider.Telephony.SMS_RECEIVED"));
					registerReceiver(sendreceiver,
							new IntentFilter(SEND_ACTION));
					registerReceiver(deliveryreceiver, new IntentFilter(
							DELIVERY_ACTION));
					registerReceiver(alarmreceiver, new IntentFilter(
							ALARM_ACTION));

					// max_distance清零，在一次刷新中求最大距离，设置比例尺
					max_distance = 0;
					baidumap.clear();

					// 给每个朋友和敌人发送一条短信
					for (People friend : list_friends)
						sendMessage(friend.getPhoneNum());
					for (People enemy : list_enemies)
						sendMessage(enemy.getPhoneNum());

					Toast.makeText(MainActivity.this, "Start to locate",
							Toast.LENGTH_SHORT).show();
					// 40秒后取消注册短信广播接收器
					AlarmManager alarmmanager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					long starttime = SystemClock.elapsedRealtime() + 1000 * 40;
					Intent intent = new Intent(ALARM_ACTION);
					PendingIntent pendingintent = PendingIntent.getBroadcast(
							MainActivity.this, 0, intent, 0);
					alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
							starttime, pendingintent);
				}
			}
		});

		// 地图上标志物单击事件
		baidumap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				String phonenum = marker.getTitle();
				int i;
				// 根据电话号码查找朋友和敌人列表，并打开相应的细节界面
				for (i = 0; i < list_friends.size(); i++)
					if (list_friends.get(i).getPhoneNum().equals(phonenum))
						break;
				// 若是朋友
				if (i != list_friends.size()) {
					Intent intent = new Intent(MainActivity.this,
							FriendDetailActivity.class);
					intent.putExtra("friend", list_friends.get(i));
					startActivity(intent);
				} else {
					for (i = 0; i < list_enemies.size(); i++)
						if (list_enemies.get(i).getPhoneNum().equals(phonenum))
							break;
					// 若是敌人
					if (i != list_enemies.size()) {
						Intent intent = new Intent(MainActivity.this,
								EnemyDetailActivity.class);
						intent.putExtra("enemy", list_enemies.get(i));
						startActivity(intent);
					} else {
						Toast.makeText(MainActivity.this,
								"onMarkerClick_Error!", Toast.LENGTH_LONG)
								.show();
					}
				}
				return false;
			}
		});

		// 百度地图地理编码监听函数
		geocoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
			@Override
			public void onGetGeoCodeResult(GeoCodeResult arg0) {
				// 正向定位，从地址到经纬度坐标，不需要用到

			}

			@Override
			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
				String index = people_index;
				// index含两个字符，第一个为0表示朋友列表，其他表示敌人列表
				// 第二个为下标
				if (index.charAt(0) == '0')
					list_friends.get(index.charAt(1) - 48).setAddr(
							result.getAddress());
				else
					list_enemies.get(index.charAt(1) - 48).setAddr(
							result.getAddress());
			}
		});
	}

	// 发送一条询问短信，形参传入手机号码
	private void sendMessage(String phonenum) {
		SmsManager smsmanager = SmsManager.getDefault();
		PendingIntent sentIntent = PendingIntent.getBroadcast(this,
				Integer.parseInt(phonenum.substring(7)),
				new Intent(SEND_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent deliveryIntent = PendingIntent.getBroadcast(this, Integer
				.parseInt(phonenum.substring(7)), new Intent(DELIVERY_ACTION),
				PendingIntent.FLAG_UPDATE_CURRENT);
		smsmanager.sendTextMessage(phonenum, null, ASK_LOCATION_MESSAGE,
				sentIntent, deliveryIntent);
	}

	// 将目前好友和敌人有记录的位置显示出来
	private void initLocation(List<People> list_people, int flag) {
		for (People people : list_people) {
			if (people.getLastUpdate() != 0) {
				double latitude = people.getLatitude();
				double longitude = people.getLongitude();

				// 添加雷达地图覆盖物
				LatLng ll = new LatLng(latitude, longitude);
				MarkerOptions makeroption = new MarkerOptions().position(ll)
						.title(people.getPhoneNum());
				if (flag == FRIEND)
					makeroption.icon(bd_friends);
				else if (flag == ENEMY)
					makeroption.icon(bd_enemies);
				else
					return;
				baidumap.addOverlay(makeroption);

				// 添加直线
				LatLng ll_i = new LatLng(mylat, mylog);
				List<LatLng> points = new ArrayList<LatLng>();
				points.add(ll);
				points.add(ll_i);
				PolylineOptions ooPolyline = new PolylineOptions().width(5)
						.points(points);// 宽度为5，颜色是绿色
				if (flag == FRIEND)
					ooPolyline.color(0xAA39b54a);
				else if (flag == ENEMY)
					ooPolyline.color(0xAAec3239);
				else
					return;
				baidumap.addOverlay(ooPolyline);

				// 显示距离
				people.setDistance(DistanceUtil.getDistance(ll_i, ll));
				String distance;
				if (people.getDistance() < 1000)
					distance = (int) (people.getDistance() + 0.5) + "M";
				else
					distance = (int) (people.getDistance() / 1000 + 0.5) + "KM";
				LatLng llText = new LatLng((latitude + mylat) / 2,
						(longitude + mylog) / 2);// 两点的中点
				TextOptions ooText = new TextOptions().fontSize(30)
						.text(distance).position(llText);
				if (flag == FRIEND)
					ooText.fontColor(0xAA39b54a);
				else if (flag == ENEMY)
					ooText.fontColor(0xAAec3239);
				else
					return;
				baidumap.addOverlay(ooText);

				// 显示姓名
				LatLng ll_name = new LatLng(latitude - 0.00005, longitude);
				TextOptions oo_name = new TextOptions().fontSize(30)
						.text(people.getName()).position(ll_name);
				if (flag == FRIEND)
					oo_name.fontColor(0xAA39b54a);
				else if (flag == ENEMY)
					oo_name.fontColor(0xAAec3239);
				else
					return;
				baidumap.addOverlay(oo_name);

				// 更新最大距离，若是新的最大距离，则调整比例尺
				setZoom(people.getDistance());
			}
		}
	}

	// 更新最大距离，若是新的最大距离，则调整比例尺
	private void setZoom(double distance) {
		if (max_distance < distance) {
			max_distance = distance;
			int scale_index = 0;
			while (scale_index < scale.length
					&& max_distance/5 < scale[scale_index])
				scale_index++;
			double zoom = scale_index - 1.3;
			if (scale_index != 0 && scale_index != scale.length) {
				if((max_distance/5 - scale[scale_index])
						/ (scale[scale_index - 1] - scale[scale_index])>0.7)
				zoom -= (max_distance/5 - scale[scale_index])
						/ (scale[scale_index - 1] - scale[scale_index])/2;
			}
			baidumap.animateMapStatus(MapStatusUpdateFactory
					.zoomTo((float) zoom));
			Log.d("MainActivity", "Zoom_to:" + zoom);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationclient.stop();
		baidumap.setMyLocationEnabled(false);
		mapview.onDestroy();
		bd_friends.recycle();
		bd_enemies.recycle();
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

	// 内部类实现广播接收器，用于接收位置短信消息并刷新屏幕雷达信息
	class RadarLocationMsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage[] messages = new SmsMessage[pdus.length];
			for (int i = 0; i < messages.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			}
			String fullmessage = "";
			for (SmsMessage msg : messages)
				fullmessage += msg.getMessageBody();// 读取完整短信
			// 注意短信内容：纬度/经度
			String[] location = fullmessage.split("/"); // 分解出纬度和经度
			if (location.length != 2)
				Toast.makeText(context, "The message received is illegal!",
						Toast.LENGTH_SHORT).show();
			else {
				String phonenum = messages[0].getOriginatingAddress();
				if (phonenum.startsWith("+86"))
					phonenum = phonenum.substring(3);// 手机号码含中国区号"+86"，需去掉
				Double latitude = Double.parseDouble(location[0]);
				Double longitude = Double.parseDouble(location[1]);

				int i;
				for (i = 0; i < list_friends.size(); i++) {
					if (list_friends.get(i).getPhoneNum().equals(phonenum))
						break;
				}

				if (i != list_friends.size()) {// 是朋友
					people_index = "0" + i;
					// 设置该朋友属性
					People friend = list_friends.get(i);
					friend.setLatitude(latitude);
					friend.setLongitude(longitude);
					friend.setUpdateTime(System.currentTimeMillis());

					// 反地址编码取得地址，但是不能立刻在这之后firend.setAddr，因为调用
					// 反地址编码后并不能确保立刻得到地址
					geocoder.reverseGeoCode(new ReverseGeoCodeOption()
							.location(new LatLng(latitude, longitude)));

					friend.setDistance(DistanceUtil.getDistance(new LatLng(
							mylat, mylog), new LatLng(latitude, longitude)));

					Log.d("mytag", "MainActivity_Message:" + fullmessage);
					Log.d("mytag", "MainActivity_Phonenum:" + phonenum);
					Log.d("mytag", "Latitude:" + friend.getLatitude());
					Log.d("mytag", "Longitude:" + friend.getLongitude());
					Log.d("mytag", "Distance:" + friend.getDistance());

					// 添加雷达地图覆盖物
					LatLng ll = new LatLng(latitude, longitude);
					MarkerOptions makeroption = new MarkerOptions()
							.icon(bd_friends).position(ll)
							.title(friend.getPhoneNum());
					baidumap.addOverlay(makeroption);

					// 添加直线
					LatLng ll_friend = new LatLng(latitude, longitude);
					LatLng ll_i = new LatLng(mylat, mylog);
					List<LatLng> points = new ArrayList<LatLng>();
					points.add(ll_friend);
					points.add(ll_i);
					OverlayOptions ooPolyline = new PolylineOptions().width(5)
							.color(0xAA39b54a).points(points);// 宽度为5，颜色是绿色
					baidumap.addOverlay(ooPolyline);

					// 显示距离
					String distance;
					if (friend.getDistance() < 1000) {
						distance = (int) (friend.getDistance() + 0.5) + "M";
					} else {
						distance = (int) (friend.getDistance() / 1000 + 0.5)
								+ "KM";
					}
					LatLng llText = new LatLng((latitude + mylat) / 2,
							(longitude + mylog) / 2);// 两点的中点
					OverlayOptions ooText = new TextOptions().fontSize(30)
							.fontColor(0xFF39b54a).text(distance)
							.position(llText);
					baidumap.addOverlay(ooText);

					// 显示姓名
					LatLng ll_name = new LatLng(latitude - 0.00005, longitude);
					OverlayOptions oo_name = new TextOptions().fontSize(30)
							.fontColor(0xFF39b54a).text(friend.getName())
							.position(ll_name);
					baidumap.addOverlay(oo_name);

					// 更新最大距离，若是新的最大距离，则调整比例尺
					setZoom(friend.getDistance());
				} else {// 不在朋友列表，搜索敌人列表
					for (i = 0; i < list_enemies.size(); i++)
						if (list_enemies.get(i).getPhoneNum().equals(phonenum))
							break;
					if (i != list_enemies.size()) {// 在敌人列表中
						people_index = "1" + i;
						// 设置该敌人属性
						People enemy = list_enemies.get(i);
						enemy.setLatitude(latitude);
						enemy.setLongitude(longitude);
						enemy.setUpdateTime(System.currentTimeMillis());

						// 反地址编码取得地址，但是不能立刻在这之后firend.setAddr，因为调用
						// 反地址编码后并不能确保立刻得到地址
						geocoder.reverseGeoCode(new ReverseGeoCodeOption()
								.location(new LatLng(latitude, longitude)));

						enemy.setDistance(DistanceUtil.getDistance(new LatLng(
								mylat, mylog), new LatLng(latitude, longitude)));

						Log.d("mytag", "MainActivity_Message:" + fullmessage);
						Log.d("mytag", "MainActivity_Phonenum:" + phonenum);
						Log.d("mytag", "Latitude:" + enemy.getLatitude());
						Log.d("mytag", "Longitude:" + enemy.getLongitude());
						Log.d("mytag", "Distance:" + enemy.getDistance());

						// 添加雷达地图覆盖物
						LatLng ll = new LatLng(latitude, longitude);
						MarkerOptions makeroption = new MarkerOptions()
								.icon(bd_enemies).position(ll)
								.title(enemy.getPhoneNum());
						baidumap.addOverlay(makeroption);

						// 添加直线
						LatLng ll_enemy = new LatLng(latitude, longitude);
						LatLng ll_i = new LatLng(mylat, mylog);
						List<LatLng> points = new ArrayList<LatLng>();
						points.add(ll_enemy);
						points.add(ll_i);
						OverlayOptions ooPolyline = new PolylineOptions()
								.width(5).color(0xFFec3239).points(points);// 宽度为5，颜色是红色
						baidumap.addOverlay(ooPolyline);

						// 显示距离
						String distance;
						if (enemy.getDistance() < 1000) {
							distance = (int) (enemy.getDistance() + 0.5) + "M";
						} else {
							distance = (int) (enemy.getDistance() / 1000 + 0.5)
									+ "KM";
						}
						LatLng llText = new LatLng((latitude + mylat) / 2,
								(longitude + mylog) / 2);// 两点的中点
						OverlayOptions ooText = new TextOptions().fontSize(30)
								.fontColor(0xFFec3239).text(distance)
								.position(llText);
						baidumap.addOverlay(ooText);

						// 显示姓名
						LatLng ll_name = new LatLng(latitude - 0.00005,
								longitude);
						OverlayOptions oo_name = new TextOptions().fontSize(30)
								.fontColor(0xFFec3239).text(enemy.getName())
								.position(ll_name);
						baidumap.addOverlay(oo_name);

						// 更新最大距离，若是新的最大距离，则调整比例尺
						setZoom(enemy.getDistance());
					}
				}
			}
		}
	}

	// 用来取消注册短信广播接收器的广播，40秒后自动执行
	class AlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(msgreceiver);
			context.unregisterReceiver(sendreceiver);
			context.unregisterReceiver(deliveryreceiver);
			context.unregisterReceiver(alarmreceiver);
			Toast.makeText(MainActivity.this, "Stop to locate",
					Toast.LENGTH_SHORT).show();
		}
	}

	// 当短信发送成功时调用此广播接收器反馈短信发送情况
	class SendReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(getBaseContext(), "Send Success!",
						Toast.LENGTH_SHORT).show();
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				Toast.makeText(getBaseContext(),
						"Send Failed because generic failure cause.",
						Toast.LENGTH_SHORT).show();
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE:
				Toast.makeText(
						getBaseContext(),
						"Send Failed because service is currently unavailable.",
						Toast.LENGTH_SHORT).show();
				break;
			case SmsManager.RESULT_ERROR_NULL_PDU:
				Toast.makeText(getBaseContext(),
						"Send Failed because no pdu provided.",
						Toast.LENGTH_SHORT).show();
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				Toast.makeText(getBaseContext(),
						"Send Failed because radio was explicitly turned off.",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(getBaseContext(), "Send Failed.",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}

	// 当短信被成功接收时调用此广播接收器反馈短信接收情况
	class DeliveryReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(getBaseContext(), "Delivered Success!",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(getBaseContext(), "Delivered Failed!",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}
}
