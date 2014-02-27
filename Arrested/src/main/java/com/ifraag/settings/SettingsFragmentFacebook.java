package com.ifraag.settings;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;


import com.facebook.android.Facebook;
import com.ifraag.arrested.R;
import com.ifraag.arrested.SettingsActivityFacebook;
import com.ifraag.facebookclient.FacebookClient;

/* Note that PreferenceFragment class requires API level 11 but current minimum SDK is 7 so
 * I have to use unofficial android-support-v4-PreferenceFragment library project to be able to use Preference Fragment
 * on any Android API whose level is 4+ */
public class SettingsFragmentFacebook extends PreferenceFragment {

    private Preference mPreference;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Important note: if passed bundle is not null, it will contain two keys: android:preferences & android:view_state
        * Try Set<String> set = savedInstanceState.keySet() It has no relation with the bundle of the Activity context
        * within which PreferenceFragment is running.*/

         /* Be careful to call getActivity() only when the fragment is attached to an activity.
        * When the fragment is not yet attached, or was detached during the end of its lifecycle or device orientation changes,
        * getActivity() will return null.*/
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        if(null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            /* Since all fragments of Sub-screens are displayed on same Settings Activity so you have to change Activity title according
            * to the given fragment. */
            actionBar.setTitle(R.string.title_activity_settings_facebook);
        }

        /*Load the preferences from an XML resource*/
        addPreferencesFromResource(R.xml.preferences_facebook);

        /* Set default icon to default facebook login icon */
        mPreference = findPreference("pref_facebook_account");
        if ( null != mPreference) {
            updatePreferenceAttributes(FacebookClient.DEFAULT_USER_NAME,
                    getResources().getDrawable(R.drawable.com_facebook_profile_default_icon));

            /* Following line must be added to handle corner case that user has logged in Facebook Settings Screen
            * then, it gets back/up to Settings Screen and finally it gets into Facebook Settings again.
            * In this case preferences takes time to be loaded hence updateLayoutViews method will not update
            * Displayed username and profile picture since mPreference was still null.
            * To fix this problem, simply call the callback after being sure that preferences have been loaded.*/
            SettingsActivityFacebook.myFacebookView.updateLayoutViews();
        }

        /* TODO: Remove, this was just for testing. */
        Preference preference2 = findPreference("pref_facebook_account_v2");
        preference2.setIcon(R.drawable.com_facebook_profile_picture_blank_portrait);
        preference2.setTitle("profile_picture_blank_portrait");

        Preference preference3 = findPreference("pref_facebook_account_v3");
        preference3.setIcon(R.drawable.com_facebook_profile_picture_blank_square );
        preference3.setTitle("profile_picture_blank_square");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void updatePreferenceAttributes(String a_userName, Drawable a_drawable){
        if(null != mPreference) {
            mPreference.setTitle(a_userName);
            mPreference.setIcon(a_drawable);
        }
    }

    public Preference getPreference() {
        return mPreference;
    }
}
