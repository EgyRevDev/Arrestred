package com.ifraag.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.preference.Preference;

import com.ifraag.arrested.R;
import com.ifraag.arrested.SettingsActivityFacebook;

/* Custom Preference that is added during run-time. The main point behind this class is to
    1- Wrap the Listener instance that will be executed upon clicking on this custom preference.
    2- define special method to retrieve drawable resource id attached to the icon of this custom preference.
 */
public class CustomPreference extends Preference {

    /* An instance of Activity context within which custom preference is running. */
    private Context mContext;

    /* An instance of icon resource ID that is coupled with custom preference.*/
    int mIconResourceID;

    /* An instance of custom preference click listener that will be invoked upon clicking on custom preference. */
    CustomPreferenceClickListener mClickListener;

    public CustomPreference(Context context) {
        super(context);

        /* Initialize context and click listener. */
        mContext = context;
        mClickListener = new CustomPreferenceClickListener();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void setIcon(int iconResId) {
        /* Unfortunately, setIcon is added in API 11 and not backported to any support library.
        * Check out this post: http://goo.gl/YpSXPb*/

        /* Save the given icon resource ID then continue normal Android behavior. */
        mIconResourceID = iconResId;
        super.setIcon(iconResId);
    }

    public int getIconResId() {
        /* Important method that wasn't found in Android APIs so I have to implement it myself. */
        return mIconResourceID;
    }

    @Override
    public OnPreferenceClickListener getOnPreferenceClickListener() {
        super.getOnPreferenceClickListener();

        /* Get an instance of click listener that is coupled with this custom preference. */
        return mClickListener;
    }

    /* Internal class that represents click listener interface and it is wrapped inside this custom preference.
    * It can be obtained through getOnPreferenceClickListener method. */
    private class CustomPreferenceClickListener implements Preference.OnPreferenceClickListener{
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent;

            /* Check what is the title of the give preference and start the corresponding setting Activity.*/
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
