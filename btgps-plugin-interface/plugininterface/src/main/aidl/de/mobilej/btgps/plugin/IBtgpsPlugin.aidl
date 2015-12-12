// IBtgpsPlugin.aidl
package de.mobilej.btgps.plugin;

import android.location.Location;
import android.widget.RemoteViews;

// Declare any non-default types here with import statements

interface IBtgpsPlugin {

    /**
     * Called before the NMEA reading loop.
     * Return true if you don't want to setup mock locations.
     */
    boolean setup();

    /**
    * Don't use bluetooth
    */
    boolean useAlternateSource();

    boolean noMockLocations();

    String getNMEA();

    /**
     *
     */
    String beforeParse(String nmeaSentence);


    /**
     *
     */
    boolean setLocation(in Location loc);

    boolean setGpsInfo(in int[] prns, in float[] snrs, in float[] elevations,
                                   in float[] azimuths, in int ephemerisMask, in int almanacMask, in int usedInFixMask);

    boolean setStatus(in int status);

    boolean tearDown();


    RemoteViews getMainScreenWidget();
}
