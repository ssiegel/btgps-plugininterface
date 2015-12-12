package de.mobilej.xposedlocation;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.os.Build.VERSION;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import de.mobilej.btgpsxposed.BuildConfig;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

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

        findAndHookMethod("com.android.server.LocationManagerService", lpparam.classLoader,
                "loadProvidersLocked", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Context ctx = (Context) getObjectField(param.thisObject, "mContext");

                        Class<?>[] sinterfaces = new Class[]{
                                param.thisObject.getClass().forName("android.location.IGpsStatusProvider")};
                        Object originalGpsStatusProvider = getObjectField(param.thisObject,
                                "mGpsStatusProvider");
                        BTGPSStatusProviderHandler sh = new BTGPSStatusProviderHandler(ctx,
                                param.thisObject, originalGpsStatusProvider);
                        Object sproxy = Proxy
                                .newProxyInstance(param.thisObject.getClass().getClassLoader(), sinterfaces,
                                        sh);
                        setObjectField(param.thisObject, "mGpsStatusProvider", sproxy);

                        HashMap providers = (HashMap) getObjectField(param.thisObject, "mRealProviders");
                        Object originalGpsProvider = providers.get(LocationManager.GPS_PROVIDER);
                        Class<?>[] interfaces = originalGpsProvider.getClass().getInterfaces();
                        InvocationHandler h = new BTGPSProviderHandler(ctx, param.thisObject,
                                originalGpsProvider);
                        Object proxy = Proxy
                                .newProxyInstance(param.thisObject.getClass().getClassLoader(), interfaces,
                                        h);

                        providers.put(LocationManager.GPS_PROVIDER, proxy);

                        callMethod(param.thisObject, "addProviderLocked", proxy);

                    }

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });

    }
}