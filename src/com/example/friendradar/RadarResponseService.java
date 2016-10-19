package com.example.friendradar;

import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class RadarResponseService extends Service {

	private final String ASK_LOCATION_MESSAGE = "Where are you?";

	LocationClient client;
	String phonenum;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		SDKInitializer.initialize(getApplicationContext());
		super.onCreate();
	}

	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle bundle = intent.getExtras();
		Object[] pdus = (Object[]) bundle.get("pdus");
		SmsMessage[] messages = new SmsMessage[pdus.length];
		for (int i = 0; i < messages.length; i++) {
			messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
		}
		String fullmessage = "";
		for (SmsMessage msg : messages)
			fullmessage += msg.getMessageBody();// ��ȡ��������
		
		// ��ȡ�绰���룬�ֻ�������ܺ��й�����"+86"����ȥ��
		phonenum = messages[0].getOriginatingAddress();
		if (phonenum.startsWith("+86"))
			phonenum = phonenum.substring(3);
		Log.d("mytag", "Receiver_Phonenum:" + phonenum);

		// ������͹����Ķ�����"where are you?"
		if (fullmessage.equals(ASK_LOCATION_MESSAGE)) {
			// ��ѯ�Ƿ����Լ�����
			RadarApplication radarapplication = (RadarApplication) getApplicationContext();
			List<People> list_friends = radarapplication.getFriends();
			int i;
			for (i = 0; i < list_friends.size(); i++)
				if (list_friends.get(i).getPhoneNum().equals(phonenum))
					break;

			Notification notification = new Notification(
					R.drawable.radar_launcher, phonenum + " ����һ��λ���������",
					System.currentTimeMillis());
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notification.defaults = Notification.DEFAULT_ALL;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			// �ǵĻ���λ�Լ�λ��
			if (i != list_friends.size()) {
				notification.setLatestEventInfo(getBaseContext(), list_friends
						.get(i).getName() + "(" + phonenum + ")����һ��λ���������",
						"�ѽ���", null);
				manager.notify(1, notification);

				// ʹ�ðٶȵ�ͼAPI��λ�Լ���λ��
				LocationClientOption option = new LocationClientOption();
				option.setOpenGps(true); // ��GPS
				option.setCoorType("bd09ll"); // ������������
				option.setScanSpan(1000); // ʱ������Ϊÿ1�붨λһ�Σ���λ��λ�þ͹ر�

				client = new LocationClient(getBaseContext());
				client.setLocOption(option);
				client.registerLocationListener(new BDLocationListener() {
					@Override
					public void onReceiveLocation(BDLocation location) {
						// ��λ���Լ�λ���ˣ��������겢�ر�client�������߳�
						if (location != null) {
							String latitude = String.valueOf(location
									.getLatitude());
							String longitude = String.valueOf(location
									.getLongitude());
							String message = latitude + "/" + longitude;
							Log.d("mytag", "Receiver_Message:" + message);
							SmsManager smsmanager = SmsManager.getDefault();
							smsmanager.sendTextMessage(phonenum, null, message,
									null, null);

							client.stop();
							stopSelf();
						}
					}
				});
				client.start();
			} else {
				notification.setLatestEventInfo(getBaseContext(), phonenum
						+ " ����һ��λ���������", "�Ѿܾ�", null);
				manager.notify(1, notification);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
}
