package com.xunix.ycej.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.xunix.ycej.R;
import com.xunix.ycej.utils.DateFomats;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * 消息列表Adapter
 *
 * @author Xunix Huang
 */
public class MessageAdapter extends BaseAdapter {
    private LinkedList<AVIMMessage> messages;
    private LayoutInflater li;
    public final int ITEM_TYPES = 6, MyText=4,YourText=5,MyMessage = 1, YourMessage = 0, MyImage = 2, YourImage = 3;
    private String myName = null;
    private String MyportraitURI;
    private String YourportraitURI;


    public MessageAdapter(Context context, LinkedList<AVIMMessage> list, String PortraitURL) {
        messages = list;
        li = LayoutInflater.from(context);
        myName = AVUser.getCurrentUser().getUsername();
        MyportraitURI=((String) AVUser.getCurrentUser().get("avatur"));
        YourportraitURI=PortraitURL;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_TYPES;
    }

    @Override
    public int getItemViewType(int position) {
        AVIMMessage theMessage = messages.get(position);
        if (theMessage.getFrom() == null || messages.get(position).getFrom().equals(myName)) {
            if (((AVIMTypedMessage) theMessage).getMessageType() == -2) {
                return MyImage;
            } else if(((AVIMTypedMessage)theMessage).getMessageType()==-1) {
                return MyText;
            }
            else{
                return MyMessage;
            }
        } else {
            if (((AVIMTypedMessage) theMessage).getMessageType() == -2) {
                return YourImage;
            } else if(((AVIMTypedMessage)theMessage).getMessageType()==-1){
                return YourText;
            }
            else{
                return YourMessage;
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        AVIMMessage theMessage = messages.get(position);
        int type = getItemViewType(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            switch (type) {
                case MyMessage:
                    convertView = li.inflate(R.layout.listview_item_right_layout, parent, false);
                    viewHolder.image = (ImageView) convertView.findViewById(R.id.Image);
                    viewHolder.message = (TextView) convertView.findViewById(R.id.Msg);
                    viewHolder.portrait=(SimpleDraweeView) convertView.findViewById(R.id.portraitView);
                    viewHolder.portrait.setImageURI(MyportraitURI);
                    break;
                case YourMessage:
                    convertView = li.inflate(R.layout.listview_item_left_layout, parent, false);
                    viewHolder.image = (ImageView) convertView.findViewById(R.id.Image);
                    viewHolder.message = (TextView) convertView.findViewById(R.id.Msg);
                    viewHolder.portrait=(SimpleDraweeView) convertView.findViewById(R.id.portraitView);
                    viewHolder.portrait.setImageURI(YourportraitURI);
                    break;
                case MyText:
                    convertView=li.inflate(R.layout.listview_right_text,parent,false);
                    viewHolder.message=(TextView)convertView.findViewById(R.id.Msg);
                    viewHolder.portrait=(SimpleDraweeView) convertView.findViewById(R.id.portraitView);
                    viewHolder.portrait.setImageURI(MyportraitURI);
                    break;
                case YourText:
                    convertView=li.inflate(R.layout.listview_left_text,parent,false);
                    viewHolder.message=(TextView)convertView.findViewById(R.id.Msg);
                    viewHolder.portrait=(SimpleDraweeView) convertView.findViewById(R.id.portraitView);
                    viewHolder.portrait.setImageURI(YourportraitURI);
                    break;
                default:
                    break;
            }
            viewHolder.time = (TextView) convertView.findViewById(R.id.Time);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder=(ViewHolder)convertView.getTag();
        }

        if (position == 0) {   //if time is close to last one ,hide it
            viewHolder.time.setText(DateFomats.getStringTime(new Date(theMessage.getTimestamp())));
        } else if (theMessage.getTimestamp() - messages.get(position - 1).getTimestamp() >= 30000) {
            viewHolder.time.setText(DateFomats.getStringTime(new Date(theMessage.getTimestamp())));
        } else {
            viewHolder.time.setTextSize(0);
        }

        AVIMTypedMessage typedMessage = (AVIMTypedMessage) theMessage;
        switch (typedMessage.getMessageType()) {
            case -1:        //文本消息
                String text = ((AVIMTextMessage) typedMessage).getText();
                viewHolder.message.setText(text);
                break;
            case 1:        //PDF文件消息
                viewHolder.image.setImageResource(R.drawable.ic_note_black_48dp);
                viewHolder.message.setTextColor(Color.parseColor("#FF000000"));
                viewHolder.message.setText("[作业]");
                break;
            case 2:         //故事文件消息
                viewHolder.image.setImageResource(R.drawable.ic_library_music_black_48dp);
                viewHolder.message.setTextColor(Color.parseColor("#FF000000"));
                viewHolder.message.setText("[故事]");
        }

        return convertView;

    }

    public static class ViewHolder {
        public TextView time;
        public TextView message;
        public ImageView image;
        public SimpleDraweeView portrait;
    }

    public void sendMessage(AVIMMessage message) {
        messages.add(message);
        notifyDataSetChanged();
    }
    public void viewHistory(List<AVIMMessage> messageList){
        for(int i=0;i<messageList.size();i++){
            messages.add(0,messageList.get(messageList.size()-i-1));
        }
        notifyDataSetChanged();
    }
}
