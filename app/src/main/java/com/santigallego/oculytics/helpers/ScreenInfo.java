package com.santigallego.oculytics.helpers;

import android.content.res.Configuration;
import android.content.res.Resources;

/*
 * Created by santigallego on 8/19/16.
 */
public class ScreenInfo {

    public ScreenInfo() {}


    // check if phone is in portrait mode
    public static boolean isPortrait(Resources resources) {
        return resources.getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT;
    }

    // check if phone is in landscape mode
    public static boolean isLandscape(Resources resources) {
        return resources.getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
    }
}
