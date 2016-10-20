package com.example.friendradar;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class EnemyDetailActivity extends Activity {

	//UI相关
	Button button_radar;
	Button button_friend;
	Button button_list;
	ToggleButton edir_or_done;
	Button button_delete;
	TextView people_info[];
	TextView people_name;
	
	//数据相关
	People enemy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.enemy_detail);

		button_radar = (Button) findViewById(R.id.btn_radar);
		button_friend = (Button) findViewById(R.id.btn_friends);
		button_list = (Button) findViewById(R.id.btn_enemies_list);
		edir_or_done = (ToggleButton) findViewById(R.id.btn_enemies_list_edit);
		button_delete = (Button) findViewById(R.id.btn_delete);

		people_info = new TextView[10];
		people_info[0] = (TextView) findViewById(R.id.txt_number_label);
		people_info[1] = (TextView) findViewById(R.id.txt_number);
		people_info[2] = (TextView) findViewById(R.id.txt_long_lang_label);
		people_info[3] = (TextView) findViewById(R.id.txt_long_lang);
		people_info[4] = (TextView) findViewById(R.id.txt_address_label);
		people_info[5] = (TextView) findViewById(R.id.txt_address);
		people_info[6] = (TextView) findViewById(R.id.txt_last_update_time_label);
		people_info[7] = (TextView) findViewById(R.id.txt_last_update_time);
		people_info[8] = (TextView) findViewById(R.id.txt_distance_label);
		people_info[9] = (TextView) findViewById(R.id.txt_distance);

		people_name = (TextView) findViewById(R.id.txt_enemy_name);

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
						/ getBaseContext().getResources()
								.getDisplayMetrics().xdpi);
			}
		});
		
		//设置各个textview的内容
		enemy=(People) getIntent().getSerializableExtra("enemy");
		people_name.setText(enemy.getName());
		people_info[1].setText(enemy.getPhoneNum());
		if(enemy.getLastUpdate()==0){
			for(int i=3;i<10;i+=2)
				people_info[i].setText("无");
		}else{
			String latitude;
			String longitude;
			if(enemy.getLongitude()>0)
				longitude=enemy.getLongitude()+"E";
			else
				longitude=-enemy.getLongitude()+"W";
			if(enemy.getLatitude()>0)
				latitude=enemy.getLatitude()+"N";
			else
				latitude=-enemy.getLatitude()+"S";
			people_info[3].setText(latitude+"/"+longitude);
			
			people_info[5].setText(enemy.getAddr());
			
			String distance;
			if(enemy.getDistance()<1000){
				distance="<"+(int)(enemy.getDistance()+1)+"M";
			}else{
				distance="<"+(int)(enemy.getDistance()/1000+1)+"M";
			}
			people_info[7].setText(distance);
			
			long currenttime=System.currentTimeMillis();
			Date date=new Date(enemy.getLastUpdate()-currenttime);
			SimpleDateFormat dateformat=new SimpleDateFormat("HH:mm:ss");
			String str_time=dateformat.format(date);
			people_info[9].setText(str_time);
		}
		
		// 返回雷达
		button_radar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(EnemyDetailActivity.this,
						MainActivity.class);
				startActivity(intent);
			}
		});

		// 进入朋友列表
		button_friend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(EnemyDetailActivity.this,
						FriendsListActivity.class);
				startActivity(intent);
			}
		});

		// 返回敌人列表
		button_list.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(EnemyDetailActivity.this,
						EnemiesListActivity.class);
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
	}
}
