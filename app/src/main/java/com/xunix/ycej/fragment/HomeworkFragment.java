package com.xunix.ycej.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
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
import com.xunix.ycej.MyPDFReader;
import com.xunix.ycej.adapter.FriendAdapter;
import com.xunix.ycej.adapter.HomeWorkAdapter;
import com.xunix.ycej.message.AVIMPDFMessage;
import com.xunix.ycej.utils.FileSave;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by xunixhuang on 06/10/2016.
 */

public class HomeworkFragment extends Fragment {
    private RecyclerView homworkList;
    private HomeWorkAdapter adapter;
    private PullRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_homework, container, false);
        homworkList = (RecyclerView) v.findViewById(R.id.homeworkList);
        refreshLayout = (PullRefreshLayout) v.findViewById(R.id.swipeRefreshLayout);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        homworkList.setLayoutManager(llm);
        adapter = new HomeWorkAdapter(getActivity());
        homworkList.setAdapter(adapter);
        adapter.setClickListener(new HomeWorkAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), MyPDFReader.class);
                intent.putExtra("filepath", FileSave.getHomeworkFiles().get(position).getAbsolutePath());
                startActivity(intent);
            }
        });
        adapter.setLongClickListener(new HomeWorkAdapter.OnItemLongClickListener() {
            @Override
            public boolean onLongClick(View view, final int position) {
                Log.i("homeworkfragment", "longclick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("请选择对作业的操作")
                        .setPositiveButton("发送", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                                builder1.setTitle("请选择发送的好友");
                                String[] usernames = new String[FriendAdapter.getRemarks().size()];
                                for (int ii = 0; ii < FriendAdapter.getRemarks().size(); ii++) {
                                    usernames[ii] = FriendAdapter.getRemarks().get(ii);
                                }
                                builder1.setItems(usernames, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        sendHomework(position, i);
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
                                    Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                builder.create().show();
                return false;
            }
        });
        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.onRefresh();
                        Toast.makeText(getActivity(), "文件刷新成功", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        return v;
    }


    private void sendHomework(final int homeworkPosition, int friendPosition) {
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
                                    final File file = FileSave.getHomeworkFiles().get(homeworkPosition);
                                    final AVIMPDFMessage message = new AVIMPDFMessage(file);
                                    message.getAVFile().saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(AVException e) {
                                            message.setFileUrl(message.getAVFile().getUrl());
                                            message.setFileName(file.getName());
                                            avimConversation.sendMessage(message, new AVIMConversationCallback() {
                                                @Override
                                                public void done(AVIMException e) {
                                                    Toast.makeText(getActivity(), "发送成功", Toast.LENGTH_SHORT).show();
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
