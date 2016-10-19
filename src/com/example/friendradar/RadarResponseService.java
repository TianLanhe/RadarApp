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
			fullmessage += msg.getMessageBody();// 读取完整短信
		phonenum = messages[0].getOriginatingAddress();
		if(phonenum.startsWith("+86"))phonenum=phonenum.substring(3);// 手机号码含中国区号"+86"，需去掉
		Log.d("mytag", "Receiver_Phonenum:" + phonenum);

		if (fullmessage.equals(ASK_LOCATION_MESSAGE)) {// 如果发送过来的短信是"where are you?"
			// 查询是否是自己好友
			RadarApplication radarapplication = (RadarApplication) getApplicationContext();
			List<People> list_friends = radarapplication.getFriends();
			int i;
			for (i = 0; i < list_friends.size(); i++)
				if (list_friends.get(i).getPhoneNum().equals(phonenum))
					break;

			Notification notification = new Notification(
					R.drawable.radar_launcher, phonenum + " 发来一条位置请求短信",
					System.currentTimeMillis());
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notification.defaults = Notification.DEFAULT_ALL;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			// 是的话定位自己位置
			if (i != list_friends.size()) {
				notification.setLatestEventInfo(getBaseContext(), phonenum
						+ "发来一条位置请求短信", "已接受", null);
				manager.notify(1, notification);

				// 使用百度地图API定位自己的位置
				LocationClientOption option = new LocationClientOption();
				option.setOpenGps(true); // 打开GPS
				option.setCoorType("bd09ll"); // 设置坐标类型
				option.setScanSpan(1000); // 时间设置为每1秒定位一次，定位到位置就关闭

				client = new LocationClient(getBaseContext());
				client.setLocOption(option);
				client.registerLocationListener(new BDLocationListener() {
					@Override
					public void onReceiveLocation(BDLocation location) {
						// 定位到自己位置了，发送坐标并关闭client、结束线程
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
						+ "发来一条位置请求短信", "已拒绝", null);
				manager.notify(1, notification);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
