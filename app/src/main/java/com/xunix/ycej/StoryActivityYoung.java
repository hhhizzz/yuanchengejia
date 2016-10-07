package com.xunix.ycej;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.baoyz.widget.PullRefreshLayout;
import com.radaee.reader.R;
import com.xunix.ycej.adapter.FriendAdapter;
import com.xunix.ycej.adapter.StoryAdapter;
import com.xunix.ycej.message.AVIMStoryMessage;
import com.xunix.ycej.utils.FileSave;
import com.xunix.ycej.utils.StoryActivityMainYoung;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by xunixhuang on 08/10/2016.
 */

public class StoryActivityYoung extends AppCompatActivity {
    private RecyclerView storyList;
    private StoryAdapter adapter;
    private PullRefreshLayout layout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list_young);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        layout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        storyList = (RecyclerView) findViewById(R.id.storyList);
        LinearLayoutManager llm = new LinearLayoutManager(StoryActivityYoung.this);
        storyList.setLayoutManager(llm);
        adapter = new StoryAdapter(StoryActivityYoung.this);
        storyList.setAdapter(adapter);
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.onRefresh();
                        Toast.makeText(StoryActivityYoung.this, "文件刷新成功", Toast.LENGTH_SHORT).show();
                        layout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        adapter.setClickListener(new StoryAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(StoryActivityYoung.this, StoryActivityMainYoung.class);
                intent.putExtra("filepath", adapter.getFile(position).getAbsolutePath());
                startActivity(intent);
            }
        });
        adapter.setLongClickListener(new StoryAdapter.OnItemLongClickListener() {
            @Override
            public boolean onLongClick(View view, final int position) {
                Log.i("homeworkfragment", "longclick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(StoryActivityYoung.this);
                builder.setMessage("请选择对作业的操作")
                        .setPositiveButton("发送", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(StoryActivityYoung.this);
                                builder1.setTitle("请选择发送的好友");
                                String[] usernames = new String[FriendAdapter.getRemarks().size()];
                                for (int ii = 0; ii < FriendAdapter.getRemarks().size(); ii++) {
                                    usernames[ii] = FriendAdapter.getRemarks().get(ii);
                                }
                                builder1.setItems(usernames, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        sendStory(position, i);
                                    }
                                });
                                builder1.create().show();
                            }
                        })
                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (adapter.deleteFile(position)) {
                                    Toast.makeText(StoryActivityYoung.this, "删除成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(StoryActivityYoung.this, "删除失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                builder.create().show();
                return false;
            }
        });
    }
    private void sendStory(final int Position, int friendPosition) {
        final String username = FriendAdapter.getUsers().get(friendPosition).getUsername();
        final String myname = AVUser.getCurrentUser().getUsername();
        AVIMClient theClient = AVIMClient.getInstance(myname);
        theClient.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if (e == null) {
                    avimClient.createConversation(Arrays.asList(username, myname), username + "&" + myname, null, false, true, new AVIMConversationCreatedCallback() {
                        @Override
                        public void done(final AVIMConversation avimConversation, AVIMException e) {
                            if (e == null) {
                                try {
                                    File filePath = FileSave.getStoryFiles().get(Position);
                                    final File file=new FileSave().zipStory(filePath);
                                    final AVIMStoryMessage message = new AVIMStoryMessage(file);
                                    message.getAVFile().saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(AVException e) {
                                            message.setFileUrl(message.getAVFile().getUrl());
                                            message.setFileName(file.getName());
                                            avimConversation.sendMessage(message, new AVIMConversationCallback() {
                                                @Override
                                                public void done(AVIMException e) {
                                                    file.delete();
                                                    adapter.onRefresh();
                                                    Toast.makeText(StoryActivityYoung.this, "发送成功", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
