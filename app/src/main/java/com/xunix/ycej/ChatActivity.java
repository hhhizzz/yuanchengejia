package com.xunix.ycej;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.radaee.reader.R;
import com.xunix.ycej.adapter.MessageAdapter;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.xunix.ycej.utils.FileSave;
import com.xunix.ycej.utils.ImTypeMessageEvent;
import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuPopup;
import com.shehabic.droppy.animations.DroppyFadeInAnimation;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.xunix.ycej.message.AVIMPDFMessage;
import com.xunix.ycej.message.AVIMStoryMessage;
import de.greenrobot.event.EventBus;

import java.util.*;


/**
 * 聊天界面
 * @author Xunix Huang
 */
public class ChatActivity extends AppCompatActivity {
    private LinkedList<AVIMMessage> messages = null;
    private AVIMConversation theConversation;
    private AVIMClient theClient;

    /** message list */
    private ListView listView;
    /** EditText */
    private EmojiEditText edt;
    /** Send Button */
    private Button btnEnter;
    /** Face Button  */
    private ImageButton btnFace;
    /** emoji keyboard */
    private EmojiPopup emojiPopup;

    private MessageAdapter adapter;


    private String userID;
    private String username;
    private String myname;
    private String portraitURL;
    private String remarkName;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        theClient.close(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fresco.initialize(ChatActivity.this);
        Intent intent=getIntent();
        username=intent.getStringExtra("username");
        userID=intent.getStringExtra("id");
        portraitURL=intent.getStringExtra("portrait");
        remarkName=intent.getStringExtra("remark");

        myname= AVUser.getCurrentUser().getUsername();
        initConversation();
        super.onCreate(savedInstanceState);


        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setElevation(0);
        }
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_chat);
        setTitle(remarkName);
        initViewsMethod();
        onHandleMethod();
    }

    /**
     * deal with the listView
     * */
    private void initViewsMethod(){
        listView = (ListView) findViewById(R.id.lvMessages);
        edt = (EmojiEditText) findViewById(R.id.toolbox_et_message);
        btnEnter = (Button) findViewById(R.id.toolbox_btn_send);
        btnFace=(ImageButton) findViewById(R.id.toolbox_btn_face);


        //setting the emojiPopup
        ViewGroup rootView = (ViewGroup) findViewById(R.id.chat_main_activity);
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                btnFace.setBackgroundResource(R.drawable.icon_keyboard_nor);
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                btnFace.setBackgroundResource(R.drawable.icon_face_normal);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
            @Override
            public void onKeyboardClose() {
                emojiPopup.dismiss();
            }
        })
                .build(edt);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AVIMTypedMessage message=(AVIMTypedMessage)adapter.getItem(position);
                if(message.getMessageType()==1){
                    dialog(1,message);
                }
                if(message.getMessageType()==2){
                    dialog(2,message);
                }
            }
        });
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {

                menu.setHeaderTitle("注意");
                menu.setHeaderIcon(android.R.drawable.stat_notify_error);
                menu.add(0, 0, 1, "复制消息");
                menu.add(1, 1, 0, "取消");

            }
        });
        btnFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                emojiPopup.toggle();
            }
        });

    }
    @Override
    public void onBackPressed() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }


    /**
     * deal with the messages
     **/
    public void onHandleMethod(){
        messages=new LinkedList<>();
        adapter = new MessageAdapter(this,messages,portraitURL);
        listView.setAdapter(adapter);


        //sending messages
        btnEnter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String txt = edt.getText().toString();
                if(txt.equals(""))
                    Toast.makeText(getApplicationContext(), "消息为空", Toast.LENGTH_SHORT).show();
                else {
                    AVIMTextMessage msg = new AVIMTextMessage();
                    msg.setText(txt);
                    theConversation.sendMessage(msg, new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {
                            if (e == null) {
                                AVIMTextMessage message=new AVIMTextMessage();
                                Log.i("chatActivity","sendmentssage"+message.getFrom());
                                message.setText(txt);
                                message.setTimestamp(new Date().getTime());
                                adapter.sendMessage(message);
                                edt.setText("");
                                listView.setSelection(messages.size() - 1);    //jump to bottom
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:

                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                ClipboardManager cmb = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                try {
                    cmb.setText(((AVIMTextMessage) messages.get(info.position)).getText());
                }
                catch (ClassCastException e){
                    Toast.makeText(ChatActivity.this,"该消息无法复制到剪贴板", Toast.LENGTH_SHORT).show();
                    break;
                }
                Toast.makeText(ChatActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);

    }



    /**
     * 初始化conversation
     * */
    private void initConversation(){
        theClient= AVIMClient.getInstance(myname);
        theClient.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if(e==null){
                    avimClient.createConversation(Arrays.asList(username, myname), username + "&" + myname, null, false, true, new AVIMConversationCreatedCallback() {
                        @Override
                        public void done(AVIMConversation avimConversation, AVIMException e) {
                            if(e==null){
                                theConversation =avimConversation;
                                getChatHistory();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 获得聊天记录
     * 默认只获取30条
     * */
    private void getChatHistory(){
        int limit=30;
        theConversation.queryMessages(limit, new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> messages, AVIMException e) {
                if (e == null) {
                    for(int i=0;i<messages.size();i++){
                        adapter.sendMessage(messages.get(i));
                    }
                    listView.setSelection(messages.size() - 1);    //jump to bottom
                }
            }
        });
    }
    public void onEvent(ImTypeMessageEvent event) {
        adapter.sendMessage((event.message));
        Log.i("chatActivity","getMessage");
        listView.setSelection(messages.size()-1);
    }
    protected void dialog(int type, final AVIMTypedMessage message) {
        AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
        if(type==1){
            builder.setTitle("存储作业");
            builder.setTitle("请问要存储这份作业吗");
            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FileSave.saveHomework((AVIMPDFMessage)message);
                    Toast.makeText(ChatActivity.this,"存储中", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        if(type==2){
            builder.setTitle("存储故事");
            builder.setTitle("请问要存储这条故事吗");
            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FileSave.saveStory((AVIMStoryMessage)message);
                    Toast.makeText(ChatActivity.this,"存储中", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        builder.show();
    }

}
