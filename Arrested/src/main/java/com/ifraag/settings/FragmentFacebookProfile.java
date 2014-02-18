package com.ifraag.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.ifraag.arrested.R;

public class FragmentFacebookProfile extends Fragment {

    private static final String TAG ="Frag-Profile";

    /*  constant that you'll use later on whenever you make a new permissions request.
    You'll use it to decide whether to update a session's info in the onActivityResult() method*/
    private static final int REAUTH_ACTIVITY_CODE = 100;

    /* Instance variable to set user's profile picture and name. */
    private ProfilePictureView profilePictureView;
    private TextView userNameView;

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fb_profile, container, false);

            /* Assign instance variables to respective layout elements. */
        profilePictureView = (ProfilePictureView) rootView.findViewById(R.id.selection_profile_pic);
        profilePictureView.setCropped(true);

        userNameView = (TextView) rootView.findViewById(R.id.selection_user_name);

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Get the user's data
            getUserProfileInformation(session);
        }
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REAUTH_ACTIVITY_CODE) { /*TODO: I don't get it why we just can't call uiHelper directly?!!*/
            uiHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        uiHelper.onSaveInstanceState(bundle);
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

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
                /*Get the user's data.*/
            getUserProfileInformation(session);
        }
    }
    private void getUserProfileInformation(final Session session) {

        Log.i(TAG, "Prepare Graph Request to get logged in user basic information");

            /*Make an API call to get user data and define a new callback to handle the response.*/
        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                            /*If the response is successful*/
                        if (session == Session.getActiveSession()) {
                            if (user != null) {

                                Log.d(TAG, "response is received correctly");

                                    /*Set the id for the ProfilePictureView view that in turn displays the profile picture.*/
                                profilePictureView.setProfileId(user.getId());
                                    /*Set the Textview's text to the user's name.*/
                                userNameView.setText(user.getName());
                            } else {
                                Log.w(TAG, "successful response with null user Information");
                            }
                        }

                        if (response.getError() != null) {
                            Log.d(TAG, "Error while retrieving response");
                        }
                    }
                });
            request.executeAsync(); /* This is asynchronous task since there is network communication*/
    }
}