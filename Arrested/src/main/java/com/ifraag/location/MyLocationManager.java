package com.ifraag.location;

import android.location.Location;

/**
 * Created by wael.showair_2 on 2/19/14.
 */
public class MyLocationManager {

    public Location getUserLocation(){

        /* Coordinates of current location, they are obtained by GPS/WiFi location sensors in user's phone */
        Location myLocation;

        /* TODO: Use Phone sensors and set default location attributes to Cairo where Latitude is 30.0380279 & Longitude is 31.2405339 */
        myLocation  =  new Location("");
        myLocation.setLatitude(30.0380279);
        myLocation.setLongitude(31.2405339);

        return myLocation;
    }
}
