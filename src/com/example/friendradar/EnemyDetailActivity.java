package com.example.friendradar;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class EnemyDetailActivity extends Activity {

	// UI���
	Button button_radar;
	Button button_friend;
	Button button_list;
	ToggleButton edir_or_done;
	Button button_delete;
	TextView people_info[];
	TextView people_name;

	// �������
	People enemy;
	RadarApplication radarapplication;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.enemy_detail);

		radarapplication = (RadarApplication) getApplication();

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
		people_info[6] = (TextView) findViewById(R.id.txt_distance_label);
		people_info[7] = (TextView) findViewById(R.id.txt_distance);
		people_info[8] = (TextView) findViewById(R.id.txt_last_update_time_label);
		people_info[9] = (TextView) findViewById(R.id.txt_last_update_time);

		people_name = (TextView) findViewById(R.id.txt_enemy_name);

		// ����textview�Ĵ�С��Ӧ��ͬ�ֱ��ʵ���Ļ
		// getWidth()��õ�ֵ��ֱ����йأ���������dpi�Ի���ܶ�
		// ������oncreate�л�ȡ������textview�Ĵ�С����Ϊ��ʱ�ؼ���δ���غͻ���
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

		// ���ø���textview������
		enemy = (People) getIntent().getSerializableExtra("enemy");
		people_name.setText(enemy.getName());
		people_info[1].setText(enemy.getPhoneNum());
		if (enemy.getLastUpdate() == 0) {
			for (int i = 3; i < 10; i += 2)
				people_info[i].setText("��");
		} else {
			// ��γ��
			String latitude;
			String longitude;
			if (enemy.getLongitude() > 0)
				longitude = enemy.getLongitude() + "E";
			else
				longitude = -enemy.getLongitude() + "W";
			if (enemy.getLatitude() > 0)
				latitude = enemy.getLatitude() + "N";
			else
				latitude = -enemy.getLatitude() + "S";
			people_info[3].setText(latitude + "/" + longitude);

			// ��ַ
			people_info[5].setText(enemy.getAddr());

			// ����
			String distance;
			if (enemy.getDistance() < 1000) {
				distance = "<" + (int) (enemy.getDistance() + 1) + "M";
			} else {
				distance = "<" + (int) (enemy.getDistance() / 1000 + 1) + "KM";
			}
			people_info[7].setText(distance);

			// ������ʱ��
			long interval = System.currentTimeMillis() - enemy.getLastUpdate();
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

		// �����״�
		button_radar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(EnemyDetailActivity.this,
						MainActivity.class);
				startActivity(intent);
			}
		});

		// ���������б�
		button_friend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(EnemyDetailActivity.this,
						FriendsListActivity.class);
				startActivity(intent);
			}
		});

		// ���ص����б�
		button_list.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(EnemyDetailActivity.this,
						EnemiesListActivity.class);
				startActivity(intent);
				finish();
			}
		});

		// �򿪻�ر�ɾ����ť
		edir_or_done.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1)
					button_delete.setVisibility(View.VISIBLE);
				else
					button_delete.setVisibility(View.GONE);
			}
		});

		// ɾ����ť��ɾ���õ���
		button_delete.setOnClickListener(new OnClickListener() {
			@SuppressLint("InflateParams")
			@Override
			public void onClick(View arg0) {
				View dialogview = null;
				Button ok;
				Button close;
				TextView phonenum;
				final List<People> enemies;

				enemies = radarapplication.getEnemies();
				dialogview = LayoutInflater.from(EnemyDetailActivity.this)
						.inflate(R.layout.dialog_delete_enemy, null);

				ok = (Button) dialogview.findViewById(R.id.btn_dialog_ok);
				close = (Button) dialogview.findViewById(R.id.btn_dialog_close);
				phonenum = (TextView) dialogview.findViewById(R.id.txt_number);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						EnemyDetailActivity.this);
				builder.setView(dialogview);
				final AlertDialog dialog = builder.create();

				phonenum.setText("");

				// �رհ�ť
				close.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}
				});

				// ȷ��ɾ����ť
				ok.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						int i;
						for (i = 0; i < enemies.size(); i++)
							if (enemies.get(i).getPhoneNum()
									.equals(enemy.getPhoneNum()))
								break;
						if (i != enemies.size()) {
							enemies.remove(i);
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
