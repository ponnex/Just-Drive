package com.ponnex.justdrive;

import android.location.Location;

/**
 * Created by EmmanuelFrancis on 5/24/2015.
 */
public interface GPSCallback {
    public abstract void onGPSUpdate(Location location);
}
