package com.xunix.ycej.utils;

import android.util.Log;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.*;
import de.greenrobot.event.EventBus;

/**
 * Created by xunixhuang on 4/28/16.
 */
public class CustomMessageHandler extends AVIMTypedMessageHandler<AVIMTypedMessage> {


    //接收到消息后的处理逻辑
    @Override
    public void onMessage(AVIMTypedMessage message, AVIMConversation conversation, AVIMClient client) {
        Log.i("onMessage",message.getMessageId());
        if(message.getFrom().equals(AVUser.getCurrentUser().getUsername())){
            return;
        }
        sendEvent(message, conversation);
        super.onMessage(message, conversation, client);
    }

    /**
     * 因为没有 db，所以暂时先把消息广播出去，由接收方自己处理
     * 稍后应该加入 db
     * @param message
     * @param conversation
     */
    private void sendEvent(AVIMMessage message, AVIMConversation conversation) {
        ImTypeMessageEvent event = new ImTypeMessageEvent();
        event.message = (AVIMTypedMessage) message;
        event.conversation = conversation;
        EventBus.getDefault().post(event);
    }
}
