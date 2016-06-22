package com.something.somewhere;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.mobilej.btgps.plugin.IBtgpsPlugin;

/**
 * The plugin service.
 * <p>
 * Created by bjoern on 09.11.14.
 */
public class PluginService extends Service {

    public class BtgpsPluginImpl extends IBtgpsPlugin.Stub {

        private DataInputStream is;

        @Override
        public String beforeParse(String nmeaSentence) throws RemoteException {
            return nmeaSentence;
        }

        @Override
        public RemoteViews getMainScreenWidget() throws RemoteException {
            return null;
        }

        @Override
        public boolean setGpsInfo(int[] prns, float[] snrs, float[] elevations, float[] azimuths,
                                  int ephemerisMask, int almanacMask, int usedInFixMask) throws RemoteException {
            return false;
        }

        @Override
        public boolean setLocation(Location loc) throws RemoteException {
            return false;
        }

        @Override
        public boolean setStatus(int status) throws RemoteException {
            return false;
        }

        @Override
        public boolean setup() throws RemoteException {
            try {
                is = new DataInputStream(new FileInputStream("/sdcard/nmea.log"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean useAlternateSource() throws RemoteException {
            return true;
        }

        @Override
        public boolean noMockLocations() throws RemoteException {
            return false;
        }

        @Override
        public String getNMEA() throws RemoteException {
            String line = null;
            try {
                if (is != null) {
                    line = is.readLine();

                    if (line == null) {
                        is.close();
                        is = new DataInputStream(new FileInputStream("/sdcard/nmea.log"));
                        line = is.readLine();
                    }

                    if (line.contains("RMC,")) {
                        SystemClock.sleep(1000);
                    }
                } else {
                    return "$GPRMC,081836,A,3751.65,S,14507.36,E,000.0,360.0,130998,011.3,E*62";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return line;
        }

        @Override
        public boolean tearDown() throws RemoteException {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
