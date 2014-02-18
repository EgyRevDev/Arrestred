package com.ifraag.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ifraag.arrested.R;

public class FragmentFacebookLike extends Fragment {

    private static final String TAG = "FragmentFB";
    private ImageButton imgBtnIfraag;
    private ImageButton imgBtnNoMilTrials;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_fb_like, container, false);
        /* Take an a reference to Facebook Like Button. */
        imgBtnIfraag = (ImageButton) rootView.findViewById(R.id.like_btn_ifraag);
        imgBtnIfraag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Open webview with corresponding facebook page url*/
                String url = "https://www.facebook.com/Ifraag";
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
            }
        });

        imgBtnNoMilTrials = (ImageButton) rootView.findViewById(R.id.like_btn_nomiltrials);
        imgBtnNoMilTrials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Open webview with corresponding facebook page url*/
                String url = "https://www.facebook.com/NoMilTrials";
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
            }
        });

        return rootView;
    }
}
