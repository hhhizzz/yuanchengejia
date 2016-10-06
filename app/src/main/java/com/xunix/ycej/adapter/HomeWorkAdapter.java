package com.xunix.ycej.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.radaee.reader.R;
import com.xunix.ycej.utils.FileSave;

import java.io.File;
import java.util.List;

/**
 * Created by xunixhuang on 06/10/2016.
 */

public class HomeWorkAdapter extends RecyclerView.Adapter<HomeWorkAdapter.MyViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<File> fileList;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public HomeWorkAdapter(Context context) {
        mContext = context;
        fileList = FileSave.getHomeworkFiles();
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.card_homework, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.homeworkName.setText(fileList.get(position).getName());
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView homeworkName;
        CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.homeworkName=(TextView)itemView.findViewById(R.id.homeworkTextview);
            this.cardView=(CardView)itemView.findViewById(R.id.cardView);
            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
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
