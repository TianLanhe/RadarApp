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
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
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

	// 百度地图相关
	private MapView mapview;
	private BaiduMap baidumap;
	private LocationClient locationclient;
	private BitmapDescriptor bd_friends;
	private BitmapDescriptor bd_enemies;

	// 数据相关
	private List<People> list_friends;
	private List<People> list_enemies;
	private RadarApplication radarapplication;

	// 询问短信常量
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
		baidumap.animateMapStatus(MapStatusUpdateFactory.zoomTo(19));

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

					Log.d("mytag", "Latitude:" + location.getLatitude());
					Log.d("mytag", "Longitude:" + location.getLongitude());
					Log.d("mytag", "Address:" + location.getAddrStr());

					// baidumap.animateMapStatus(MapStatusUpdateFactory.zoomTo(15));
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

					// 给每个朋友和敌人发送一条短信
					for (People friend : list_friends)
						sendMessage(friend.getPhoneNum());
					for (People enemy : list_enemies)
						sendMessage(enemy.getPhoneNum());

					// 30秒后取消注册短信广播接收器
					AlarmManager alarmmanager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					long starttime = SystemClock.elapsedRealtime() + 1000 * 30;
					Intent intent = new Intent(ALARM_ACTION);
					PendingIntent pendingintent = PendingIntent.getBroadcast(
							MainActivity.this, 0, intent, 0);
					alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
							starttime, pendingintent);
				}
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
			Log.d("mytag", "MainActivity_Message:" + fullmessage);
			// 注意短信内容：纬度/经度
			String[] location = fullmessage.split("/"); // 分解出纬度和经度
			if (location.length != 2)
				Toast.makeText(context, "The message received is illegal!",
						Toast.LENGTH_SHORT).show();
			else {
				String phonenum = messages[0].getOriginatingAddress();
				if(phonenum.startsWith("+86"))phonenum=phonenum.substring(3);// 手机号码含中国区号"+86"，需去掉
				Log.d("mytag", "MainActivity_Phonenum:" + phonenum);
				Double latitude = Double.parseDouble(location[0]);
				Double longitude = Double.parseDouble(location[1]);
				int i;
				for (i = 0; i < list_friends.size(); i++) {
					if (list_friends.get(i).getPhoneNum().equals(phonenum))
						break;
				}

				if (i != list_friends.size()) {// 是朋友
					// 设置该朋友属性
					People friend = list_friends.get(i);
					friend.setLatitude(latitude);
					friend.setLongitude(longitude);
					friend.setUpdateTime(System.currentTimeMillis());

					// 更新雷达地图覆盖物
					LatLng ll = new LatLng(latitude, longitude);
					MarkerOptions makeroption = new MarkerOptions()
							.icon(bd_friends).position(ll);
					Bundle temp = new Bundle();
					temp.putString("phonenum", phonenum);
					makeroption.extraInfo(temp);
					baidumap.addOverlay(makeroption);
				} else {// 不在朋友列表，搜索敌人列表
					for (i = 0; i < list_enemies.size(); i++)
						if (list_enemies.get(i).getPhoneNum().equals(phonenum))
							break;
					if (i != list_enemies.size()) {// 在敌人列表中
						// 设置该敌人属性
						People enemy = list_enemies.get(i);
						enemy.setLatitude(latitude);
						enemy.setLongitude(longitude);
						enemy.setUpdateTime(System.currentTimeMillis());

						// 更新雷达地图覆盖物
						LatLng ll = new LatLng(latitude, longitude);
						MarkerOptions makeroption = new MarkerOptions()
								.icon(bd_enemies).position(ll);
						Bundle temp = new Bundle();
						temp.putString("phonenum", phonenum);
						makeroption.extraInfo(temp);
						baidumap.addOverlay(makeroption);
					}
				}
			}
		}
	}

	// 用来取消注册短信广播接收器的广播，30秒后自动执行
	class AlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(msgreceiver);
			context.unregisterReceiver(sendreceiver);
			context.unregisterReceiver(deliveryreceiver);
			context.unregisterReceiver(alarmreceiver);
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
