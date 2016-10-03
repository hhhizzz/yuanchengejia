package com.xunix.ycej;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.avos.avoscloud.*;
import com.avos.avoscloud.callback.AVFriendshipCallback;
import com.baoyz.widget.PullRefreshLayout;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xunix.ycej.adapter.FriendAdapter;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Drawer drawer = null;
    private RecyclerView friendRecyclerView = null;
    private List<AVUser> followees;     //好友列表mObjects = new ArrayList();
    private FriendAdapter friendAdapter;
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

        setContentView(R.layout.activity_main);
        initDrawer();
        initFriend();
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
                        Toast.makeText(MainActivity.this, "好友列表更新成功", Toast.LENGTH_SHORT).show();
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
                friendAdapter = new FriendAdapter(MainActivity.this, followees);
                friendRecyclerView.setAdapter(friendAdapter);
                LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
                friendRecyclerView.setLayoutManager(llm);
                friendAdapter.setLongClickListener(new FriendAdapter.OnItemLongClickListener() {
                    @Override
                    public boolean onLongClick(View view, int position) {
                        remarkDialog(followees.get(position).getUsername(), (String) followees.get(position).get("avatur"));
                        return true;
                    }
                });
                friendAdapter.setClickListener(new FriendAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        String username = followees.get(position).getUsername();
                        String id = followees.get(position).getObjectId();
                        String portraitURL = (String) followees.get(position).get("avatur");
                        String remark=friendAdapter.getRemarks().get(position);
                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        intent.putExtra("remark",remark);
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

    private void initDrawer() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        new DrawerBuilder().withActivity(this).build();
        String portrait = (String) AVUser.getCurrentUser().get("avatur");
        Log.i("MainActivity", portrait);
        final IProfile profile = new ProfileDrawerItem().withName(AVUser.getCurrentUser().getUsername()).withIcon(portrait).withIdentifier(100);
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.background)
                .addProfiles(
                        profile,
                        new ProfileSettingDrawerItem().withName("注销").withDescription("退出当前账户").withIcon(R.drawable.ic_speaker_notes_off_black_48dp),
                        new ProfileSettingDrawerItem().withName("账户设置").withIcon(R.drawable.ic_settings_black_48dp)
                )
                .build();
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withHasStableIds(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("互动").withDescription("与家人一起互动").withIcon(R.drawable.ic_accessibility_black_48dp).withIdentifier(1).withSelectable(false).withTextColor(Color.BLACK),
                        new PrimaryDrawerItem().withName("求救").withDescription("设定紧急求救信息").withIcon(R.drawable.ic_report_problem_black_48dp).withIdentifier(2).withSelectable(false).withTextColor(Color.BLACK),
                        new PrimaryDrawerItem().withName("交流圈").withDescription("分享自己的最新状态").withIcon(R.drawable.ic_people_black_48dp).withIdentifier(3).withSelectable(false).withTextColor(Color.BLACK),
                        new PrimaryDrawerItem().withName("足迹分析").withDescription("分析家人的位置状态").withIcon(R.drawable.ic_public_black_48dp).withIdentifier(4).withSelectable(false).withTextColor(Color.BLACK)
                )
                .withShowDrawerOnFirstLaunch(true)
                .build();

    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
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

    private void remarkDialog(String name, String URI) {
        FragmentTransaction mFragTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = getFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            mFragTransaction.remove(fragment);
        }
        RemarkDialogFragment remarkDialogFragment = RemarkDialogFragment.newString(name, URI);
        remarkDialogFragment.show(mFragTransaction, "dialog");
    }


    /**
     * 一个内部类,用于修改备注的Dialog
     */
    public static class RemarkDialogFragment extends DialogFragment {
        private MaterialEditText materialEditText;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            final String username = getArguments().getString("name");
            String URI = getArguments().getString("URI");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_remark, null);
            SimpleDraweeView simpleDraweeView = (SimpleDraweeView) view.findViewById(R.id.portraitView);
            materialEditText = (MaterialEditText) view.findViewById(R.id.remarkEditText);
            if (URI != null) {
                simpleDraweeView.setImageURI(URI);
            }

            builder.setView(view)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            if (materialEditText.getText().toString() == "") {
                                Toast.makeText(activity, "备注不能为空", Toast.LENGTH_SHORT);
                            } else {
                                AVQuery<AVObject> query = new AVQuery<>("remark");
                                query.whereEqualTo("remarkUser", AVUser.getCurrentUser().getUsername());
                                query.whereEqualTo("nameUser", username);
                                query.findInBackground(new FindCallback<AVObject>() {
                                    @Override
                                    public void done(List<AVObject> list, AVException e) {
                                        if (list.size() > 0 && e == null) {
                                            String objectId = list.get(0).getObjectId();
                                            AVObject object = AVObject.createWithoutData("remark", objectId);
                                            object.put("remarkName", materialEditText.getText().toString());
                                            object.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(AVException e) {
                                                    if (e == null) {
                                                        Toast.makeText(activity, "备注修改成功,请下拉刷新", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(activity, "备注修改失败,请检查网络", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        else{
                                            AVObject object = new AVObject("remark");
                                            object.put("remarkUser", AVUser.getCurrentUser().getUsername());
                                            object.put("nameUser", username);
                                            object.put("remarkName", materialEditText.getText().toString());
                                            object.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(AVException e) {
                                                    if (e == null) {
                                                        Toast.makeText(activity, "备注修改成功,请下拉刷新", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(activity, "备注修改失败,请检查网络", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            RemarkDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

        public static RemarkDialogFragment newString(String name, String URI) {
            RemarkDialogFragment f = new RemarkDialogFragment();
            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putString("name", name);
            args.putString("URI", URI);
            f.setArguments(args);
            return f;
        }
    }
}
