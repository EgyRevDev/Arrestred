package com.ifraag.arrested;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.Session;
import com.ifraag.facebookclient.FacebookClient;
import com.ifraag.settings.SettingsFragmentFacebook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingsActivityFacebook extends ActionBarActivity {

    /* An instance of Facebook client. It is responsible for wrapping Facebook GRAPH API communication*/
    FacebookClient facebookClient;

    /* An instance of Login/Logout Action Bar button. Its text title should be changed according to login state of user.*/
    MenuItem actionBarLogin;

    /* An instance of Facebook Settings Fragment that is running within current Activity. It contains current user name & profile picture*/
    SettingsFragmentFacebook settingsFragmentFacebook;

    MyFBView myFacebookView;
    private String FILE_NAME="profile_pic.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_activity_child_fb);

        /* Enable the app icon as an Up button */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingsFragmentFacebook = new SettingsFragmentFacebook();
        /* Add Preference Fragment to the current Activity context. */
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, settingsFragmentFacebook)
                .commit();

        /* An instance of FacebookView interface to implement how would my layout views change at run-time as result for
        * facebook session state changes. */
        myFacebookView = new MyFBView();

        /* Initialize facebook client instance variable with the layout callback to be invoked upon session changes. */
        facebookClient = new FacebookClient(this, myFacebookView);

        if(null == savedInstanceState){
            loadFromSharedPreference();
            Drawable d = loadProfilePic();
            facebookClient.setUserProfilePicture(d);
        }

        /* Activate user's facebook session. */
        facebookClient.activateSession(/*savedInstanceState*/);
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

        /*TODO: Not sure whether to save session or not.*/
        //Session.saveSession(facebookClient.getSession(), outState);

        /* Save username upon screen rotation */
        outState.putString(FacebookClient.KEY_USER_NAME,
                facebookClient.getUserName());

        saveToSharedPreferences();

        saveUserProfilePic(facebookClient.getUserProfilePicture());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        //Session session = Session.restoreSession(this, null, facebookClient.getStatusCallback(), savedInstanceState);
        boolean pendingPublishReauthorization = savedInstanceState.getBoolean(FacebookClient.PENDING_PUBLISH_KEY, false);
        String userName = savedInstanceState.getString(FacebookClient.KEY_USER_NAME);

        //facebookClient.setSession(session);
        facebookClient.setPendingPublishReauthorization(pendingPublishReauthorization);
        facebookClient.setUserName(userName);

        Drawable d = loadProfilePic();
        facebookClient.setUserProfilePicture(d);

        myFacebookView.updateLayoutViews();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_activity_child_fb, menu);
        actionBarLogin = menu.findItem(R.id.action_login);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(facebookClient.isUserLoggedIn()){
            actionBarLogin.setTitle("logout");
        }else
            actionBarLogin.setTitle("login");
        return super.onPrepareOptionsMenu(menu);
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

            Toast.makeText(this, "Help TBD", Toast.LENGTH_SHORT).show();
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
                if(facebookClient.isUserLoggedIn() &&
                        null != settingsFragmentFacebook.getPreference()){

                    settingsFragmentFacebook.updatePreferenceAttributes(facebookClient.getUserName(),
                            facebookClient.getUserProfilePicture());

                        /* Check the login state of the user to update the title of the corresponding action button. */
                        if(null != actionBarLogin)
                            actionBarLogin.setTitle("logout");

                }
            } else { /*Case Facebook Session is closed.*/
                Log.i("MyFBClient","Session is closed");
                if(null != actionBarLogin)
                    actionBarLogin.setTitle("login");
            }
        }
    }

    private void saveToSharedPreferences() {
        /* Get reference to shared preferences file whose name is typical to your application package name.
        * It is shared among your application activities but not accessible to other application (private mode)*/
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        /* Get an instance of editor to the file of shared preferences. */
        SharedPreferences.Editor editor = sharedPreferences.edit();

        /* Save Facebook session parameters; username and boolean flag indicating if pending publish is requested  */
        editor.putBoolean(FacebookClient.PENDING_PUBLISH_KEY,
                facebookClient.isPendingPublishReauthorization());

        editor.putString(FacebookClient.KEY_USER_NAME,
                facebookClient.getUserName());

        /* Finally commit all changes that you have made to the shared preferences file. */
        editor.commit();
    }

    private void loadFromSharedPreference() {
        /* Get reference to shared preferences file whose name is typical to your application package name.
        * It is shared among your application activities but not accessible to other application (private mode)*/
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        boolean pendingPublishReauthorization = sharedPreferences.getBoolean(FacebookClient.PENDING_PUBLISH_KEY,false);
        String username = sharedPreferences.getString(FacebookClient.KEY_USER_NAME, FacebookClient.DEFAULT_USER_NAME);

        facebookClient.setPendingPublishReauthorization(pendingPublishReauthorization);
        facebookClient.setUserName(username);
    }

    private void saveUserProfilePic(Drawable drawable){

        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();

        try {
            File out = new File(getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), FILE_NAME);
            out.setWritable(true);
            FileOutputStream fileOutStream = new FileOutputStream(out);

            fileOutStream.write(b);  //b is byte array
            //(used if you have your picture downloaded
            // from the *Web* or got it from the *devices camera*)
            //otherwise this technique is useless
            fileOutStream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private Drawable loadProfilePic(){

        Drawable drawable;
        if(facebookClient.isUserLoggedIn()) {
            //File filePath = getFileStreamPath(FILE_NAME);
            File in = new File(getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), FILE_NAME);
            drawable =  Drawable.createFromPath(in.toString());
        }else{
            drawable =  getResources().getDrawable(R.drawable.com_facebook_profile_default_icon);
        }

        return drawable;
    }
}
