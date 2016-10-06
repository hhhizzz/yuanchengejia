package com.xunix.ycej.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.radaee.reader.R;
import com.xunix.ycej.adapter.StoryAdapter;

/**
 * Created by xunixhuang on 06/10/2016.
 */

public class StoryFragment extends Fragment {
    private RecyclerView storyList;
    private StoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_story, container, false);
        storyList = (RecyclerView) v.findViewById(R.id.storyList);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        storyList.setLayoutManager(llm);
        adapter = new StoryAdapter(getActivity());
        storyList.setAdapter(adapter);
        return v;
    }
}
