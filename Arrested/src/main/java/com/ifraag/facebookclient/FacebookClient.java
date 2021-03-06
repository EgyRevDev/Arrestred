/**
 * © 2014 Ifraag Campaign. All rights reserved. This code is only licensed and owned by Ifraag Campaign.
 * Please keep this copyright information if you are going to use this code.
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.ifraag.facebookclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.ifraag.arrested.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class FacebookClient {

    /* Current Facebook Android SDK version is 3.6 and this is updated version till today. */

    /* String that holds a facebook permission to post on wall */
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions", "user_location", "user_checkins", "user_status");

    /* String to save pendingPublishReauthorization flag in bundle */
    public static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";

    public static final String DEFAULT_USER_NAME = "Username";

    /* Key string for name of facebook user. It will be used during screen rotation or app restarting*/
    public static final String KEY_USER_NAME = "fb_username";

    /* To get publish permission, you have to open session for read first then re-authorize your application to ask for
    * publish permission. This flag indicates whether we are in the middle of pending publish permission re-authorization or not. */
    private boolean pendingPublishReauthorization;

    /* String to hold the page id of current place so that the reported location is precise as much as possible.
    * In Graph API, a user place is represented by GraphPlace, it consists of geographical information (Latitude & Longitude) and
    * page id of that place, so you need to get this page id.
    * TODO: Think about error handling if there is no page id for user's place. Perhaps you will just use Google maps. */
    private String placePageID;

    /* An interface instance that implements user's layout changes according to facebook session changes. */
    FacebookView facebookView;

    /* An instance for current active facebook session. */
    private Session session;

    /* An interface instance that implements facebook callback function once session state changes. */
    private Session.StatusCallback statusCallback;

    /* Current context from which facebook session is running, it can be Activity or Fragment. */
    private Context currentContext;

    /* An instance of location object that represents current geographical information (Latitude & Longitude) of the user. */
    private Location myLocation;

    /* An instance holds name of facebook user.*/
    private String name;

    /* An instance holds profile picture of facebook user. */
    private Drawable userProfilePicture;

    /* Constructor of FacebookClient.*/
    public FacebookClient(Context a_context, FacebookView a_FacebookView ) {

        /* Initial value must be false since we are not in the middle of pending publish permission. */
        pendingPublishReauthorization = false;

        /* Since page id of the user's place is unknown, set it to null. */
        placePageID = null;

        facebookView = a_FacebookView;

        /* Create an instance of interface that implements the callback function that will be called after changing session state. */
        statusCallback = new SessionStatusCallback();

        /* 1- Every Activity is a Context, but not every Context is an Activity
           2- Sometimes we pass context of an activity as a parameter to some other function and later we might want the activity too.
        So instead of passing the activity also to that function, we can get the activity from context itself.

        According to previous two points, I can cast context parameter to an activity since I am sure that it is an activity in
        my case */

        currentContext = a_context;

        /* Set log level of facebook SDK, any log level beyond this should be enabled in production phase. */
        /* TODO: You may get use of Settings.getSdkVersion for future use. */
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        /* Set default username and profile picture for any facebook client. */
        name = DEFAULT_USER_NAME;
        userProfilePicture  = a_context.getResources().getDrawable(R.drawable.com_facebook_profile_default_icon);
    }

    /* Logger tag that is used within current class. */
    private static final String FB_CLIENT_TAG = "MyFBClient";

    public void activateSession() {

        session = Session.getActiveSession();
        if (session == null) {
            Log.i(FB_CLIENT_TAG, "No active Session");
            openSession();
        }else { // case session instance variable is not null
            /* Make sure that current session state is opened. */
            if (session.getState().equals(SessionState.CLOSED)
                    || session.getState().equals(SessionState.CLOSED_LOGIN_FAILED)){
                /*Sessions can only be opened once. When a session is closed, it cannot be re-opened.
                Instead, a new session should be created. Typical apps will only require one active session at any time.
                The Facebook SDK provides static active session methods that take care of opening new session instances.*/
                openSession();
            }
        }

        /* FacebookView is null when you don't have any updates in layout.*/
        if(null != facebookView) {
            /* Update your application layout views.*/
            facebookView.updateLayoutViews();
        }
    }

    private void openSession(){

        /* Create a new session */
        session = new Session(currentContext);

        /* Set new session to be the active session. */
        Session.setActiveSession(session);
        Log.i(FB_CLIENT_TAG, "Active Session is set");

        /*Sessions can only be opened once. When a session is closed, it cannot be re-opened.
        Instead, a new session should be created. Typical apps will only require one active session at any time.
        The Facebook SDK provides static active session methods that take care of opening new session instances.*/
        Log.i(FB_CLIENT_TAG, "Open a session for read");

        /* Open session for read.*/
        session.openForRead(new Session.OpenRequest((Activity) currentContext).setCallback(statusCallback));
    }

    public void getUserProfileInformation() {

        /* This must be added in case login is required after logout because when session is closed, it cannot be re-opened.*/
        activateSession();

        /*Make an API call to get user data and define a new callback to handle the response. */
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {

                /*If the response is successful and user information is not null, get the user name and profile picture then update your Settings Views.*/
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        /* Get user name*/
                        name = user.getName();
                        Log.i(FB_CLIENT_TAG, " Current name is " + name);
                        Toast.makeText(currentContext, name +" has logged to Facebook", Toast.LENGTH_SHORT).show();
                        //Log.i(FB_CLIENT_TAG, " Current Username is " + user.getUsername());

                        /* Get user profile picture. */
                        new ProfilePic().execute(user.getId());

                        /* Update your views with the obtained user's name and profile picture. */
                        facebookView.updateLayoutViews();
                    }
                }
                if (response.getError() != null) {
                    /*TODO: Handle two different errors; first one is for Connection Error while second one is session closure.
                     * Please check what is returned from response.getError()*/
                    Log.i(FB_CLIENT_TAG, " an error occurred while getting Facebook API response");
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(currentContext)
                            .setTitle("Connection Error")
                            .setMessage("Turn on WiFi or Data Connection");

                    alertDialog.show();
                }
            } /*onCompleted*/
        }/* anonymous class */);

        /* Execute your requests asynchronously (with request.executeAsync()) to avoid running your requests in the UI Thread.
        This improves performance and ensures compatibility with Android 3.0+ */
        request.executeAsync();
    }

    public void logout() {
        /* close and clear all token information.*/
        /*session.closeAndClearTokenInformation();*/
        session.close();

        /* Reset user name and profile picture. */
        name = DEFAULT_USER_NAME;
        userProfilePicture = currentContext.getResources().getDrawable(R.drawable.com_facebook_profile_default_icon);

        /* You needn't invoke this method explicitly because it will be called once facebook session status is changes.*/
        //facebookView.updateLayoutViews();
    }

    private void requestPublishPermission() {

        /* Now a pending publish permission will be fired. */
        pendingPublishReauthorization = true;

        /* 1- Every Activity is a Context, but not every Context is an Activity
           2- Sometimes we pass context of an activity as a parameter to some other function and later we might want the activity too.
        So instead of passing the activity also to that function, we can get the activity from context itself.

        According to previous two points, I can cast context parameter to an activity since I am sure that it is an activity in
        my case */

        Session.NewPermissionsRequest newPermissionsRequest = new Session
                .NewPermissionsRequest((Activity) currentContext, PERMISSIONS);
        session.requestNewPublishPermissions(newPermissionsRequest);
    }

    public String getFacebookPageId(Location a_Location) {

        myLocation = a_Location;
        if (session != null) {

            /* Check if current session contains publish permissions */
            List<String> permissions = session.getPermissions();
            if (!isSubsetOf(PERMISSIONS, permissions)) {
                requestPublishPermission();
                return null;
            }

            Request request = prepSearchRequest();
            sendRequestAsync(request);
        }

        return null;
    }

    private void publishStoryWithLoc(Location a_Location) {

        myLocation = a_Location;

        if ((session != null) &&
                (placePageID != null)) {

            /* Check if current session contains publish permissions */
            List<String> permissions = session.getPermissions();
            if (!isSubsetOf(PERMISSIONS, permissions)) {
                requestPublishPermission();
                return;
            }

            Bundle postParams = prepBundleStoryWithLoc();
            Request request = prepPublishReq("me/checkins", postParams);
            sendRequestAsync(request);
        }
    }

    public void publishStoryWithLocOptimized(Location aLocation) {

        myLocation = aLocation;
        if (session != null) {
            // Check for publish permissions
            List<String> permissions = session.getPermissions();
            if (!isSubsetOf(PERMISSIONS, permissions)) {

                requestPublishPermission();
                return;
            } /* if permissions */

            /***************************************************************************************************
             * First Request to get place page id
             * *************************************************************************************************/

            Request searchPlacePageIDReq = prepSearchRequest();
            searchPlacePageIDReq.setBatchEntryName("PlacePageIDReq");

            /* You must maintain the result request back because by default, it is not returned. */
            searchPlacePageIDReq.setBatchEntryOmitResultOnSuccess(false);

            /***************************************************************************************************
             * Second Request to post a status associated with current user location
             * *************************************************************************************************/
            /* TODO: how can I set placeID in this multiple requests optimized technique. Check your stackoverflow post: http://goo.gl/MCUg6t*/
            Bundle postParams = prepBundleStoryWithLoc();

            /* Prepare story publish request and indicate that it depends on previous place page id request. */
            Request requestPublishStory = prepPublishReq("me/checkins", postParams);
            requestPublishStory.setBatchEntryDependsOn("PlacePageIDReq");

            /* Fill in the batch of requests then execute them asynchronously. */
            RequestBatch myBatch = new RequestBatch();
            myBatch.add(searchPlacePageIDReq);
            myBatch.add(requestPublishStory);
            myBatch.executeAsync();

        } /* if session not null */
    }

    public void publishStatusText(String a_msg) {
        if (session != null) {

            /* Check if current session contains publish permissions */
            List<String> permissions = session.getPermissions();
            if (!isSubsetOf(PERMISSIONS, permissions)) {
                requestPublishPermission();
                return;
            }

            Bundle postParams = new Bundle();

            /* Our case is just a text message that you can post on your wall.*/
            postParams.putString("message", " My Text Message ");

            /* Prepare your facebook request then send it to server. */
            Request request = prepPublishReq("me/feed", postParams);
            sendRequestAsync(request);
        }
    }

    public void publishStoryWithLink(String a_name, String a_caption, String a_description, String a_link, String a_picture) {
        if ((session != null) &&
                (placePageID != null)) {

            /* Check if current session contains publish permissions */
            List<String> permissions = session.getPermissions();
            if (!isSubsetOf(PERMISSIONS, permissions)) {
                requestPublishPermission();
                return;
            }


            Bundle postParams = prepBundleStoryWithLink(a_name, a_caption, a_description, a_link, a_picture);
            Request request = prepPublishReq("me/feed", postParams);
            sendRequestAsync(request);
        }
    }

    private Bundle prepBundleStoryWithLink(String a_name, String a_caption, String a_description, String a_link, String a_picture) {

        Bundle postParams = new Bundle();
        postParams.putString("name", a_name);                   /* "Facebook SDK for Android" */
        postParams.putString("caption", a_caption);             /* "Build great social apps and get more installs." */
        postParams.putString("description", a_description);     /* "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps." */
        postParams.putString("link", a_link);                   /* "https://developers.facebook.com/android" */
        postParams.putString("picture", a_picture);             /* "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png" */
        postParams.putString("message", " My Text Message ");

            /* TODO: Don't forget to check how would your Facebook post would look like in the following URL:
            * https://www.facebook.com/wael.showair/posts/10152292112008277 */
        return postParams;
    }

    private Bundle prepBundleStoryWithLoc() {

        Bundle postParams = new Bundle();
        postParams.putString("message", " My Text Message ");

            /* According to SDK documentation: place object contains id and name of Page associated with this location,
            and a location field containing geographic information such as latitude, longitude, country */
        postParams.putString("place", placePageID);

        JSONObject coordinates = new JSONObject();
        try {
            coordinates.put("latitude", myLocation.getLatitude());
            coordinates.put("longitude", myLocation.getLongitude());
            Log.i(FB_CLIENT_TAG, "adding latitude and longitude");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.w(FB_CLIENT_TAG, "Exception while adding latitude and longitude");
        }

        postParams.putString("coordinates", coordinates.toString());

        return postParams;
    }

    private Request prepPublishReq(String a_endPoint, Bundle a_postParams) {

        Request.Callback callback = new Request.Callback() {
            public void onCompleted(Response response) {
                JSONObject graphResponse = response
                        .getGraphObject()
                        .getInnerJSONObject();
                String postId = null;
                try {
                    postId = graphResponse.getString("id");
                } catch (JSONException e) {
                    Log.i(FB_CLIENT_TAG,
                            "JSON error " + e.getMessage());
                }
                FacebookRequestError error = response.getError();
                if (error != null) {
                    Log.i(FB_CLIENT_TAG, "Posting to wall faces an error which is: " + error.getErrorMessage());
                } else {
                    Log.i(FB_CLIENT_TAG, "Posting to wall successfully with the following FB id: " + postId);
                }
            }
        };

        /* feed endpoint is used to post text only in your status while
        * checkins endpoint is used to post(check in) a place map into your status */
        return new Request(session, a_endPoint, a_postParams,
                HttpMethod.POST, callback);
    }

    private Request prepSearchRequest() {

        /* Radius of search area in meters. */
        final int radius = 1000;

        /* Maximum number of search results that can be returned. */
        final int limitOfResults = 10;

        Request.GraphPlaceListCallback callback = new Request.GraphPlaceListCallback() {

            @Override
            public void onCompleted(List<GraphPlace> places, Response response) {

                try {
                    placePageID = places.get(0).getInnerJSONObject().getString("id");
                    Log.i(FB_CLIENT_TAG, "Place id is " + placePageID);
                    publishStoryWithLoc(myLocation);
                } catch (JSONException e) {
                    e.printStackTrace();

                        /* In case of error cases set page_id to Cairo City is: 115351105145884 or try to get current city id.
                        * TODO: send a link to google map as a facebook post adding marker to current user place. */
                    placePageID = "115351105145884";
                }
            }
        };

            /* This is the Graph API url that searches for places around your current location:
            * "search?type=place&center=30.0380279,31.2405339&distance=1000"*/
        return Request.newPlacesSearchRequest(session,
                myLocation, /* TODO: Error handling may be required */
                radius,
                limitOfResults,
                null,
                callback);
    }

    private void sendRequestAsync(Request a_request) {

        RequestAsyncTask task = new RequestAsyncTask(a_request);
        task.execute();
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {

            /* FacebookView is null when you don't have any updates in layout.*/
            if(null != facebookView)
                facebookView.updateLayoutViews();

            if (pendingPublishReauthorization &&
                    state.equals(SessionState.OPENED_TOKEN_UPDATED)) {

                pendingPublishReauthorization = false;

                getFacebookPageId(myLocation);

                /*publishStory(session);*/

                /*publishStory_v2(session);*/
            }
        }
    }

    public interface FacebookView {
        public void updateLayoutViews();
    }

    public final void addFacebookViewCallback(FacebookView a_FacebookView) {
        facebookView = a_FacebookView;
    }

    private boolean isSubsetOf(Collection<String> subset, Collection<String> superSet) {
        for (String string : subset) {
            if (!superSet.contains(string)) {
                return false;
            }
        }
        return true;
    }

    public boolean isPendingPublishReauthorization() {
        return pendingPublishReauthorization;
    }

    public void setPendingPublishReauthorization(boolean pendingPublishReauthorization) {
        this.pendingPublishReauthorization = pendingPublishReauthorization;
    }

    public Session.StatusCallback getStatusCallback() {
        return statusCallback;
    }

    public Session getSession() {
        return session;
    }

    public boolean isUserLoggedIn(){
        return !name.equals(DEFAULT_USER_NAME);
    }

    public void setUserName(String userName) {
        this.name = userName;
    }

    public String getUserName() {
        return name;
    }

    /* Class to obtain user profile picture in Drawable format, It takes facebook user id as an input*/
    private class ProfilePic extends AsyncTask<String, Void ,Bitmap>{

        private final String URL_PREFIX = "http://graph.facebook.com/";
        private final String URL_SUFFIX = "/picture?style=large";

        @Override
        protected Bitmap doInBackground(String... params) {
            /* Typical URL request is: "http://graph.facebook.com/"+id+"/picture?style=small" */
            String url = URL_PREFIX + params[0] + URL_SUFFIX;
            InputStream inputStream = null;
            try {
                /* Obtain bytes of user profile picture in small size. */
                inputStream = new URL(url).openStream();
            } catch (IOException e) {
                Log.e(FB_CLIENT_TAG, "Failed to get user profile picture");

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(currentContext)
                        .setTitle("Connection Error")
                        .setMessage("Turn on WiFi or Data Connection");

                alertDialog.show();
                e.printStackTrace();
            }

            /* Get Bitmap from input stream then convert it to Drawable object. */
            return  BitmapFactory.decodeStream(inputStream);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            bitmap = Bitmap.createScaledBitmap(bitmap,120,120,false);
            Drawable drawable = new BitmapDrawable(currentContext.getResources(), bitmap);

            /* Set instance variable that will be used to reference drawable object. */
            userProfilePicture = drawable;

            /* Now after getting the user profile picture, update corresponding preference icon. */
            if(null != facebookView)
                facebookView.updateLayoutViews();
        }
    }

    public Drawable getUserProfilePicture() {
        return userProfilePicture;
    }

    public void setUserProfilePicture(Drawable drawable) {
        userProfilePicture = drawable;
    }
     /* TODO: Check this link again for the guidelines of permissions.
    * https://developers.facebook.com/docs/facebook-login/permissions */

    /* TODO: Think about using Open Graph story to publish as described in this link:
    * https://developers.facebook.com/docs/android/scrumptious/publish-open-graph-story */

    /* TODO: It seems that it will be very useful to use advanced features described here: http://goo.gl/IQosfp*/

}
