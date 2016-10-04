package com.xunix.ycej;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.avos.avoscloud.*;
import com.avos.avoscloud.callback.AVFriendshipCallback;
import com.baoyz.widget.PullRefreshLayout;
import com.facebook.drawee.view.SimpleDraweeView;
import com.rey.material.widget.Button;
import com.xunix.ycej.adapter.FriendAdapterYoung;

import java.util.List;

/**
 * Created by xunixhuang on 04/10/2016.
 */

public class MainActivityYoung extends AppCompatActivity implements View.OnClickListener{
    private List<AVUser> followees;
    private FriendAdapterYoung friendAdapter;
    private RecyclerView friendRecyclerView = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int position = msg.what;
            String name = (String) msg.obj;
            friendAdapter.setRemarkName(position, name);
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_young);
        initView();
        initFriend();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String portraitURI = (String) AVUser.getCurrentUser().get("avatur");
        Button helpButton = (Button) findViewById(R.id.helpButton);
        Button homeworkButton = (Button) findViewById(R.id.homeworkButton);
        TextView username=(TextView)findViewById(R.id.username);
        Button storyButoon = (Button) findViewById(R.id.storyButton);
        Button mapButton = (Button) findViewById(R.id.mapButton);
        friendRecyclerView=(RecyclerView)findViewById(R.id.friendList);
        SimpleDraweeView portraitView=(SimpleDraweeView)findViewById(R.id.portraitView);
        PullRefreshLayout refreshLayout=(PullRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        username.setText(AVUser.getCurrentUser().getUsername());
        portraitView.setImageURI(portraitURI);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.searchFridend:
                        Toast.makeText(MainActivityYoung.this, "搜索好友!", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.logout:
                        AVUser.logOut();
                        startActivity(new Intent(MainActivityYoung.this,LoginActivity.class));
                        finish();
                        break;
                    case R.id.setting:
                        Toast.makeText(MainActivityYoung.this, "账号设置", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }
    private void initFriend() {
        final PullRefreshLayout layout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AVFriendshipQuery query = AVUser.friendshipQuery(AVUser.getCurrentUser().getObjectId(), AVUser.class);
                query.include("followee");
                query.getInBackground(new AVFriendshipCallback() {
                    @Override
                    public void done(AVFriendship friendship, AVException e) {
                        followees = friendship.getFollowees(); //获取关注列表
                        friendAdapter.onRefresh(followees);
                        layout.setRefreshing(false);
                        for (int i = 0; i < followees.size(); i++) {
                            setRemark(i);
                        }
                        Toast.makeText(MainActivityYoung.this, "好友列表更新成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        friendRecyclerView = (RecyclerView) findViewById(R.id.friendList);
        AVFriendshipQuery query = AVUser.friendshipQuery(AVUser.getCurrentUser().getObjectId(), AVUser.class);
        query.include("followee");
        query.getInBackground(new AVFriendshipCallback() {
            @Override
            public void done(AVFriendship friendship, AVException e) {
                followees = friendship.getFollowees(); //获取关注列表
                friendAdapter = new FriendAdapterYoung(MainActivityYoung.this, followees);
                friendRecyclerView.setAdapter(friendAdapter);
                LinearLayoutManager llm = new LinearLayoutManager(MainActivityYoung.this);
                friendRecyclerView.setLayoutManager(llm);
                friendAdapter.setLongClickListener(new FriendAdapterYoung.OnItemLongClickListener() {
                    @Override
                    public boolean onLongClick(View view, int position) {
                        remarkDialog(followees.get(position).getUsername(), (String) followees.get(position).get("avatur"));
                        return true;
                    }
                });
                friendAdapter.setClickListener(new FriendAdapterYoung.OnItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        String username = followees.get(position).getUsername();
                        String id = followees.get(position).getObjectId();
                        String portraitURL = (String) followees.get(position).get("avatur");
                        String remark = friendAdapter.getRemarks().get(position);
                        Intent intent = new Intent(MainActivityYoung.this, ChatActivity.class);
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
        });
    }
    /*
   用于获得的备注信息,使用的多线程
 */
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
        FragmentTransaction mFragTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = getFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            mFragTransaction.remove(fragment);
        }
        MainActivity.RemarkDialogFragment remarkDialogFragment = MainActivity.RemarkDialogFragment.newString(name, URI);
        remarkDialogFragment.show(mFragTransaction, "dialog");
    }
    @Override
    public void onClick(View view) {

    }
}
