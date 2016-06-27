package de.mobilej.xposedlocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Build.VERSION;

import java.io.BufferedReader;
import java.io.FileReader;

public class XposedHelper extends BroadcastReceiver {

    public static final String ACTION_STICKY_RUNNING = "de.mobilej.btgps.Service";

    public static final String ACTION_STICKY_RUNNING_EXTRA_STATE
            = "RUNNING";

    public static final String INSTALLER_PACKAGE_NAME = "de.robv.android.xposed.installer";

    public static final String BASE_DIR = "/data/data/" + INSTALLER_PACKAGE_NAME + "/";

    private static long lastChecked = -1;

    private static boolean lastRes = false;

    private static boolean registered = false;

    private static boolean runningState = false;

    public static boolean isEnabled(Context ctx) {
        if (VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false; // won't work on Android < 4.2
        }

        long now = System.currentTimeMillis();
        if (now - lastChecked < 5000) {
            return lastRes;
        }
        lastChecked = now;

        String myApk = ctx.getPackageResourcePath();
        boolean res = false;
        try {
            BufferedReader apks = new BufferedReader(
                    new FileReader(BASE_DIR + "conf/modules.list"));
            String apk;
            while ((apk = apks.readLine()) != null) {
                if (apk.equals(myApk)) {
                    res = true;
                    break;
                }
            }
            apks.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        lastRes = res;
        return res;
    }

    public static boolean isRunning(Context ctx) {
        if (!registered && ctx != null) {
            Context appCtx = ctx.getApplicationContext();
            if (appCtx == null) {
                appCtx = ctx;
            }
            IntentFilter filter = new IntentFilter(ACTION_STICKY_RUNNING);
            appCtx.registerReceiver(new XposedHelper(), filter);

            registered = true;
        }

        return runningState;
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent == null) {
            return;
        }

        runningState = intent
                .getBooleanExtra(ACTION_STICKY_RUNNING_EXTRA_STATE, false);
    }
}
