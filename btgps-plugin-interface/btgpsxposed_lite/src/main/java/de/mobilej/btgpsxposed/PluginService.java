package de.mobilej.btgpsxposed;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.RemoteViews;

import de.mobilej.btgps.plugin.IBtgpsPlugin;

/**
 * Not much done here. Just prevent the mock location settings enforcement.
 * <p/>
 * Created by bjoern on 15.01.2016
 */
public class PluginService extends Service {

    public class BtgpsPluginImpl extends IBtgpsPlugin.Stub {

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
            return false;
        }

        @Override
        public boolean setStatus(int status) throws RemoteException {
            return true;
        }

        @Override
        public boolean setup() throws RemoteException {
            return false;
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

    private IBinder mBinder = new BtgpsPluginImpl();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
