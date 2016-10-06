package com.xunix.ycej.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.xunix.ycej.MainActivity;
import com.radaee.reader.R;
import com.xunix.ycej.MyPDFReader;
import com.xunix.ycej.adapter.HomeWorkAdapter;
import com.xunix.ycej.utils.FileSave;

/**
 * Created by xunixhuang on 06/10/2016.
 */

public class HomeworkFragment extends Fragment {
    private RecyclerView homworkList;
    private HomeWorkAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_homework, container, false);
        homworkList = (RecyclerView) v.findViewById(R.id.homeworkList);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        homworkList.setLayoutManager(llm);
        adapter = new HomeWorkAdapter(getActivity());
        homworkList.setAdapter(adapter);
        adapter.setClickListener(new HomeWorkAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent=new Intent(getActivity(), MyPDFReader.class);
                intent.putExtra("filepath", FileSave.getHomeworkFiles().get(position).getAbsolutePath());
                startActivity(intent);
            }
        });
        return v;
    }
}
