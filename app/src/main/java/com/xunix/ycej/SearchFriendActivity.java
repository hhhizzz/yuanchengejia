package com.xunix.ycej;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.avos.avoscloud.*;
import com.baoyz.widget.PullRefreshLayout;
import com.xunix.ycej.adapter.FriendAdapter;
import com.xunix.ycej.adapter.FriendAdapterOld;
import com.xunix.ycej.adapter.FriendAdapterYoung;

import java.util.List;

/**
 * Created by xunixhuang on 04/10/2016.
 */

public class SearchFriendActivity extends AppCompatActivity{
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private List<AVUser> resultList;
    private PullRefreshLayout refreshLayout;
    private int type=(int)AVUser.getCurrentUser().get("userType");
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int position = msg.what;
            String name = (String) msg.obj;
            switch (type) {
                case 0:
                    ((FriendAdapterYoung)recyclerView.getAdapter()).setRemarkName(position, name);
                    break;
                case 1:
                    ((FriendAdapter)recyclerView.getAdapter()).setRemarkName(position, name);
                    break;
                default:
                    ((FriendAdapterOld)recyclerView.getAdapter()).setRemarkName(position, name);
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        refreshLayout=(PullRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        recyclerView=(RecyclerView)findViewById(R.id.recyclerview);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        searchEditText=(EditText)findViewById(R.id.searchEditText);
        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onSearch();
            }
        });
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent event) {
                if (i == EditorInfo.IME_ACTION_SEND||i==EditorInfo.IME_ACTION_DONE ||event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction()) {
                    onSearch();
                }
                return false;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.searchFridend){
                    onSearch();
                }
                return false;
            }
        });
    }

    private void onSearch(){
        String username=searchEditText.getText().toString();
        if(username.equals("")){
            Toast.makeText(this,"请输入有效的字符",Toast.LENGTH_SHORT).show();
        }
        else{
            AVQuery<AVUser> userQuery = new AVQuery<>("_User");
            userQuery.whereContains("username",username);
            userQuery.findInBackground(new FindCallback<AVUser>() {
                @Override
                public void done(List<AVUser> list, AVException e) {
                    resultList=list;
                    switch (type){
                        case 0:
                            FriendAdapterYoung friendAdapter0=new FriendAdapterYoung(SearchFriendActivity.this,list);
                            friendAdapter0.setClickListener(new FriendAdapterYoung.OnItemClickListener() {
                                @Override
                                public void onClick(View view, int position) {
                                    addFriend(position);
                                }
                            });
                            friendAdapter0.setLongClickListener(new FriendAdapterYoung.OnItemLongClickListener() {
                                @Override
                                public boolean onLongClick(View view, int position) {
                                    remarkDialog(resultList.get(position).getUsername(),(String)resultList.get(position).get("avatur"));
                                    return false;
                                }
                            });
                            recyclerView.setAdapter(friendAdapter0);
                            break;
                        case 1:
                            FriendAdapter friendAdapter1=new FriendAdapter(SearchFriendActivity.this,list);
                            friendAdapter1.setClickListener(new FriendAdapter.OnItemClickListener() {
                                @Override
                                public void onClick(View view, int position) {
                                    addFriend(position);
                                }
                            });
                            friendAdapter1.setLongClickListener(new FriendAdapter.OnItemLongClickListener() {
                                @Override
                                public boolean onLongClick(View view, int position) {
                                    remarkDialog(resultList.get(position).getUsername(),(String)resultList.get(position).get("avatur"));
                                    return false;
                                }
                            });
                            recyclerView.setAdapter(friendAdapter1);
                            break;
                        default:
                            FriendAdapterOld friendAdapter2=new FriendAdapterOld(SearchFriendActivity.this,list);
                            friendAdapter2.setClickListener(new FriendAdapterOld.OnItemClickListener() {
                                @Override
                                public void onClick(View view, int position) {
                                    addFriend(position);
                                }
                            });
                            friendAdapter2.setLongClickListener(new FriendAdapterOld.OnItemLongClickListener() {
                                @Override
                                public boolean onLongClick(View view, int position) {
                                    remarkDialog(resultList.get(position).getUsername(),(String)resultList.get(position).get("avatur"));
                                    return false;
                                }
                            });
                            recyclerView.setAdapter(friendAdapter2);
                    }
                    LinearLayoutManager llm = new LinearLayoutManager(SearchFriendActivity.this);
                    recyclerView.setLayoutManager(llm);
                    for(int i=0;i<resultList.size();i++){
                        setRemark(i);
                    }
                }
            });
        }
        refreshLayout.setRefreshing(false);
    }
    private void setRemark(final int posttion) {
        AVQuery<AVObject> query = new AVQuery<>("remark");
        query.whereEqualTo("remarkUser", AVUser.getCurrentUser().getUsername());
        query.whereEqualTo("nameUser", resultList.get(posttion).getUsername());
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
    private void remarkDialog(String name, String URI) {
        FragmentTransaction mFragTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = getFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            mFragTransaction.remove(fragment);
        }
        MainActivity.RemarkDialogFragment remarkDialogFragment = MainActivity.RemarkDialogFragment.newString(name, URI);
        remarkDialogFragment.show(mFragTransaction, "dialog");
    }
    private void addFriend(final int position){
        AlertDialog.Builder builder=new AlertDialog.Builder(SearchFriendActivity.this);
        builder.setMessage("是否要添加好友");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AVUser.getCurrentUser().followInBackground(resultList.get(position).getObjectId(), new FollowCallback() {
                    @Override
                    public void done(AVObject object, AVException e) {
                        if (e == null) {
                            Toast.makeText(SearchFriendActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                            AVQuery pushQuery = AVInstallation.getQuery();
                            pushQuery.whereEqualTo("channels", resultList.get(position).getUsername());
                            AVPush push = new AVPush();
                            push.setQuery(pushQuery);
                            push.setMessage(AVUser.getCurrentUser().getUsername()+"已添加你为好友");
                            push.setPushToAndroid(true);
                            push.sendInBackground(new SendCallback() {
                                @Override
                                public void done(AVException e) {
                                    Log.i("FriendRequest","AcceptSuccessed");
                                }
                            });
                        } else if (e.getCode() == AVException.DUPLICATE_VALUE) {
                            Toast.makeText(SearchFriendActivity.this, "已经添加过了", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }
}
