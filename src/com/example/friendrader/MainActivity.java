package com.example.friendrader;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;


public class MainActivity extends Activity {
	
	private Button friends;
	private Button enemies;
	private MapView mapview;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        mapview=(MapView) findViewById(R.id.mapView);
        friends=(Button) findViewById(R.id.btn_friends);
        enemies=(Button) findViewById(R.id.btn_enemies);
        
        WindowManager windowmanager=(WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int width=windowmanager.getDefaultDisplay().getWidth();
        friends.setWidth(width/2);
        enemies.setWidth(width/2);
    }
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		mapview.onDestroy();
	}
}
