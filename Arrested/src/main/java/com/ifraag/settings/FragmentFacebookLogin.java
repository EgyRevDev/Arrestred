package com.ifraag.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.ifraag.arrested.R;

import java.util.Arrays;

public class FragmentFacebookLogin extends Fragment {

    private static final String TAG = "FragmentFB";

    /* Define a variable that implements session changes listener. */
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session,state,exception);
        }
    };

    private UiLifecycleHelper uiHelper;

    public FragmentFacebookLogin() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_fb_login, container, false);
            /* Take an a reference to Facebook Login Button. */
        LoginButton authButton = (LoginButton) rootView.findViewById(R.id.authButton);
            /* To let my fragment handle the Main Activity result, Fragment needs to enable the reception of result of
            * Main Activity. This is achieved by calling setFragment method. */
        authButton.setFragment(this);

        authButton.setReadPermissions(Arrays.asList("basic_info"));
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            /* Create a new instance of UI Helper class. */
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
            /* creates the Facebook session and opens it automatically if a cached token is available. */
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
            /*For scenarios where the main activity is launched and user session is not null, the session state change notification
            may not be triggered. Trigger it if it's open/closed.
            For these type of scenarios, trigger the onSessionStateChange() method whenever fragment is resumed.
            */
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    /* callback method for changing facebook session changes.*/
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {

            /* Just interested in state transition to opened or closed. */
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
                /*TODO: Change respective UI layout. */
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
                /* TODO: Change respective UI layout */
        }
    }
}