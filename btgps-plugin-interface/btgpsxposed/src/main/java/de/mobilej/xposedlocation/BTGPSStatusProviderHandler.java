package de.mobilej.xposedlocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import de.mobilej.btgpsxposed.BuildConfig;
import de.robv.android.xposed.XposedBridge;

public class BTGPSStatusProviderHandler implements InvocationHandler {

    public static final String ACTION_STATUS_UPDATE = "BTGPS_STATDATE";

    public static final String EXTRA_NMEA = "BTGPS_NMEA";

    public static final String EXTRA_PRNS = "BTGPS_PRNS";

    public static final String EXTRA_SNRS = "BTGPS_SNRS";

    public static final String EXTRA_ELEVATIONS = "BTGPS_ELEVATIONS";

    public static final String EXTRA_AZIMUTHS = "BTGPS_AZIMUTHS";

    public static final String EXTRA_EPHEMERIS_MASK = "BTGPS_EPHEMERIS";

    public static final String EXTRA_ALMANAC_MASK = "BTGPS_ALMANAC";

    public static final String EXTRA_USED_IN_FIX_MASK = "BTGPS_USED_IN_FIX";

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(EXTRA_NMEA)) {
                sendNMEAUpdate(intent.getStringExtra(EXTRA_NMEA));
            } else if (intent.hasExtra(EXTRA_PRNS)) {
                int[] prns = intent.getIntArrayExtra(EXTRA_PRNS);
                float[] snrs = intent.getFloatArrayExtra(EXTRA_SNRS);
                float[] elevations = intent.getFloatArrayExtra(EXTRA_ELEVATIONS);
                float[] azimuths = intent.getFloatArrayExtra(EXTRA_AZIMUTHS);
                int ephemerisMask = intent.getIntExtra(EXTRA_EPHEMERIS_MASK, 0);
                int almanacMask = intent.getIntExtra(EXTRA_ALMANAC_MASK, 0);
                int usedInFixMask = intent.getIntExtra(EXTRA_USED_IN_FIX_MASK, 0);
                sendStatusUpdate(prns, snrs, elevations, azimuths, ephemerisMask, almanacMask,
                        usedInFixMask);
            }
        }

    };

    private Context ctx;

    private ArrayList<Object> listeners = new ArrayList<Object>();

    private Object locationManager;

    private Object original;

    private boolean registered = false;

    public BTGPSStatusProviderHandler(Context ctx, Object locationManager, Object original) {
        this.locationManager = locationManager;
        this.original = original;
        this.ctx = ctx;

        IntentFilter filter = new IntentFilter(ACTION_STATUS_UPDATE);
        ctx.registerReceiver(receiver, filter);
        registered = true;
    }

    @Override
    public Object invoke(Object o, Method m, Object[] args) throws Throwable {
        if (BuildConfig.DEBUG) {
            XposedBridge.log("about to invoke " + m.getName() + " on gps status provider!");
        }

        if (!XposedHelper.isRunning(ctx)) {
            return m.invoke(original, args);
        }

        String methodName = m.getName();

        if ("addGpsStatusListener".equals(methodName)) {
            listeners.add(args[0]);
        } else if ("removeGpsStatusListener".equals(methodName)) {
            listeners.remove(args[0]);
        }

        return null;
    }

    public void sendNMEAUpdate(String nmea) {
        ArrayList<Object> deadListeners = new ArrayList<Object>();
        for (Object listener : listeners) {
            long timestamp = System.currentTimeMillis();

            try {
                sendNMEA(listener, timestamp, nmea);
            } catch (InvocationTargetException e) {
                XposedBridge.log("exception in sendstatusupdate:" + e.getCause());
                deadListeners.add(listener);
            }

        }

        for (Object deadListener : deadListeners) {
            listeners.remove(deadListener);
        }
    }

    /**
     * Methods of IGpsStatusListener
     *
     * void onGpsStarted(); void onGpsStopped(); void onFirstFix(int ttff); void
     * onSvStatusChanged(int svCount, in int[] prns, in float[] snrs, in float[] elevations, in
     * float[] azimuths, int ephemerisMask, int almanacMask, int usedInFixMask); void
     * onNmeaReceived(long timestamp, String nmea);
     */

    public static void sendNmea(Context ctx, String nmea) {
        Intent intent = new Intent(ACTION_STATUS_UPDATE);
        intent.putExtra(EXTRA_NMEA, nmea);
        ctx.sendBroadcast(intent);
    }

    public static void sendStatus(Context ctx, int[] prns, float[] snrs, float[] elevations,
            float[] azimuths, int ephemerisMask, int almanacMask, int usedInFixMask) {
        Intent intent = new Intent(ACTION_STATUS_UPDATE);
        intent.putExtra(EXTRA_PRNS, prns);
        intent.putExtra(EXTRA_SNRS, snrs);
        intent.putExtra(EXTRA_ELEVATIONS, elevations);
        intent.putExtra(EXTRA_AZIMUTHS, azimuths);
        intent.putExtra(EXTRA_EPHEMERIS_MASK, ephemerisMask);
        intent.putExtra(EXTRA_ALMANAC_MASK, almanacMask);
        intent.putExtra(EXTRA_USED_IN_FIX_MASK, usedInFixMask);
        ctx.sendBroadcast(intent);
    }

    public void sendStatusUpdate(int[] prns, float[] snrs, float[] elevations, float[] azimuths,
            int ephemerisMask, int almanacMask, int usedInFixMask) {
        ArrayList<Object> deadListeners = new ArrayList<Object>();
        for (Object listener : listeners) {
            int svCount = prns.length;
            try {
                callOnSvStatusChanged(listener, svCount, prns, snrs, elevations, azimuths,
                        ephemerisMask, almanacMask, usedInFixMask);
            } catch (InvocationTargetException e) {
                XposedBridge.log("exception in sendstatusupdate:" + e.getCause());

                deadListeners.add(listener);
            }


        }

        for (Object deadListener : deadListeners) {
            listeners.remove(deadListener);
        }
    }

    private void callOnSvStatusChanged(Object listener, int svCount, int[] prns, float[] snrs,
            float[] elevations, float[] azimuths, int ephemerisMask, int almanacMask,
            int usedInFixMask) throws InvocationTargetException {
        try {

            Method[] allMethods = listener.getClass().getDeclaredMethods();
            for (Method m : allMethods) {
                XposedBridge
                        .log(">" + m.getName() + " " + m.toString() + " " + m.toGenericString());
            }
            allMethods = listener.getClass().getMethods();
            for (Method m : allMethods) {
                XposedBridge
                        .log("]" + m.getName() + " " + m.toString() + " " + m.toGenericString());
            }

            Method m = listener.getClass()
                    .getDeclaredMethod("onSvStatusChanged", int.class, int[].class, float[].class,
                            float[].class, float[].class, int.class, int.class, int.class);
            m.invoke(listener, svCount, prns, snrs, elevations, azimuths, ephemerisMask,
                    almanacMask, usedInFixMask);

        } catch (NoSuchMethodException e) {
            throw new InvocationTargetException(e);
        } catch (IllegalArgumentException e) {
            throw new InvocationTargetException(e);
        } catch (IllegalAccessException e) {
            throw new InvocationTargetException(e);
        }

    }

    private void sendNMEA(Object listener, long timestamp, String nmea)
            throws InvocationTargetException {
        try {

            Method m = listener.getClass()
                    .getDeclaredMethod("onNmeaReceived", long.class, String.class);
            m.invoke(listener, timestamp, nmea);

        } catch (NoSuchMethodException e) {
            throw new InvocationTargetException(e);
        } catch (IllegalArgumentException e) {
            throw new InvocationTargetException(e);
        } catch (IllegalAccessException e) {
            throw new InvocationTargetException(e);
        }
    }

}
