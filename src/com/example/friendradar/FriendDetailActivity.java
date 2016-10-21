package com.example.friendradar;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class FriendDetailActivity extends Activity {

	// UI相关
	Button button_radar;
	Button button_enemy;
	Button button_list;
	ToggleButton edir_or_done;
	Button button_delete;
	TextView people_name;
	TextView people_info[];

	// 数据相关
	People friend;
	RadarApplication radarapplication;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.friend_detail);

		radarapplication = (RadarApplication) getApplication();

		button_radar = (Button) findViewById(R.id.btn_radar);
		button_enemy = (Button) findViewById(R.id.btn_enemies);
		button_list = (Button) findViewById(R.id.btn_friends_list);
		edir_or_done = (ToggleButton) findViewById(R.id.btn_friends_list_edit);
		button_delete = (Button) findViewById(R.id.btn_delete);

		people_info = new TextView[10];
		people_info[0] = (TextView) findViewById(R.id.txt_number_label);
		people_info[1] = (TextView) findViewById(R.id.txt_number);
		people_info[2] = (TextView) findViewById(R.id.txt_long_lang_label);
		people_info[3] = (TextView) findViewById(R.id.txt_long_lang);
		people_info[4] = (TextView) findViewById(R.id.txt_address_label);
		people_info[5] = (TextView) findViewById(R.id.txt_address);
		people_info[6] = (TextView) findViewById(R.id.txt_distance_label);
		people_info[7] = (TextView) findViewById(R.id.txt_distance);
		people_info[8] = (TextView) findViewById(R.id.txt_last_update_time_label);
		people_info[9] = (TextView) findViewById(R.id.txt_last_update_time);

		people_name = (TextView) findViewById(R.id.txt_friend_name);

		// 设置textview的大小适应不同分辨率的屏幕
		// getWidth()获得的值与分辨率有关，因此需除以dpi以获得密度
		// 不能再oncreate中获取和设置textview的大小，因为此时控件还未加载和绘制
		ViewTreeObserver vto = people_info[7].getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				for (int i = 0; i < 10; i++)
					people_info[i].setTextSize(10
							* people_info[i].getWidth()
							/ getBaseContext().getResources()
									.getDisplayMetrics().xdpi);
				people_name.setTextSize(10
						* people_info[0].getWidth()
						/ getBaseContext().getResources().getDisplayMetrics().xdpi);
			}
		});

		// 设置各个textview的内容
		friend = (People) getIntent().getSerializableExtra("friend");
		people_name.setText(friend.getName());
		people_info[1].setText(friend.getPhoneNum());
		if (friend.getLastUpdate() == 0) {
			for (int i = 3; i < 10; i += 2)
				people_info[i].setText("无");
		} else {
			// 经纬度
			String latitude;
			String longitude;
			if (friend.getLongitude() > 0)
				longitude = friend.getLongitude() + "E";
			else
				longitude = -friend.getLongitude() + "W";
			if (friend.getLatitude() > 0)
				latitude = friend.getLatitude() + "N";
			else
				latitude = -friend.getLatitude() + "S";
			people_info[3].setText(latitude + "/" + longitude);

			// 地址
			people_info[5].setText(friend.getAddr());

			// 距离
			String distance;
			if (friend.getDistance() < 1000) {
				distance = "<" + (int) (friend.getDistance() + 1) + "M";
			} else {
				distance = "<" + (int) (friend.getDistance() / 1000 + 1) + "M";
			}
			people_info[7].setText(distance);

			// 最后更新时间
			long interval = System.currentTimeMillis() - friend.getLastUpdate();
			long day = interval / 1000 / 60 / 60 / 24;
			String str_time;
			if (day > 0) {
				str_time = day + " day ago";
			} else {
				long hours = (interval % (1000 * 60 * 60 * 24))
						/ (1000 * 60 * 60);
				long minutes = (interval % (1000 * 60 * 60)) / (1000 * 60);
				long seconds = (interval % (1000 * 60)) / 1000;
				str_time = "Before " + hours + ":" + minutes + ":" + seconds;
			}
			people_info[9].setText(str_time);
		}

		// 返回雷达
		button_radar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(FriendDetailActivity.this,
						MainActivity.class);
				startActivity(intent);
			}
		});

		// 进入敌人列表
		button_enemy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(FriendDetailActivity.this,
						EnemiesListActivity.class);
				startActivity(intent);
			}
		});

		// 返回朋友列表
		button_list.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(FriendDetailActivity.this,
						FriendsListActivity.class);
				startActivity(intent);
				finish();
			}
		});

		// 打开或关闭删除按钮
		edir_or_done.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1)
					button_delete.setVisibility(View.VISIBLE);
				else
					button_delete.setVisibility(View.GONE);
			}
		});

		// 删除按钮，删除该朋友
		button_delete.setOnClickListener(new OnClickListener() {
			@SuppressLint("InflateParams")
			@Override
			public void onClick(View arg0) {
				View dialogview = null;
				Button ok;
				Button close;
				TextView phonenum;
				final List<People> friends;

				friends = radarapplication.getFriends();
				dialogview = LayoutInflater.from(FriendDetailActivity.this)
						.inflate(R.layout.dialog_delete_friend, null);

				ok = (Button) dialogview.findViewById(R.id.btn_dialog_ok);
				close = (Button) dialogview.findViewById(R.id.btn_dialog_close);
				phonenum = (TextView) dialogview.findViewById(R.id.txt_number);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						FriendDetailActivity.this);
				builder.setView(dialogview);
				final AlertDialog dialog = builder.create();

				phonenum.setText("");

				// 关闭按钮
				close.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}
				});

				// 确定删除按钮
				ok.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						int i;
						for (i = 0; i < friends.size(); i++)
							if (friends.get(i).getPhoneNum()
									.equals(friend.getPhoneNum()))
								break;
						if (i != friends.size()) {
							friends.remove(i);
							dialog.dismiss();
							finish();
						} else {
							Toast.makeText(getBaseContext(), "Delete Error!",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
				dialog.show();
			}
		});
	}
}
