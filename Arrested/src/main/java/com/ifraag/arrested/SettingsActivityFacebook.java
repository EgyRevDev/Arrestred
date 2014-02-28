package com.ifraag.arrested;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
    MenuItem actionBarLoginLogout;

    /* An instance of Facebook Settings Fragment that is running within current Activity. It contains current user name & profile picture*/
    SettingsFragmentFacebook settingsFragmentFacebook;

    public static MyFBView myFacebookView;
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

        /* Save username as well as boolean flag indicating pending publish request */
        saveToSharedPreferences();

        /* Save user profile picture into separate private file so that it can be user in case of device orientation
        * change or app re-sarting after killing.*/
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

        /* Load profile picture from private file and set it into facebook client instance. */
        Drawable d = loadProfilePic();
        facebookClient.setUserProfilePicture(d);

        /* Update your layout views accordingly to display previous saved name/profile picture.*/
        myFacebookView.updateLayoutViews();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        /*Inflate the menu; this adds items to the action bar if it is present. Note that this method is called only once
        * upon creating menu*/

        getMenuInflater().inflate(R.menu.settings_activity_child_fb, menu);
        actionBarLoginLogout = menu.findItem(R.id.action_login_logout);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /* This method is called right before the menu is shown, every time it is shown.
        You can use this method to efficiently enable/disable items or otherwise dynamically modify the contents. */

        /* Check user login state, if user has logged in, then the menu action button must display logout.
        * If user has logged out, then the e=menu action button must display login*/
        if(facebookClient.isUserLoggedIn()){
            actionBarLoginLogout.setTitle("logout");
        }else
            actionBarLoginLogout.setTitle("login");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*Handle action bar item clicks here. The action bar will
        automatically handle clicks on the Home/Up button, so long
        as you specify a parent activity in AndroidManifest.xml.*/
        int id = item.getItemId();

        switch (id){
            case android.R.id.home: //case up caret is pressed.
                restartParentActivity();
                break;

            case R.id.action_login_logout:
                /* Check login state of the user to handle the button press. In case login is required, obtain
                * user name and profile picture using facebook client. But in case logout is required,
                * ask facebook client to perform facebook session logout */
                if (item.getTitle().toString().equalsIgnoreCase("login")){
                    facebookClient.getUserProfileInformation();
                }else if (item.getTitle().toString().equalsIgnoreCase("logout")){
                    facebookClient.logout();
                }
                break;
            case R.id.action_help:
                Toast.makeText(this, "Help TBD", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        /* In case Back button is pressed, restart Settings Activity. I have to do this because of fragment misbehavior
        * that I can't understand 100% */
        restartParentActivity();
    }

    private void restartParentActivity () {

       /* Since onSaveInstanceState is not called if Back/Up button is pressed, you have to save dynamic preferences
        * in shared preferences explicitly. As well as user profile picture to private file.*/
        saveToSharedPreferences();
        saveUserProfilePic(facebookClient.getUserProfilePicture());

        Intent intent = new Intent(this, SettingsActivity.class);

        /* Since Settings Activity has an intent filter for "preferences" scheme in AndroidManifest file, then you have to
         * set Uri before starting activity otherwise Settings Activity will not be started and an Exception is triggered. */
        intent.setAction("com.ifraag.arrested.action.VIEW");

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
    public class MyFBView implements FacebookClient.FacebookView {
        private final String TAG="MyFBView";
        @Override
        public void updateLayoutViews() {
            /* Do nothing in the UI when the session status changes. Everything will be sent without any need to user interaction */

            Session session = facebookClient.getSession();
            if (session.isOpened()) {
                Log.i(TAG, "updateLayoutViews: State of session is opened");

                /* Check if user has logged in successfully to FB*/
                if(facebookClient.isUserLoggedIn()){

                    /* Update username and profile picture with facebook client corresponding fields. */
                    settingsFragmentFacebook.updatePreferenceAttributes(facebookClient.getUserName(),
                            facebookClient.getUserProfilePicture());

                        /* Check the login state of the user to update the title of the corresponding action button.
                         * Note that sometimes current method is called before loading Menu items so you have to
                         * handle this corner case. */
                        if(null != actionBarLoginLogout)
                            actionBarLoginLogout.setTitle("logout");

                }
            } else { /*Case Facebook Session is closed.*/
                Log.i(TAG,"updateLayoutViews:Session is closed");

                /* Update username and profile picture with facebook client corresponding fields. */
                settingsFragmentFacebook.updatePreferenceAttributes(facebookClient.getUserName(),
                        facebookClient.getUserProfilePicture());

                /* Check the login state of the user to update the title of the corresponding action button.
                 * Note that sometimes current method is called before loading Menu items so you have to
                 * handle this corner case. */
                if(null != actionBarLoginLogout)
                    actionBarLoginLogout.setTitle("login");
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

        /* Set facebook client with corresponding save fields. */
        facebookClient.setPendingPublishReauthorization(pendingPublishReauthorization);
        facebookClient.setUserName(username);
    }

    /* Method to save Facebook Profile picture to private file accessible by your application only.*/
    private void saveUserProfilePic(Drawable drawable){

        final int QUALITY=100;
        /* Convert give user profile picture into BitMap. */
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

        /* Create an instance of Byte Array that will hold bytes of the bitmap. */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        /* Compress and Save bitmap to PNG file as sequence of bytes. */
        bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY , byteArrayOutputStream);
        byte[] byteArrayPic= byteArrayOutputStream.toByteArray();

        /* Save bytes of the given Pic into a private file*/
        try {
            /* Create new file profile_pic.png into YOUR_APP_PACKAGE/files/Pictures/profile_pic.png.
            * Absolute Path to access in DDMS: /storage/emulated/0/Android/data/com.ifraag.arrested/files/Pictures/profile_pic.png*/
            /* Get the absolute path to the Pictures directory on the primary external filesystem then use it to
            * create your own PNG file*/
            File out = new File(getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), FILE_NAME);
            out.setWritable(true);
            FileOutputStream fileOutStream = new FileOutputStream(out);

            /* Write bytes array of the given picture into the corresponding file then close the output PNG file. */
            fileOutStream.write(byteArrayPic);
            fileOutStream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /* Method to load PNG private file of user profile picture as Drawable to be set inside facebook client. */
    private Drawable loadProfilePic(){

        Drawable drawable;

        final int WIDTH=120;
        final int HEIGHT=120;

        /* Check state of user login, In case user has already logged in, then load the saved profile picture.
        * Otherwise, return default facebook profile icon that indicates that user has logged out.*/
        if(facebookClient.isUserLoggedIn()) {
            /* Absolute Path to access in DDMS: /storage/emulated/0/Android/data/com.ifraag.arrested/files/Pictures/profile_pic.png*/
            /* Get the absolute path to the Pictures directory on the primary external filesystem then use it to
            * reach your own PNG file*/
            File in = new File(getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), FILE_NAME);

            /* Convert bytes in the saved PNG file into a BitMap.*/
            Bitmap bitmap = BitmapFactory.decodeFile(in.toString());

            /* Since profile picture obtained from facebook has very small size, You need to resize it to be 120x120 pixels.*/
            bitmap = Bitmap.createScaledBitmap(bitmap,WIDTH,HEIGHT,false);

            /* Finally convert BitMap into Drawable object.*/
            drawable =  new BitmapDrawable(getResources(),bitmap);

        }else{ // case user has logged out from Facebook session.
            /* Simply load default facebook profile icon from resources. */
            drawable =  getResources().getDrawable(R.drawable.com_facebook_profile_default_icon);
        }

        return drawable;
    }
}
