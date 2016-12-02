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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class FriendsListActivity extends Activity {
	// UI相关
	Button button_radar;
	Button button_enemy;
	Button button_add;
	ToggleButton edit_or_done;
	ListView listview_friends;

	// 数据相关
	List<People> friendslist;
	PeopleListAdapter friendadapter;
	RadarApplication radarapplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.friends_list);

		button_radar = (Button) findViewById(R.id.btn_friends_list_radar);
		button_enemy = (Button) findViewById(R.id.btn_friends_list_enemies);
		button_add = (Button) findViewById(R.id.btn_friends_list_add);
		edit_or_done = (ToggleButton) findViewById(R.id.btn_friends_list_edit);
		listview_friends = (ListView) findViewById(R.id.lvw_friends_list);

		radarapplication = (RadarApplication) getApplication();
		friendslist = radarapplication.getFriends();
		friendadapter = new PeopleListAdapter(this, R.layout.friends_list_item,
				friendslist);

		listview_friends.setAdapter(friendadapter);

		// 左下角按钮，返回主界面
		button_radar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(FriendsListActivity.this,
						MainActivity.class));
			}
		});

		// 右下角按钮，进入敌人列表界面
		button_enemy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(FriendsListActivity.this,
						EnemiesListActivity.class));
			}
		});

		// 添加朋友按钮，弹出添加对话框
		button_add.setOnClickListener(new OnClickListener() {
			@SuppressLint("InflateParams")
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						FriendsListActivity.this);

				View dialogview = LayoutInflater.from(FriendsListActivity.this)
						.inflate(R.layout.dialog_add_friend, null);

				Button close = (Button) dialogview
						.findViewById(R.id.btn_dialog_close);
				Button add = (Button) dialogview
						.findViewById(R.id.btn_dialog_ok);
				final EditText etxt_number = (EditText) dialogview
						.findViewById(R.id.txt_friend_number);
				final EditText etxt_name = (EditText) dialogview
						.findViewById(R.id.txt_friend_name);

				builder.setView(dialogview);

				final AlertDialog dialog = builder.create();

				// 对话框中关闭按钮
				close.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}

				});

				// 对话框中确定按钮，检查名字和电话号码是否合法，合法则添加新朋友
				add.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String name = etxt_name.getText().toString();
						String number = etxt_number.getText().toString();

						if (!name.equals("") && !number.equals("")) {
							// 判断电话号码是否已经在朋友列表添加过
							for (People temp : friendslist) {
								if (temp.getPhoneNum().equals(number)) {
									Toast.makeText(
											getBaseContext(),
											"The phone number you entered has already existed!",
											Toast.LENGTH_LONG).show();
									etxt_number.setText("");
									return;
								}
							}
							// 判断电话号码是否已经在敌人列表添加过
							for (People temp : radarapplication.getEnemies()) {
								if (temp.getPhoneNum().equals(number)) {
									Toast.makeText(
											getBaseContext(),
											"The phone number you entered is your enemy!",
											Toast.LENGTH_LONG).show();
									etxt_number.setText("");
									return;
								}
							}
							// 电话号码格式不对
							if (number.length() != 11
									|| !number.startsWith("1")) {
								Toast.makeText(
										getBaseContext(),
										"Please enter the correct phone number!",
										Toast.LENGTH_SHORT).show();
								etxt_number.setText("");
							} else {
								People friend = new People(name, number);
								friendslist.add(friend);
								friendadapter.notifyDataSetChanged();
								listview_friends.setSelection(friendslist
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
					friendadapter.setEdit(PeopleListAdapter.EDIT);
				} else {
					friendadapter.setEdit(PeopleListAdapter.DONE);
				}
				listview_friends.setClickable(!arg1);
				friendadapter.notifyDataSetChanged();
			}
		});

		// 子项单击事件，打开敌人详细界面
		listview_friends.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Intent intent = new Intent(FriendsListActivity.this,
						FriendDetailActivity.class);
				intent.putExtra("friend", friendslist.get(position));
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		friendadapter.notifyDataSetChanged();
	}
}
