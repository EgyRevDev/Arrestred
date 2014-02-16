package com.ifraag.arrested;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ifraag.settings.SettingsFragment;
import com.ifraag.settings.SettingsFragmentFacebook;

public class SettingsActivity extends PreferenceActivity {

    /* String represents the scheme value passed to Settings Activity in the intent. */
    private String mScheme;
    @SuppressWarnings("deprecation") /* To remove Lint warning for using deprecated method addPreferencesFromResource. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /* Get scheme value from the passed activity intent*/
        mScheme = getIntent().getData().toString();

        /* Check running Android API version. If it is less than API Level 11, use deprecated methods to respective Settings */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

            /* Case given scheme is related to FB/Parent Settings activity. Not that it is passed in preferences.xml file as extra
             * data to start Activity. */
            if(mScheme.equals("preferences://fb_activity")) {
                addPreferencesFromResource(R.xml.preferences_facebook);
            } else {
                addPreferencesFromResource(R.xml.preferences);
            }
        }else{ /* Case running Android is greater than or equal to API Level 11 */

            /*if (savedInstanceState == null)*/ /* TODO: I am not sure why did I comment this line? will it cause any run-time errors?*/

            /* Case given scheme is related to FB/Parent Settings activity. Not that it is passed in preferences.xml file as extra
             * data to start Activity. */
            if(mScheme.equals("preferences://fb_activity")){
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new SettingsFragmentFacebook())
                        .commit();
            }else {
                /* Too important note; PreferenceFragment has been introduced in API 11 which equals to minimum SDK version
                 * of my current project so I did not face any compilation error when using an instance of PreferenceFragment.
                 * Otherwise I have got a compilation error since PreferenceFragment is not back-ported into support library v4.*/
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new SettingsFragment())
                        .commit();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /* Check if Up button (Caret beside application logo) is pressed. If Settings Activity contains a sub-screen fragment
        * then it should move to Parent Settings Activity. Otherwise, if Settings Activity contains Parent Settings Activity,
        * then it should move to Main Activity.
        * All of this must be handled in both cases pre/post Android API 11. */
        if (id == android.R.id.home){
            /* Case current scheme value refers to sub-screen such as facebook, twitter or gmail setting screen. */
            if(!mScheme.equals("preferences://activity1")){

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
                    /* Load Parent Settings Activity. */
                    addPreferencesFromResource(R.xml.preferences);
                }else { /* Load Parent Settings fragment in Settings Activity. */

                    /* Too important note; PreferenceFragment has been introduced in API 11 which equals to minimum SDK version
                     * of my current project so I did not face any compilation error when using an instance of PreferenceFragment.
                     * Otherwise I have got a compilation error since PreferenceFragment is not back-ported into support library v4.*/
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, new SettingsFragment())
                            .commit();

                    /* Change the saved scheme value to point to Parent Setting Activity so that when Settings is restarted,
                    * it loads the correct Parent Settings Activity. */
                    mScheme = "preferences://activity1";
                }
            }else{ /* Case current scheme value refers to Parent settings screen.*/

                Intent intent = new Intent(this, MainActivity.class);

                /* With this flag, if the activity you're starting already exists in the current task,
                 * then all activities on top of it are destroyed and it is brought to the front.
                 * you should usually not create a new instance of the home activity. Otherwise,
                 * you might end up with a long stack of activities in the current task with multiple
                 * instances of the home activity.*/

                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }

        if (id == R.id.action_help) {

            Toast.makeText(this, "Help is pressed", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);

        /* Too important note; I did not handle R.id.home since I have already added in the Manifest file:
        * 1- parentActivityName attribute (starting from API 16)
        * 2- Meta data to support older devices. */
    }

    @Override
    public void onBackPressed() {
    /* It seems that either i still don't understand fragments well or there is a bug in Android so I have to workaround
    Back button pressed behaviour since, by above code if pressed Up button from Facebook sub-screen then
    back from settings screen, You will have another settings screen not Main Activity. */

        if (mScheme.equals("preferences://activity1")){
            Intent intent = new Intent(this, MainActivity.class);

                /* With this flag, if the activity you're starting already exists in the current task,
                 * then all activities on top of it are destroyed and it is brought to the front.
                 * you should usually not create a new instance of the home activity. Otherwise,
                 * you might end up with a long stack of activities in the current task with multiple
                 * instances of the home activity.*/

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else
            super.onBackPressed();
    }
}
