package com.xunix.ycej.fragment;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.avos.avoscloud.*;
import com.avos.avoscloud.callback.AVFriendshipCallback;
import com.baoyz.widget.PullRefreshLayout;
import com.xunix.ycej.ChatActivity;
import com.xunix.ycej.MainActivity;
import com.xunix.ycej.MainActivityOld;
import com.radaee.reader.R;
import com.xunix.ycej.adapter.FriendAdapter;
import com.xunix.ycej.adapter.FriendAdapterOld;

import java.util.List;

/**
 * Created by xunixhuang on 04/10/2016.
 */

public class FriendFragmentOld extends Fragment {
    private RecyclerView friendRecyclerView = null;
    private List<AVUser> followees;
    private FriendAdapterOld friendAdapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int position = msg.what;
            String name = (String) msg.obj;
            friendAdapter.setRemarkName(position, name);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friend_old, container, false);
        initFriend(v);
        return v;
    }

    private void initFriend(View v) {
        final PullRefreshLayout layout = (PullRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AVQuery<AVUser> query = AVUser.followeeQuery(AVUser.getCurrentUser().getObjectId(), AVUser.class);
                query.include("followee");
                query.findInBackground(new FindCallback<AVUser>() {
                    @Override
                    public void done(List<AVUser> avObjects, AVException e) {
                        if (e == null) {
                            followees = avObjects;
                            friendAdapter.onRefresh(followees);
                            layout.setRefreshing(false);
                            for (int i = 0; i < followees.size(); i++) {
                                setRemark(i);
                            }
                            Toast.makeText(getActivity() ,"好友列表更新成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        friendRecyclerView = (RecyclerView) v.findViewById(R.id.friendList);
        AVQuery<AVUser> query = AVUser.followeeQuery(AVUser.getCurrentUser().getObjectId(), AVUser.class);
        query.include("followee");
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avUsers, AVException e) {
                if (e == null) {
                    followees = avUsers;
                    friendAdapter = new FriendAdapterOld(getActivity(), followees);
                    friendRecyclerView.setAdapter(friendAdapter);
                    LinearLayoutManager llm = new LinearLayoutManager(getActivity());
                    friendRecyclerView.setLayoutManager(llm);
                    friendAdapter.setLongClickListener(new FriendAdapterOld.OnItemLongClickListener() {
                        @Override
                        public boolean onLongClick(View view, int position) {
                            remarkDialog(followees.get(position).getUsername(), (String) followees.get(position).get("avatur"));
                            return true;
                        }
                    });
                    friendAdapter.setClickListener(new FriendAdapterOld.OnItemClickListener() {
                        @Override
                        public void onClick(View view, int position) {
                            String username = followees.get(position).getUsername();
                            String id = followees.get(position).getObjectId();
                            String portraitURL = (String) followees.get(position).get("avatur");
                            String remark = friendAdapter.getRemarks().get(position);
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("remark", remark);
                            intent.putExtra("username", username);
                            intent.putExtra("id", id);
                            intent.putExtra("portrait", portraitURL);
                            startActivity(intent);
                        }
                    });
                    for (int i = 0; i < followees.size(); i++) {
                        setRemark(i);
                    }
                }
            }
        });
    }

    private void setRemark(final int posttion) {
        AVQuery<AVObject> query = new AVQuery<>("remark");
        query.whereEqualTo("remarkUser", AVUser.getCurrentUser().getUsername());
        query.whereEqualTo("nameUser", followees.get(posttion).getUsername());
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (list.size() > 0 && e == null) {
                    Message message = new Message();
                    message.what = posttion;
                    message.obj = (list.get(0).get("remarkName"));
                    handler.sendMessage(message);
                }
            }
        });
    }

    /**
     * 用于弹出备注框
     */
    private void remarkDialog(String name, String URI) {
        FragmentTransaction mFragTransaction = getActivity().getFragmentManager().beginTransaction();
        android.app.Fragment fragment = getActivity().getFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            mFragTransaction.remove(fragment);
        }
        MainActivity.RemarkDialogFragment remarkDialogFragment = MainActivity.RemarkDialogFragment.newString(name, URI);
        remarkDialogFragment.show(mFragTransaction, "dialog");
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
