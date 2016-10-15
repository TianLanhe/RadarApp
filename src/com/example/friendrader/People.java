package com.example.friendrader;

public class People {
	private String name;
	private String phone_num;
	private double longitude;
	private double latitude;
	private long last_update_time;
	private double distance;

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
