package com.example.friendradar;

import java.util.List;

import com.example.friendradar.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class EnemiesListActivity extends Activity {
	// UI相关
	ListView listview_enemies;
	Button button_radar;
	Button button_friend;
	Button button_add;
	ToggleButton edit_or_done;

	// 数据相关
	List<People> enemieslist;
	RadarApplication radarapplication;
	PeopleListAdapter enemyadapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.enemies_list);

		listview_enemies = (ListView) findViewById(R.id.lvw_enemies_list);
		button_radar = (Button) findViewById(R.id.btn_enemies_list_radar);
		button_friend = (Button) findViewById(R.id.btn_enemies_list_friends);
		button_add = (Button) findViewById(R.id.btn_enemies_list_add);
		edit_or_done = (ToggleButton) findViewById(R.id.btn_enemies_list_edit);

		radarapplication = (RadarApplication) getApplication();
		enemieslist = radarapplication.getEnemies();
		enemyadapter = new PeopleListAdapter(this, R.layout.enemies_list_item,
				enemieslist);

		listview_enemies.setAdapter(enemyadapter);

		// 左上角按钮，返回主界面
		button_radar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(EnemiesListActivity.this,
						MainActivity.class));
			}
		});

		// 右下角按钮，进入朋友列表界面
		button_friend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(EnemiesListActivity.this,
						FriendsListActivity.class));
			}
		});

		// 添加敌人按钮，弹出添加对话框
		button_add.setOnClickListener(new OnClickListener() {
			@SuppressLint("InflateParams")
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						EnemiesListActivity.this);
				View dialogview = LayoutInflater.from(EnemiesListActivity.this)
						.inflate(R.layout.dialog_add_enemy, null);
				Button close = (Button) dialogview
						.findViewById(R.id.btn_dialog_close);
				Button add = (Button) dialogview
						.findViewById(R.id.btn_dialog_ok);
				final EditText etxt_number = (EditText) dialogview
						.findViewById(R.id.txt_enemy_number);
				final EditText etxt_name = (EditText) dialogview
						.findViewById(R.id.txt_enemy_name);
				builder.setView(dialogview);
				final AlertDialog dialog = builder.create();
				close.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}

				});
				add.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String name = etxt_name.getText().toString();
						String number = etxt_number.getText().toString();
						if (!name.equals("") && !number.equals("")) {
							// 判断电话号码是否已经在敌人列表添加过
							for (People temp : enemieslist) {
								if (temp.getPhoneNum().equals(number)) {
									Toast.makeText(
											getBaseContext(),
											"The phone number you entered has already existed!",
											Toast.LENGTH_LONG).show();
									etxt_number.setText("");
									return;
								}
							}
							// 判断电话号码是否已经在朋友列表添加过
							for (People temp : radarapplication.getFriends()) {
								if (temp.getPhoneNum().equals(number)) {
									Toast.makeText(
											getBaseContext(),
											"The phone number you entered is your friend!",
											Toast.LENGTH_LONG).show();
									etxt_number.setText("");
									return;
								}
							}
							if (number.length() != 11
									|| !number.startsWith("1")) {
								Toast.makeText(
										getBaseContext(),
										"Please enter the correct phone number!",
										Toast.LENGTH_SHORT).show();
								etxt_number.setText("");
							} else {
								People friend = new People(name, number);
								enemieslist.add(friend);
								enemyadapter.notifyDataSetChanged();
								listview_enemies.setSelection(enemieslist
										.size());
								dialog.dismiss();
							}
						} else {
							Toast.makeText(getBaseContext(),
									"Name or number can't be null!",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
				dialog.show();
			}
		});

		// 编辑按钮，设置是否可以删除
		edit_or_done.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					enemyadapter.setEdit(PeopleListAdapter.EDIT);
				} else {
					enemyadapter.setEdit(PeopleListAdapter.DONE);
				}
				listview_enemies.setClickable(!arg1);
				enemyadapter.notifyDataSetChanged();
			}
		});

		// 子项单击事件，打开敌人详细界面
		listview_enemies.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Intent intent = new Intent(EnemiesListActivity.this,
						EnemyDetailActivity.class);
				intent.putExtra("enemy", enemieslist.get(position));
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		enemyadapter.notifyDataSetChanged();
	}
}
