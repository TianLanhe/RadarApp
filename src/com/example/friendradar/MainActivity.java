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

	// UI���
	private Button friends;
	private Button enemies;
	private Button button_location;
	private Button refresh;

	// �ٶȵ�ͼ���
	private MapView mapview;
	private BaiduMap baidumap;
	private LocationClient locationclient;
	private BitmapDescriptor bd_friends;
	private BitmapDescriptor bd_enemies;

	// �������
	private List<People> list_friends;
	private List<People> list_enemies;
	private RadarApplication radarapplication;

	// ѯ�ʶ��ų���
	private final String ASK_LOCATION_MESSAGE = "Where are you?";
	private final String SEND_ACTION = "com.example.friendradar.SEND_ACTION";
	private final String DELIVERY_ACTION = "com.example.frinedradar.DELIVERY_ACTION";
	private final String ALARM_ACTION = "com.example.frinedradar.ALARM_ACTION";

	// �㲥������
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

		// ��ֹ��ͼ�������Ʋ���
		baidumap.getUiSettings().setAllGesturesEnabled(false);

		// ���½����Ѱ�ť�����������б����
		friends.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this,
						FriendsListActivity.class);
				startActivity(intent);
			}
		});

		// ���½ǵ��˰�ť����������б����
		enemies.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this,
						EnemiesListActivity.class);
				startActivity(intent);
			}
		});

		// �ٶȵ�ͼ������λ����������ʾ�Լ���λ��
		baidumap.setMyLocationEnabled(true);
		baidumap.animateMapStatus(MapStatusUpdateFactory.zoomTo(19));

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // ��GPS
		option.setCoorType("bd09ll"); // ������������
		option.setScanSpan(0); // ʱ������Ϊ0���رն�ʱ��λ
		option.setIsNeedAddress(true);// ����Я������λ����Ϣ
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
					baidumap.animateMapStatus(MapStatusUpdateFactory // ����ͼ��λ����λ��
							.newLatLng(ll));

					// �ڵ�ͼ����ʾ�ҵ�λ�ã�һ����
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

		// ���ϽǶ�λ�Լ����ܵİ�ť
		button_location.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				baidumap.clear();// �����ͼ���и�����
				locationclient.requestLocation();// Ҫstart���������λ��
			}
		});

		// ���Ͻ�ˢ�°�ť�����״�����ʾ���Ѻ͵��˵�λ��
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (list_friends.size() == 0 && list_enemies.size() == 0) {
					Toast.makeText(MainActivity.this,
							"You have no friends or enemies!",
							Toast.LENGTH_LONG).show();
				} else {
					// ע����Ź㲥������
					registerReceiver(msgreceiver, new IntentFilter(
							"android.provider.Telephony.SMS_RECEIVED"));
					registerReceiver(sendreceiver,
							new IntentFilter(SEND_ACTION));
					registerReceiver(deliveryreceiver, new IntentFilter(
							DELIVERY_ACTION));
					registerReceiver(alarmreceiver, new IntentFilter(
							ALARM_ACTION));

					// ��ÿ�����Ѻ͵��˷���һ������
					for (People friend : list_friends)
						sendMessage(friend.getPhoneNum());
					for (People enemy : list_enemies)
						sendMessage(enemy.getPhoneNum());

					// 30���ȡ��ע����Ź㲥������
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

	// ����һ��ѯ�ʶ��ţ��βδ����ֻ�����
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

		// ���������б����б�Ϊ�գ���ɾ���ļ��������б����д���ļ�
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
		// ��������б����б�Ϊ�գ���ɾ���ļ��������б����д���ļ�
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

	// �ڲ���ʵ�ֹ㲥�����������ڽ���λ�ö�����Ϣ��ˢ����Ļ�״���Ϣ
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
				fullmessage += msg.getMessageBody();// ��ȡ��������
			Log.d("mytag", "MainActivity_Message:" + fullmessage);
			// ע��������ݣ�γ��/����
			String[] location = fullmessage.split("/"); // �ֽ��γ�Ⱥ;���
			if (location.length != 2)
				Toast.makeText(context, "The message received is illegal!",
						Toast.LENGTH_SHORT).show();
			else {
				String phonenum = messages[0].getOriginatingAddress();
				if(phonenum.startsWith("+86"))phonenum=phonenum.substring(3);// �ֻ����뺬�й�����"+86"����ȥ��
				Log.d("mytag", "MainActivity_Phonenum:" + phonenum);
				Double latitude = Double.parseDouble(location[0]);
				Double longitude = Double.parseDouble(location[1]);
				int i;
				for (i = 0; i < list_friends.size(); i++) {
					if (list_friends.get(i).getPhoneNum().equals(phonenum))
						break;
				}

				if (i != list_friends.size()) {// ������
					// ���ø���������
					People friend = list_friends.get(i);
					friend.setLatitude(latitude);
					friend.setLongitude(longitude);
					friend.setUpdateTime(System.currentTimeMillis());

					// �����״��ͼ������
					LatLng ll = new LatLng(latitude, longitude);
					MarkerOptions makeroption = new MarkerOptions()
							.icon(bd_friends).position(ll);
					Bundle temp = new Bundle();
					temp.putString("phonenum", phonenum);
					makeroption.extraInfo(temp);
					baidumap.addOverlay(makeroption);
				} else {// ���������б����������б�
					for (i = 0; i < list_enemies.size(); i++)
						if (list_enemies.get(i).getPhoneNum().equals(phonenum))
							break;
					if (i != list_enemies.size()) {// �ڵ����б���
						// ���øõ�������
						People enemy = list_enemies.get(i);
						enemy.setLatitude(latitude);
						enemy.setLongitude(longitude);
						enemy.setUpdateTime(System.currentTimeMillis());

						// �����״��ͼ������
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

	// ����ȡ��ע����Ź㲥�������Ĺ㲥��30����Զ�ִ��
	class AlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(msgreceiver);
			context.unregisterReceiver(sendreceiver);
			context.unregisterReceiver(deliveryreceiver);
			context.unregisterReceiver(alarmreceiver);
		}
	}

	// �����ŷ��ͳɹ�ʱ���ô˹㲥�������������ŷ������
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

	// �����ű��ɹ�����ʱ���ô˹㲥�������������Ž������
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
