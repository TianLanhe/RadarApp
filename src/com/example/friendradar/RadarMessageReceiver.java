package com.example.friendradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RadarMessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent it) {
		Intent intent = new Intent(context, RadarResponseService.class);
		intent.putExtras(it.getExtras());
		context.startService(intent);
	}

}
