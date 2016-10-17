package com.example.friendradar;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;

public class RadarApplication extends Application {

	private List<People> friends;
	private List<People> enemies;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate() {
		super.onCreate();

		FileInputStream in;
		ObjectInputStream objectin;
		// ��RadarFriendsListDate�ļ�����ȡ���������浽�����б��У���û���ļ������½�һ���յ��б�
		try {
			in = openFileInput("RadarFriendsListDate");
			objectin = new ObjectInputStream(in);
			friends = (List<People>) objectin.readObject();
		} catch (Exception exp) {
			friends = new ArrayList<People>();
		}
		// ��RadarEnemiesListDate�ļ�����ȡ���������浽�����б��У���û���ļ������½�һ���յ��б�
		try {
			in = openFileInput("RadarEnemiesListDate");
			objectin = new ObjectInputStream(in);
			enemies = (List<People>) objectin.readObject();
		} catch (Exception exp) {
			enemies = new ArrayList<People>();
		}
	}

	List<People> getFriends() {
		return friends;
	}

	List<People> getEnemies() {
		return enemies;
	}

	void setFriends(List<People> friendslist) {
		friends = friendslist;
	}

	void setEnemies(List<People> enemieslist) {
		enemies = enemieslist;
	}

	void addFriend(People friend) {
		friends.add(friend);
	}

	void addEnemy(People enemy) {
		enemies.add(enemy);
	}

	void removeFriend(int index) {
		friends.remove(index);
	}

	void removeEnemy(int index) {
		enemies.remove(index);
	}
}
