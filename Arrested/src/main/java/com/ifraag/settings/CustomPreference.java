package com.ifraag.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.preference.Preference;

import com.ifraag.arrested.R;
import com.ifraag.arrested.SettingsActivityFacebook;


public class CustomPreference extends Preference {

    int mIconResourceID;
    private Context mContext;
    CustomPreferenceClickListener mClickListener;

    public CustomPreference(Context context) {
        super(context);
        mContext = context;
        mClickListener = new CustomPreferenceClickListener();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void setIcon(int iconResId) {
        mIconResourceID = iconResId;
        super.setIcon(iconResId);
    }

    public int getIconResId() {
        return mIconResourceID;
    }

    @Override
    public OnPreferenceClickListener getOnPreferenceClickListener() {
        super.getOnPreferenceClickListener();

        return mClickListener;
    }

    private class CustomPreferenceClickListener implements Preference.OnPreferenceClickListener{
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent;

            /* Check what is the title of the give preference and start the corresponding activity.*/
            Resources resources = mContext.getResources();
            if (preference.getTitle().equals(resources.getString(R.string.pref_facebook_title))) {
                intent = new Intent(mContext, SettingsActivityFacebook.class);
                intent.setAction("com.ifraag.arrested.action.VIEW");
                preference.setIntent(intent);
                mContext.startActivity(intent);
            } else if (preference.getTitle().equals(resources.getString(R.string.pref_twitter_title))) {

            } else if (preference.getTitle().equals(resources.getString(R.string.pref_gmail_title))) {

            }
            return false;
        }
    }
}
