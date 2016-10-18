package com.example.friendradar;

import java.io.Serializable;

@SuppressWarnings("serial")
public class People implements Serializable {
	private String name; // 姓名
	private String phone_num; // 电话号码
	private double longitude; // 经度
	private double latitude; // 纬度
	private long last_update_time; // 最后获得位置信息的时间
	private double distance; // 距离

	People(String name, String num) {
		this.name = name;
		phone_num = num;
		last_update_time = 0;
		longitude = 0;
		latitude = 0;
		distance = 0;
	}

	void setName(String name) {
		this.name = name;
	}

	String getName() {
		return name;
	}

	void setPhoneNum(String num) {
		phone_num = num;
	}

	String getPhoneNum() {
		return phone_num;
	}

	void setLongitude(double x) {
		longitude = x;
	}

	double getLongitude() {
		return longitude;
	}

	void setLatitude(double x) {
		latitude = x;
	}

	double getLatitude() {
		return latitude;
	}

	void setDistance(double x) {
		distance = x;
	}

	double getDistance() {
		return distance;
	}

	void setUpdateTime(long time) {
		last_update_time = time;
	}

	long getLastUpdate() {
		return last_update_time;
	}
}
