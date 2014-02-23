package com.ifraag.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;


import com.ifraag.arrested.R;

/* Note that PreferenceFragment class requires API level 11 but current minimum SDK is 7 so
 * I have to add target API annotation telling Lint that I am sure that I will use this class only when API level greater than or equal to 11*/
public class SettingsFragmentFacebook extends PreferenceFragment {

    public static Preference mPreference;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Since it can't be called from PreferenceActivity class.
        * Be careful to call getActivity() only when the fragment is attached to an activity.
        * When the fragment is not yet attached, or was detached during the end of its lifecycle, getActivity() will return null.*/
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        /* Since all fragments of Sub-screens are displayed on same Settings Activity so you have to change Activity title according
        * to the given fragment. */
        actionBar.setTitle(R.string.pref_facebook_title);

        /*Load the preferences from an XML resource*/
        addPreferencesFromResource(R.xml.preferences_facebook);

        /* Set default icon to default facebook login icon */
        mPreference = findPreference("pref_facebook_account");
        mPreference.setIcon(R.drawable.com_facebook_profile_default_icon);
        mPreference.setTitle("Username");

        /* TODO: Remove, this was just for testing. */
        Preference preference2 = findPreference("pref_facebook_account_v2");
        preference2.setIcon(R.drawable.com_facebook_profile_picture_blank_portrait);
        preference2.setTitle("profile_picture_blank_portrait");

        Preference preference3 = findPreference("pref_facebook_account_v3");
        preference3.setIcon(R.drawable.com_facebook_profile_picture_blank_square );
        preference3.setTitle("profile_picture_blank_square");
    }
}
