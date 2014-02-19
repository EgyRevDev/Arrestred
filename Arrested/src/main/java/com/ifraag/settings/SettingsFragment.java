package com.ifraag.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.ifraag.arrested.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
/* Note that PreferenceFragment class requires API level 11 but current minimum SDK is 7 so
 * I have to add target API annotation telling Lint that I am sure that I will use this class only when API level greater than or equal to 11*/
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Since it can't be called from PreferenceActivity class.
        * Be careful to call getActivity() only when the fragment is attached to an activity.
        * When the fragment is not yet attached, or was detached during the end of its lifecycle, getActivity() will return null.*/
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        /* Since all fragments of Sub-screens are displayed on same Settings Activity so you have to change Activity title according
        * to the given fragment. */
        getActivity().getActionBar().setTitle(R.string.title_activity_settings);

        /*Load the preferences from an XML resource*/
        addPreferencesFromResource(R.xml.preferences);

        PreferenceGroup pg = (PreferenceGroup) getPreferenceManager().findPreference("pref_category_accounts");
        /* Check if there are any configured account types, Display all*/
        if (0 != CustomPreference.preferencesList.size()){

            for(Preference pref:CustomPreference.preferencesList)
                pg.addPreference(pref);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
