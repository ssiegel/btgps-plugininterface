package de.mobilej.btgpsxposed;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.RemoteViews;

import de.mobilej.btgps.plugin.IBtgpsPlugin;

/**
 * Nothing much done here ... we are exploiting the broadcasts sent by BTGPS to get the actual data
 * to inject into the location server.
 *
 * Created by bjoern on 09.11.14.
 */
public class PluginService extends Service {

    public class BtgpsPluginImpl extends IBtgpsPlugin.Stub {

        private RemoteViews rv;

        private String text;

        @Override
        public String beforeParse(String nmeaSentence) throws RemoteException {
            return nmeaSentence;
        }

        @Override
        public RemoteViews getMainScreenWidget() throws RemoteException {
            // no UI for this plugin
            return null;
        }

        @Override
        public boolean setGpsInfo(int[] prns, float[] snrs, float[] elevations, float[] azimuths,
                int ephemerisMask, int almanacMask, int usedInFixMask) throws RemoteException {
            return true;
        }

        @Override
        public boolean setLocation(Location loc) throws RemoteException {
            return true;
        }

        @Override
        public boolean setStatus(int status) throws RemoteException {
            return true;
        }

        @Override
        public boolean setup() throws RemoteException {
            return true;
        }

        @Override
        public boolean useAlternateSource() throws RemoteException {
            return false;
        }

        @Override
        public boolean noMockLocations() throws RemoteException {
            return true;
        }

        @Override
        public String getNMEA() throws RemoteException {
            return null;
        }

        @Override
        public boolean tearDown() throws RemoteException {
            return true;
        }
    }

    private static final String TAG = "plugin_example";

    private IBinder mBinder = new BtgpsPluginImpl();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
