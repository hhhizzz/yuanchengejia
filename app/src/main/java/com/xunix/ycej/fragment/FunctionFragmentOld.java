package com.xunix.ycej.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.radaee.reader.R;
import com.xunix.ycej.HeartActivity;
import com.xunix.ycej.HelpActivity;
import com.xunix.ycej.PathActivity;
import com.xunix.ycej.adapter.FriendAdapter;
import com.xunix.ycej.adapter.FunctionAdapterOld;

public class FunctionFragmentOld extends Fragment {
	private RecyclerView functionList;
	private FunctionAdapterOld adapterOld;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_function_old, container, false);
		functionList=(RecyclerView)v.findViewById(R.id.functionList);
		functionList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
		adapterOld=new FunctionAdapterOld(getActivity());
		functionList.setAdapter(adapterOld);
		adapterOld.setClickListener(new FunctionAdapterOld.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                switch (position){
                    case 0:
                        startActivity(new Intent(getActivity(), HeartActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(getActivity(), HelpActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(getActivity(), PathActivity.class));
                }
            }
        });
		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

}
