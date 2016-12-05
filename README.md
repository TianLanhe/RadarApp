### 介绍：
基于短信的定位软件，分敌人和朋友两种类型，可定位全国范围的地理位置

![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/main1.png) ![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/main2.png)  ![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/main3.png) 

### 功能简介：
* 主界面雷达地图显示以自己坐标为中心的位置
* 左上角刷新雷达，显示自己当前的位置，以及所有朋友与敌人在地图的坐标及距离
* 右上方获取朋友与敌人消息，更新朋友与敌人的最新经纬度，并在地图上显示出来
* 点击地图上的覆盖物可查看朋友（敌人）详情信息
* 左下角点击进入朋友列表界面，可以查看已添加的好友。同理，右下角点击进入敌人列表界面，可以查看已添加的敌人
* 点击朋友（敌人）可查看详细信息
* 可添加或删除朋友（敌人）
* 雷达扫描动画功能未实现
* __设计了地图比例尺智能调节算法，雷达的界面是禁止手动操作的，根据覆盖物与自己的距离智能计算比例尺，雷达能自动调节比例尺，使全国无论哪个坐标都能显示在视野范围内__
* __设置短信接收开关，防止恶意用户发送短信风暴，避免安全性问题__
* __设置下拉菜单通知提醒，让用户知道谁曾尝试定位自己的位置__

### 功能详细介绍：
核心功能：
当用户点击主界面右上角按钮尝试更新地理位置信息时，会向所有朋友和敌人发送位置请求短信"Who are you?"，并开启反馈短信接收器，40秒内有效，在40秒内若接收到相应格式的短信，则处理并更新相应的朋友或敌人信息(__[定时短信接收器相关代码](#定时短信接收器相关代码)__。40秒后则无效，此规定防止用户恶意向程序发送位置坐标，强迫程序更新信息，避免短信风暴引起的安全性问题。若在反馈短信接收器打开期间收到短信，则检查短信是否符合经纬度格式("纬度/经度")，若符合则检查短信发送者是否是自己的好友或敌人，是则根据经纬度和短信发送者手机号更新相应好友(敌人)的经纬度坐标、详细地址、距离、最后更新时间等，并在地图上相应经纬度坐标显示覆盖物。这里禁止雷达地图的手势操作，使用比例尺智能调节算法调整地图的可视范围，使任何一个覆盖物均能显示在地图上。(__[比例尺调节算法见下说明](#比例尺调节算法)__)而在被请求位置一端，若短信接收器检测到"Where are you?"的短信，则查询短信发送者是否是自己的朋友，是则将自己的经纬度发送给对方，并在通知栏显示接受消息，否则不发送坐标，并在通知栏显示拒绝消息(是自己的敌人也不能发送自己坐标)

短信示例：<br>
![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/msg_example.png)

拒绝示例：<br>
![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/reject.png)

接受实例：<br>
![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/accept.png)

成功定位实例：<br>
![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/location1.png) ![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/location2.png)

点击左下角进入朋友列表界面<br>
![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/friends_list.png)

点击右下角进入敌人列表界面<br>
![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/enemies_list.png)

添加好友，__限制号码为11位数字且不能是已添加过的电话号码__，添加敌人同理<br>
![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/add_friend.png)

编辑好友，可删除好友<br>
![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/edit_friend.png)

点击朋友可进入详情界面，详细信息包括：姓名、电话、经纬度坐标、详细地址、距离、最后更新时间，若是未进行首次定位的好友或敌人，则显示无<br>
![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/friend_detail1.png) ![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/friend_detail12.png) ![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/friend_detail13.png)

点击详细界面的EDIT，可删除该好友<br>
![](https://github.com/TianLanhe/RadarApp/raw/master/screenshot/detail_edit.png)

###比例尺调节算法
```java
private final double scale[] = { 10000001, 10000000, 5000000, 2000000,
			1000000, 500000, 200000, 100000, 50000, 25000, 20000, 10000, 5000,
			2000, 1000, 500, 200, 100, 50, 20 };// 百度地图比例尺，用于动态调整比例尺使所有覆盖物均在可视范围内
private double max_distance;// 记录一次刷新中距离最远的覆盖物，求它能显示的比例尺，保证所有覆盖物均在可视范围内

if (max_distance < distance) {
	max_distance = distance;
	int scale_index = 0;
	while (scale_index < scale.length && max_distance/5 < scale[scale_index])
		scale_index++;
	double zoom = scale_index - 1.3;	//max_distance/5 和scale_index - 1.3 是摸索出来的
	if (scale_index != 0 && scale_index != scale.length) {
		if((max_distance/5 - scale[scale_index]) / (scale[scale_index - 1] - scale[scale_index])>0.7)
		zoom -= (max_distance/5 - scale[scale_index]) / (scale[scale_index - 1] - scale[scale_index])/2;
	}
	baidumap.animateMapStatus(MapStatusUpdateFactory.zoomTo((float) zoom));
}
```

###定时短信接收器相关代码
```java
// 广播接收器
private RadarLocationMsgReceiver msgreceiver = new RadarLocationMsgReceiver();
// 注册短信广播接收器
registerReceiver(msgreceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
//定时事件接收器
registerReceiver(alarmreceiver, new IntentFilter(ALARM_ACTION));

// max_distance清零，在一次刷新中求最大距离，设置比例尺
max_distance = 0;
baidumap.clear();

// 给每个朋友和敌人发送一条短信
for (People friend : list_friends)
	sendMessage(friend.getPhoneNum());
for (People enemy : list_enemies)
	sendMessage(enemy.getPhoneNum());

// 40秒后取消注册短信广播接收器
AlarmManager alarmmanager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
long starttime = SystemClock.elapsedRealtime() + 1000 * 40;
Intent intent = new Intent(ALARM_ACTION);
PendingIntent pendingintent = PendingIntent.getBroadcast(
		MainActivity.this, 0, intent, 0);
alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		starttime, pendingintent);

// 用来取消注册短信广播接收器的广播，40秒后自动执行
class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		context.unregisterReceiver(msgreceiver);
		context.unregisterReceiver(alarmreceiver);
	}
}
```