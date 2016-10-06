package com.xunix.ycej.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.avos.avoscloud.AVUser;
import com.facebook.drawee.view.SimpleDraweeView;
import com.xunix.ycej.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xunixhuang on 03/10/2016.
 */

public class FriendAdapterYoung extends RecyclerView.Adapter<FriendAdapterYoung.MyViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<AVUser> users;
    private List<String> remarks;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public List<AVUser> getUsers(){
        return users;
    }
    public List<String> getRemarks(){
        return remarks;
    }
    public FriendAdapterYoung(Context context, List<AVUser> users) {
        mContext = context;
        this.users = users;
        remarks=new ArrayList<>();
        for(int i=0;i<users.size();i++){
            remarks.add(users.get(i).getUsername());
        }
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.card_friend_young, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.username.setText(users.get(position).getUsername());
        String photoURI = (String) users.get(position).get("avatur");
        if (photoURI != null) {
            holder.simpleDraweeView.setImageURI(photoURI);
        }
        if(!users.get(position).getUsername().equals(remarks.get(position))){
            holder.remark.setText(remarks.get(position));
            holder.remark.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView username;
        TextView remark;
        CardView cardView;
        SimpleDraweeView simpleDraweeView;

        MyViewHolder(View itemView) {
            super(itemView);
            this.simpleDraweeView = (SimpleDraweeView) itemView.findViewById(R.id.portraitView);
            this.username = (TextView) itemView.findViewById(R.id.username);
            this.cardView = (CardView) itemView.findViewById(R.id.cardView);
            this.remark = (TextView) itemView.findViewById(R.id.remark);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onClick(itemView, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longClickListener != null) {
                longClickListener.onLongClick(itemView, getAdapterPosition());
            }
            return true;
        }
    }

    public void setRemarkName(int position,String name){
        remarks.set(position,name);
        super.notifyDataSetChanged();
    }

    public void onRefresh(List<AVUser> users) {
        this.users = users;
        remarks=new ArrayList<>();
        for(int i=0;i<users.size();i++){
            remarks.add(users.get(i).getUsername());
        }
        super.notifyDataSetChanged();
    }

    public void setClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLongClickListener(OnItemLongClickListener clickListener) {
        this.longClickListener = clickListener;
    }

    public interface OnItemLongClickListener {
        boolean onLongClick(View view, int position);
    }

    public interface OnItemClickListener {
        void onClick(View view, int position);
    }
}
