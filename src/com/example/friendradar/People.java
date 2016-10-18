package com.example.friendradar;

import java.io.Serializable;

@SuppressWarnings("serial")
public class People implements Serializable {
	private String name; // ����
	private String phone_num; // �绰����
	private double longitude; // ����
	private double latitude; // γ��
	private long last_update_time; // �����λ����Ϣ��ʱ��
	private double distance; // ����
	private String address;// ��ַ
	private double accuracy;// ����

	People(String name, String num) {
		this.name = name;
		phone_num = num;
		last_update_time = 0;
		longitude = 0;
		latitude = 0;
		distance = 0;
	}

	void setAddr(String address) {
		this.address = address;
	}

	String getAddr() {
		return address;
	}

	void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	double getAccuracy() {
		return accuracy;
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
