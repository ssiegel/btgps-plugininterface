package de.mobilej.xposedlocation;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;

import de.mobilej.btgpsxposedlite.BuildConfig;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedInit implements IXposedHookLoadPackage {


    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("android")) {
            return;
        }

        if (BuildConfig.DEBUG) {
            XposedBridge.log("we are in pkg 'android'!");
        }

        if (VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            XposedBridge.log("Not running on Android 4.2+. Will do nothing.");
            return;
        }

        XposedHelpers.findAndHookMethod("android.location.Location", lpparam.classLoader,
                "setIsFromMockProvider", boolean.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("Location#setIsFromMockProvider");
                        param.args[0] = Boolean.FALSE;
                    }
                });


        XposedHelpers.findAndHookMethod("android.provider.Settings.Secure", lpparam.classLoader,
                "getInt", ContentResolver.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if ("mock_location".equals(param.args[1])) {
                            XposedBridge.log("Settings.Secure#getInt");
                            param.setResult(1);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("android.app.ContextImpl", lpparam.classLoader,
                "checkCallingPermission", String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if ("android.permission.ACCESS_MOCK_LOCATION".equals(param.args[0])) {
                            XposedBridge.log("Context#checkCallingPermission");
                            param.setResult(PackageManager.PERMISSION_GRANTED);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("android.app.AppOpsManager", lpparam.classLoader,
                "noteOp", String.class, int.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (58 == (int) param.args[0]) {
                            XposedBridge.log("AppOpsManager#noteOp");
                            param.setResult(AppOpsManager.MODE_ALLOWED);
                        }
                    }
                });


    }
}