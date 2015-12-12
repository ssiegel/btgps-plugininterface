package de.mobilej.xposedlocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.mobilej.btgpsxposed.BuildConfig;
import de.robv.android.xposed.XposedBridge;

public class BTGPSProviderHandler implements InvocationHandler {

    public static final String ACTION_LOCATION_UPDATE = "BTGPS_LOCUPDATE";

    public static final String EXTRA_LOCATION = "EXTRA_LOCATION";

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(EXTRA_LOCATION)) {
                Location loc = intent.getParcelableExtra(EXTRA_LOCATION);
                reportNewLocation(loc);
            }
        }

    };

    private Context ctx;

    private boolean enabled;

    private Object locationManager;

    private Object original;

    private boolean registered = false;

    private long updateTime;

    public BTGPSProviderHandler(Context ctx, Object locationManager, Object original) {
        this.locationManager = locationManager;
        this.original = original;
        this.ctx = ctx;

        IntentFilter filter = new IntentFilter(ACTION_LOCATION_UPDATE);
        ctx.registerReceiver(receiver, filter);
        registered = true;
    }

    @Override
    public Object invoke(Object o, Method m, Object[] args) throws Throwable {

        if (BuildConfig.DEBUG) {
            XposedBridge.log("about to invoke " + m.getName() + " on gps provider!");
        }

        if (!XposedHelper.isRunning(ctx)) {
            return m.invoke(original, args);
        }

        if ("isEnabled".equals(m.getName())) {
            return Boolean.TRUE;
        } else if ("getName".equals(m.getName())) {
            return LocationManager.GPS_PROVIDER;
        } else if ("getProperties".equals(m.getName())) {
            Object properties = o.getClass().getClass()
                    .forName("com.android.internal.location.ProviderProperties")
                    .getConstructor(boolean.class, boolean.class, boolean.class, boolean.class,
                            boolean.class, boolean.class, boolean.class, int.class, int.class)
                    .newInstance(false, true, false, false, true, true, true, Criteria.POWER_HIGH,
                            Criteria.ACCURACY_FINE);
            return properties;
        } else if ("enable".equals(m.getName())) {
            enabled = true;
        } else if ("disable".equals(m.getName())) {
            enabled = false;
        } else if ("getStatusUpdateTime".equals(m.getName())) {
            return updateTime;
        } else if ("sendExtraCommand".equals(m.getName())) {
            return Boolean.FALSE;
        } else if ("getStatus".equals(m.getName())) {
            return LocationProvider.AVAILABLE;
        } else if ("setRequest".equals(m.getName())) {
            // nothing for now
        }

        return null;
    }

    public void reportNewLocation(Location l) {
        updateTime = System.currentTimeMillis();
        try {
            Method method;
            method = locationManager.getClass()
                    .getDeclaredMethod("reportLocation", Location.class, boolean.class);
            method.invoke(locationManager, l, false);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void sendLocation(Context ctx, Location loc) {
        Intent intent = new Intent(ACTION_LOCATION_UPDATE);
        intent.putExtra(EXTRA_LOCATION, loc);
        ctx.sendBroadcast(intent);

    }

}
