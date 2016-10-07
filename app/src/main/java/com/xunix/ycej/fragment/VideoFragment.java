package com.xunix.ycej.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.radaee.reader.R;
import com.xunix.ycej.help.RecordActivity;

/**
 * Created by xunixhuang on 07/10/2016.
 */

public class VideoFragment extends Fragment {
    CardView startButton;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_video, container, false);
        startButton=(CardView)v.findViewById(R.id.cardViewVideo);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), RecordActivity.class));
            }
        });
        return v;
    }
}
