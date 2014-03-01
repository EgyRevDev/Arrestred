package com.ifraag.location;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.ifraag.arrested.MainActivity;

/**To get the current location, create a location client, connect it to Location Services, and then call its getLastLocation() method.
The return value is the best, most recent location, based on the permissions your app requested and the currently-enabled location sensors.*/
public class MyLocationManager implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String TAG = "MyLocationManager";

    /* Define Cairo Latitude & Longitude */
    private static final double DEFAULT_LATITUDE=30.0380279;
    private static final double DEFAULT_LONGITUDE=31.2405339;

    /*Milliseconds per second*/
    private static final int MILLISECONDS_PER_SECOND = 1000;
    /*Update frequency in seconds*/
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    /*Update frequency in milliseconds*/
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    /*The fastest update frequency, in seconds*/
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    /*A fast frequency ceiling in milliseconds*/
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private Context mContext;

    /* An instance of location client that connects to Google Play Services API */
    private LocationClient mLocationClient;

    /* Define an object that holds accuracy and frequency parameters */
    private LocationRequest mLocationRequest;

    /* Coordinates of current location, they are obtained by GPS/WiFi location sensors in user's phone */
    private Location mLocation;

    private LocationListener locationListener;

    private boolean mUpdatesRequested;

    public MyLocationManager(Context a_context) {
        mContext = a_context;

        /* Create an instance of location client it will connect to Google Play Services API in Activity onStart method. */
        mLocationClient = new LocationClient(mContext,this,this);

        /* Create location request by setting accuracy of Location updates, preferred updated interval and
        * maximum limit of updated interval that app can handle. */
        prepareLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY,
                UPDATE_INTERVAL,
                FASTEST_INTERVAL);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String msg = "Updated Location: " +
                        Double.toString(location.getLatitude()) + "," +
                        Double.toString(location.getLongitude());

                mLocation = location;
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            }
        };

        mUpdatesRequested = false;
    }

    public Location getUserLocation(){
        return mLocation;
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {

        Toast.makeText(mContext, "Location client is Connected to Google Play Services", Toast.LENGTH_SHORT).show();

        /* getLastLocation is good for "best effort" cases where a location is needed immediately or none at all can be used.
        * If you really want to wait for a location it's best to use requestLocationUpdates and wait for the callback.
        *
        * LocationClient (the fused location provider) uses the configured mode in your Settings->Location->Mode.
        * In case of High Accuracy: Fused location provider uses both WiFi & GPS to find your location but GPS takes a while to find
        * your location while wifi is much faster. However, if any one of these 2 services is connected, the call back method
        * onConnected will be called. And if you are trying to call LocationClient.getLastLocation() immediately,
        * it is mostly likely that you will get null value just because GPS is simply not fast enough.
        *
        * In case of  Battery Saving mode: Fused location provider uses WiFi and network to determine your location.
        * Since WiFi is fast as explained earlier, LocationClient.getLastLocation() will not return null */
        mLocation = mLocationClient.getLastLocation();
        if(null == mLocation) {

            Log.w(TAG,"Couldn't obtain your location information");

            /* set default location attributes to Cairo geo-information */
            mLocation = new Location("");
            mLocation.setLatitude(DEFAULT_LATITUDE);
            mLocation.setLongitude(DEFAULT_LONGITUDE);
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        Toast.makeText(mContext, "Location client is DisConnected from Google Play Services", Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services while attempting to connect the location client
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */

        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error.
                // The activity's onActivityResult method will be invoked after the user is done.
                //If the resultCode is RESULT_OK, the application should try to connect again.
                connectionResult.startResolutionForResult(
                        (Activity) mContext,
                        MainActivity.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    public boolean isGooglePlayServicesAvailable() {
        /*Verifies that Google Play services is installed and enabled on this device, and that the version installed on this device
        is no older than the one required by this client.
        result code indicating whether there was an error. Can be one of following in ConnectionResult:
        SUCCESS, SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, SERVICE_INVALID, DATE_INVALID.*/
        int resultCode = GooglePlayServicesUtil.
                isGooglePlayServicesAvailable(mContext);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(TAG, "Google Play services is available.");
            return true;
        } else {// Google Play services was not available for some reason
            // Get the error code
            showErrorDialog(resultCode);
            return false;
        }
    }

    private void showErrorDialog(int errorCode) {

        /* Get the error dialog from Google Play services
         * Returns a dialog to address the provided errorCode. The returned dialog displays a localized message about the error and
         * upon user confirmation (by tapping on dialog) will direct them to the Play Store if Google Play services is out of date or missing,
         * or to system settings if Google Play services is disabled on the device.
         * In other words, Create special error Dialog within activity and accordingly start Google Play activity and wait for its result
         * where the request code is CONNECTION_FAILURE_RESOLUTION_REQUEST.
         */
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                (Activity) mContext,
                MainActivity.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        /*If Google Play services can provide an error dialog*/
        if (errorDialog != null) {

            /*Create a new DialogFragment for the error dialog*/
            ErrorDialogFragment errorFragment =
                    new ErrorDialogFragment();

            /*Set the dialog in the DialogFragment*/
            errorFragment.setDialog(errorDialog);

            /*Show the error dialog in the DialogFragment*/
            errorFragment.show(((ActionBarActivity)mContext).getSupportFragmentManager(),
                    "Location Updates");
        }
    }

    public void prepareLocationRequest(int a_priority, long a_interval, long a_fastestInterval){

        /*Create the LocationRequest object*/
        mLocationRequest = LocationRequest.create();

        /*Use high accuracy*/
        mLocationRequest.setPriority(a_priority);

        /*Set the update interval to 5 seconds*/
        mLocationRequest.setInterval(a_interval);

        /*Set the fastest update interval to 1 second*/
        mLocationRequest.setFastestInterval(a_fastestInterval);
    }

    /* Location Services sends location updates to your app as an argument passed to a callback method you define. */

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    public LocationClient getLocationClient() {
        return mLocationClient;
    }

    public LocationRequest getLocationRequest() {
        return mLocationRequest;
    }

    public LocationListener getLocationListener() {
        return locationListener;
    }

    public boolean isUpdatesRequested() {
        return mUpdatesRequested;
    }

    public void setUpdatesRequested(boolean mUpdatesRequested) {
        this.mUpdatesRequested = mUpdatesRequested;
    }
}
