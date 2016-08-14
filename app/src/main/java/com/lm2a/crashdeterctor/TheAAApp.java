package com.lm2a.crashdeterctor;

import android.app.Application;


public class TheAAApp extends Application 
{
    public static final boolean DEBUG = false;
	
	public static final String APP_NAME = "AA Ireland";
	
	// Location provider
	public static MyLocationManager sMyLocation;
	
    public String lastLocation;

    public boolean shockeable = false;

	public void onCreate() {
		super.onCreate();

		sMyLocation = new MyLocationManager(this);
	}
	
	
	public void onTerminate() {
		if (sMyLocation != null) {
			sMyLocation.release();
			sMyLocation = null;
		}

	}
	
	
	public static void debugLog(String text) {
		if (DEBUG) {
			System.out.println(text);
		}
	}
}