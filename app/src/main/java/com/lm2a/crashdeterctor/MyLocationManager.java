package com.lm2a.crashdeterctor;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class MyLocationManager implements LocationListener {
	public final static int SECONDS = 1000;
	public final static int MINUTES = 60 * SECONDS;
	private final static boolean DEBUG = TheAAApp.DEBUG;
    public static final int MINIMUM_GPS_ACCURACY_REQUIRED = 100;
    private static final String TAG = "MyLocationManager";

    private LocationManager mLocationManager;
	private String mLocationProvider;
	private Location mLocation;
	private ArrayList<WeakReference<LocationListener>> mListeners;
    private Context mContext;
	public boolean mAwaitingGPSFix;
	protected Location mNetworkLocation;
	
	private LocationListener mNetworkLocationListener = new LocationListener() {
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onLocationChanged(Location location) {
			mNetworkLocation = location;
			mLocationManager.removeUpdates(this);
			if (DEBUG) debugLog("Received a location update from a cell tower: "+location.toString());
			updateLocation(location);

		}
	};
	

	public MyLocationManager(Context context) {
        mContext = context;
		try {
			mListeners = new ArrayList<WeakReference<LocationListener>>();
			mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			mLocationProvider = LocationManager.GPS_PROVIDER;
			mAwaitingGPSFix = true;
		}
		catch (Exception e) {
			debugLog("Location: "+e);
		}
	}
	
	
	// Forward the location to all listeners.
	public void updateLocation(Location newLocation) {
		try {
			if ((mLocation != null) && 
				(newLocation.getAccuracy() > mLocation.getAccuracy() + 10) && // If the accuracy is more than 10m out.
				(newLocation.getTime() < mLocation.getTime() + (5 * MINUTES))) { // And only if within 5 minutes of the more accurate reading.
				// It's less accurate than what we already have.
				debugLog("The latest location update is less accurate that what we recieved a few minutes ago, so ignoring it. (new="+newLocation.getAccuracy()+" old="+mLocation.getAccuracy()+")");
			}
			else {
				mLocation = newLocation;

				if ((mAwaitingGPSFix) && (newLocation.getAccuracy() <= MINIMUM_GPS_ACCURACY_REQUIRED)) {
					mAwaitingGPSFix = false;
				}
                ((TheAAApp) ((Application)mContext).getApplicationContext()).lastLocation=getRightAddress();

				for (Iterator<WeakReference<LocationListener>> i = mListeners.iterator(); i.hasNext();) {
					WeakReference<LocationListener> weakRef = i.next();
					LocationListener listener = weakRef.get();
					if (listener != null) {
						listener.onLocationChanged(newLocation);
					}
				}

			}
		}
		catch (Exception e) {
			debugLog("updateLocation: "+e);
		}
	}
	
	// GPS location update.
	public void onLocationChanged(Location location) {
		if (DEBUG) debugLog("location changed: "+location.toString());
		updateLocation(location);
		mAwaitingGPSFix = false;
	}
	
	// GPS disabled.
	public void onProviderDisabled(String provider) {
		if (DEBUG) debugLog("provider disabled");
		//this.mLocationProvider = null;
		this.mLocation = mNetworkLocation;
		this.mAwaitingGPSFix = true;
		if (LocationManager.NETWORK_PROVIDER.equals(provider) == false) {
			// Try switching to the network provider.
			requestNetworkLocation();
		}
		for (Iterator<WeakReference<LocationListener>> i = mListeners.iterator(); i.hasNext();) {
			WeakReference<LocationListener> weakRef = i.next();
			LocationListener listener = weakRef.get();
			if (listener != null) {
				listener.onProviderDisabled(provider);
			}
		}
	}

	// GPS enabled.
	public void onProviderEnabled(String provider) {
		if (DEBUG) debugLog("provider enabled '"+provider+"'");
		this.mLocationProvider = provider;
		startUpdates();
		for (Iterator<WeakReference<LocationListener>> i = mListeners.iterator(); i.hasNext();) {
			WeakReference<LocationListener> weakRef = i.next();
			LocationListener listener = weakRef.get();
			if (listener != null) {
				listener.onProviderEnabled(provider);
			}
		}
	}

	// GPS status change.
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	
	public Location getLocation() {
		if (mLocation != null) {
			// If this location is more than 5 minutes old then ignore it.
			long timeDifference = (new Date()).getTime() - mLocation.getTime();
			if ((!TheAAApp.DEBUG) && (timeDifference > 5 * MINUTES) && (mLocation.getTime() > 0)) {
				debugLog("Location is out-of-date. Ignoring it.");
				mLocation = null;
			}
		}
		if (mLocation == null) {
			mAwaitingGPSFix = true;
			// Default to the network location if it's less than 10 minutes old (which is unlikely if the above failed but try anyway).
			if (mNetworkLocation != null) {
				long timeDifference = (new Date()).getTime() - mNetworkLocation.getTime();
				if (timeDifference < 10 * MINUTES) {
					mLocation = mNetworkLocation;
				}
			}
			// obtain the location from the network.
			requestNetworkLocation();
		}
		return mLocation;
	}
	
	
	public Location getLastKnownLocation() {
		Location result = mLocation;
		if (result == null) {
			result = mNetworkLocation;
		}
		if (result == null) {
			try {
				result = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			catch (Exception e) {}
		}
		if (result == null) {
			try {
				result = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
			catch (Exception e) {}
		}
		return result;
	}
	
	
	public void startUpdates() {
		if ((mLocationManager != null) && 
			(mLocationProvider != null)) {
			try {
				mLocationManager.requestLocationUpdates(mLocationProvider, 2*MINUTES, 3, this);
				if (DEBUG) debugLog("started GPS listening");
			}
			catch (Exception e) {
				debugLog("startUpdates(): "+e);
			}
		}
	}
	
	
	// Save on battery, disable GPS updates when paused.
	public void stopUpdates() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(this);
			if (DEBUG) debugLog("stopped GPS listening");
		}
	}
	
	
	// Ensure location updates are stopped.
	public void reset() {
		stopUpdates();
		mLocation = null;
		mListeners.clear();
		mAwaitingGPSFix = true;
	}
	
	
	public void release() {
		if (DEBUG) debugLog("release()");
		stopUpdates();
		mLocationManager = null;
		mLocationProvider = null;
		mLocation = null;
		mListeners.clear();
		mAwaitingGPSFix = true;
	}
	
	
	// Returns true if GPS is enabled
	public boolean isGpsEnabled() {
		boolean result = false;
		try {
			result = ((mLocationManager != null) && (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
		}
		catch (Exception e) {
			debugLog("isGpsEnabled: "+e);
			result = false;
		}
		return result;
	}
	
	

    public void addListener(final LocationListener listener) {
    	synchronized(mListeners) {
    		try {
    			removeAnyNullListeners();
    			if (!mListeners.isEmpty()) {
    				for (WeakReference<LocationListener> weakRef : mListeners) {
    					if (listener.equals(weakRef.get())) {
    						return; // Already exists
    					}
    				}
    			}
    			else {
    				startUpdates();
    			}
    			mListeners.add(new WeakReference<LocationListener>(listener));
    		}
    		catch (Exception e) {
    			debugLog("addListener: "+e);
    		}
    	}
    }


    public void removeListener(final LocationListener listener) {
    	synchronized(mListeners) {
    		try {
    			removeAnyNullListeners();
    			for (int i = mListeners.size()-1; i >= 0; i--) {
    				if (listener.equals(mListeners.get(i).get())) {
    					mListeners.remove(i);
    				}
    			}
    			if (mListeners.isEmpty()) {
    				stopUpdates();
    			}
    		}
    		catch (Exception e) {
    			debugLog("removeListener: "+e);
    		}
    	}
    }

    
    private void removeAnyNullListeners() {
    	try {
    		for (int i = mListeners.size()-1; i >= 0; i--) {
    			LocationListener listener = mListeners.get(i).get();
    			if (listener == null) {
    				mListeners.remove(i);
    				if (TheAAApp.DEBUG) {
    					debugLog("Removed a null location listener");
    				}
    			}
    		}
    	}
    	catch (Exception e) {
    		debugLog("removeAnyNullListeners: "+e);
    	}
    }
    
	
	// obtain the location from the network.
	private void requestNetworkLocation() {
		try {
			if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				if (DEBUG) debugLog("Establishing location from network cell-tower");
				mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mNetworkLocationListener);
			}
		}
		catch (Exception e) {
			debugLog("requestNetworkLocation: "+e);
		}
	}
	
	private void debugLog(String text) {
		if (TheAAApp.DEBUG) {
			TheAAApp.debugLog("Location: "+text);
		}
	}

    String address, city, country = null;

    private String getRightAddress(Location loc) {
        String longitude = "Longitude: " + loc.getLongitude();
        Log.v(TAG, longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.v(TAG, latitude);


        Geocoder gcd = new Geocoder(mContext,
                Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(), loc
                    .getLongitude(), 1);
            if (addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getAddressLine(1);
                country = addresses.get(0).getAddressLine(2);
                //Toast.makeText(mContext, "You are in: " + address + " / " + city + " / " + country, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address + " / " + city + " / " + country;
    }

    private String getRightAddress() {

        Geocoder gcd = new Geocoder(mContext,
                Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
            if (addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getAddressLine(1);
                country = addresses.get(0).getAddressLine(2);
                //Toast.makeText(mContext, "You are in: " + address + " / " + city + " / " + country, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address + " / " + city + " / " + country;
    }


    }
