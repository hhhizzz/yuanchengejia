package com.xunix.ycej.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avos.avospush.notification.NotificationCompat;
import com.handler.ImTypeMessageEvent;
import com.radaee.reader.R;
import com.xunix.ygej.MainActivity;
import de.greenrobot.event.EventBus;


/**
 * 用于接收消息并发送通知的服务
 * @author Xunix Huang
 * @version 0.160915
 */
public class MessageService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //通过eventbus来传递消息
    public void onEvent(ImTypeMessageEvent event) {
        Log.i("messageService","getmessage");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        AVIMTypedMessage typedMessage=event.message;
        switch (typedMessage.getMessageType()) {
            case -1:        //文本消息
                String text = ((AVIMTextMessage) typedMessage).getText();
                builder.setContentText(text);
                break;
            case -2:        //图片消息
                builder.setContentText("[图片]");
                break;
            case 1:        //PDF文件消息
                builder.setContentText("[作业]");
                break;
            case 2:         //故事文件消息
                builder.setContentText("[故事]");
        }
        builder.setSmallIcon(R.drawable.logo);
        builder.setContentTitle("新的消息 来自:"+typedMessage.getFrom());
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
