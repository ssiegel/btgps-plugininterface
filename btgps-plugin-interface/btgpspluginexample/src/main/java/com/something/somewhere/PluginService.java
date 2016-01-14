package com.something.somewhere;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import com.something.somepackage.R;

import de.mobilej.btgps.plugin.IBtgpsPlugin;

/**
 * The plugin service.
 *
 * Created by bjoern on 09.11.14.
 */
public class PluginService extends Service {

    public class BtgpsPluginImpl extends IBtgpsPlugin.Stub {

        private RemoteViews rv;

        private String text;

        @Override
        public String beforeParse(String nmeaSentence) throws RemoteException {
            Log.d(TAG, "beforeParse " + nmeaSentence);
            return nmeaSentence;
        }

        @Override
        public RemoteViews getMainScreenWidget() throws RemoteException {

            rv = new RemoteViews(getApplicationContext().getPackageName(),
                    R.layout.plugin);
            if (text != null) {
                rv.setTextViewText(R.id.plugin_text, text);
            }
            return rv;
        }

        @Override
        public boolean setGpsInfo(int[] prns, float[] snrs, float[] elevations, float[] azimuths,
                                  int ephemerisMask, int almanacMask, int usedInFixMask) throws RemoteException {
            Log.d(TAG, "setGpsInfo");
            return false;
        }

        @Override
        public boolean setLocation(Location loc) throws RemoteException {
            Log.d(TAG, "setLocation:" + loc);
            text = "" + loc;
            return false;
        }

        @Override
        public boolean setStatus(int status) throws RemoteException {
            Log.d(TAG, "setStatus:" + status);
            return false;
        }

        @Override
        public boolean setup() throws RemoteException {
            Log.d(TAG, "setup");
            return false;
        }

        @Override
        public boolean useAlternateSource() throws RemoteException {
            Log.d(TAG, "useAlternateSource");
            return true;
        }

        @Override
        public boolean noMockLocations() throws RemoteException {
            return false;
        }

        @Override
        public String getNMEA() throws RemoteException {
            Log.d(TAG, "getNMEA");
            SystemClock.sleep(1000);
            return "$GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62";
        }

        @Override
        public boolean tearDown() throws RemoteException {
            Log.d(TAG, "tearDown");
            return false;
        }
    }

    private static final String TAG = "plugin_example";

    private IBinder mBinder = new BtgpsPluginImpl();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
