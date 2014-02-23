package com.ifraag.arrested;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.Session;
import com.ifraag.facebookclient.FacebookClient;
import com.ifraag.settings.SettingsFragmentFacebook;

public class SettingsActivityFacebook extends ActionBarActivity {

    FacebookClient facebookClient;

    MenuItem actionBarLogin;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_activity_child_fb);


            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(
                    getResources().getString(R.string.title_activity_settings) +
                            "  "+
                            getResources().getString(R.string.pref_facebook_title));

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragmentFacebook())
                    .commit();

        /* An instance of FacebookView interface to implement how would my layout views change as result for facebook session
        * state changes. */
        MyFBView myFacebookView = new MyFBView();
        facebookClient = new FacebookClient(this, myFacebookView );
        facebookClient.activateSession(getIntent().getExtras());
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* Add session callback that will be called in case session state is changed */
        facebookClient.getSession().addCallback(facebookClient.getStatusCallback());
    }

    @Override
    protected void onStop() {
        super.onStop();

        /* Add session callback that will be called in case session state is changed */
        facebookClient.getSession().addCallback(facebookClient.getStatusCallback());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* Update active session with active permission whether it is read or publish permission */
        facebookClient.getSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(FacebookClient.PENDING_PUBLISH_KEY,
                facebookClient.isPendingPublishReauthorization());

        Session.saveSession(facebookClient.getSession(), outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_activity_child_fb, menu);
        actionBarLogin = menu.findItem(R.id.action_login);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*Handle action bar item clicks here. The action bar will
        automatically handle clicks on the Home/Up button, so long
        as you specify a parent activity in AndroidManifest.xml.*/
        int id = item.getItemId();

        if (id == android.R.id.home){
            restartParentActivity();
            return true;
        }

        if (id == R.id.action_help) {

            Toast.makeText(this, "Help is pressed", Toast.LENGTH_SHORT).show();
            return true;
        }

        if(id == R.id.action_login) {

            if (item.getTitle().toString().equalsIgnoreCase("login")){
                facebookClient.getUserProfileInformation();
            }else if (item.getTitle().toString().equalsIgnoreCase("logout")){
                /* TODO: Add Logout method in Facebook client. */
                Toast.makeText(this,"LOGOUT TBD",Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        restartParentActivity();
    }

    private void restartParentActivity () {
        Intent intent = new Intent(this, SettingsActivity.class);

        /* Since Settings Activity has an intent filter for "preferences" scheme in AndroidManifest file, then you have to
         * set Uri before starting activity otherwise Settings Activity will not be started and an Exception is triggered. */
        intent.setData(Uri.parse("preferences://activity1"));

        /* With this flag, if the activity you're starting already exists in the current task,
         * then all activities on top of it are destroyed and it is brought to the front.
         * you should usually not create a new instance of the home activity. Otherwise,
         * you might end up with a long stack of activities in the current task with multiple
         * instances of the home activity.*/
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void editPreferenceEntry (String a_userName, Drawable a_drawable){
        if (null != a_userName)
            SettingsFragmentFacebook.mPreference.setTitle(a_userName);

        if(null != a_drawable)
            SettingsFragmentFacebook.mPreference.setIcon(a_drawable);
    }

    /* Create an instance from FacebookView interface implementing updateLayoutViews method that should be called whenever it is
     * required to update the view contents of your layout. In fact, it will be called from facebook session state change callback. */
    private class MyFBView implements FacebookClient.FacebookView {
        @Override
        public void updateLayoutViews() {
            /* Do nothing in the UI when the session status changes. Everything will be sent without any need to user interaction */

            Session session = facebookClient.getSession();
            if (session.isOpened()) {
                Log.i("MyFBClient", "State of session is " + session.getState());

                /* Check if user has logged in successfully to FB*/
                if(facebookClient.isUserLoggedIn()){
                    /* Change text of Login to Logout, Set username and profile picture in the preference. */
                    actionBarLogin.setTitle("logout");
                    editPreferenceEntry(facebookClient.getUserName(), facebookClient.getUserProfilePicture());
                }
            } else {
                Log.i("MyFBClient","Session is closed");
                //actionBarLogin.setTitle("login");
            }
        }
    }
}
