package com.example.friendradar;

import java.util.List;

import com.example.friendradar.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PeopleListAdapter extends ArrayAdapter<People> {
	private int resourceID;
	private int editflag;
	public static final int EDIT = 0;
	public static final int DONE = 1;

	public PeopleListAdapter(Context context, int resource, List<People> objects) {
		super(context, resource, objects);
		resourceID = resource;
		editflag = DONE;
	}

	public void setEdit(int flag) {
		if (flag == EDIT || flag == DONE)
			editflag = flag;
		else
			editflag = DONE;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		People people = getItem(position);
		View view;
		ViewHolder viewholder;
		if (convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(resourceID, null);

			// 将每个item的高度设置为listview宽度的1/10，这样比较好看
			AbsListView.LayoutParams param = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					parent.getHeight() / 10);
			view.setLayoutParams(param);

			viewholder = new ViewHolder();
			viewholder.name = (TextView) view.findViewById(R.id.name_cell);
			viewholder.button_delete = (Button) view
					.findViewById(R.id.delete_button_cell);

			// 设置textview的大小适应不同分辨率的屏幕
			// getWidth()获得的值与分辨率有关，因此需除以dpi以获得密度
			viewholder.name.setTextSize(10 * parent.getWidth()
					/ getContext().getResources().getDisplayMetrics().xdpi);

			view.setTag(viewholder);
		} else {
			view = convertView;
			viewholder = (ViewHolder) view.getTag();
		}
		viewholder.name.setText(people.getName());
		if (editflag == DONE)
			viewholder.button_delete.setVisibility(View.GONE);
		else
			viewholder.button_delete.setVisibility(View.VISIBLE);
		viewholder.button_delete.setOnClickListener(new OnClickListener() {
			@SuppressLint("InflateParams")
			@Override
			public void onClick(View arg0) {
				View dialogview = null;
				Button ok;
				Button close;
				TextView phonenum;
				final List<People> peoplelist;

				if (resourceID == R.layout.friends_list_item) {
					peoplelist = ((RadarApplication) getContext()
							.getApplicationContext()).getFriends();
					dialogview = LayoutInflater.from(getContext()).inflate(
							R.layout.dialog_delete_friend, null);
				} else if (resourceID == R.layout.enemies_list_item) {
					peoplelist = ((RadarApplication) getContext()
							.getApplicationContext()).getEnemies();
					dialogview = LayoutInflater.from(getContext()).inflate(
							R.layout.dialog_delete_enemy, null);
				} else {
					Toast.makeText(getContext(), "Error!", Toast.LENGTH_LONG)
							.show();
					peoplelist = null;
				}

				ok = (Button) dialogview.findViewById(R.id.btn_dialog_ok);
				close = (Button) dialogview.findViewById(R.id.btn_dialog_close);
				phonenum = (TextView) dialogview.findViewById(R.id.txt_number);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getContext());
				builder.setView(dialogview);
				final AlertDialog dialog = builder.create();

				phonenum.setText(peoplelist.get(position).getName() + ": "
						+ peoplelist.get(position).getPhoneNum());
				phonenum.setTextSize(10 * ((View) arg0.getParent()).getWidth()
						/ getContext().getResources().getDisplayMetrics().xdpi);

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
						peoplelist.remove(position);
						notifyDataSetChanged();
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
		return view;
	}
}

class ViewHolder {
	TextView name;
	Button button_delete;
}