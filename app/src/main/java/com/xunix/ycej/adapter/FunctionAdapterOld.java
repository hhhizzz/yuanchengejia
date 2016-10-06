package com.xunix.ycej.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.radaee.reader.R;

/**
 * Created by xunixhuang on 04/10/2016.
 */

public class FunctionAdapterOld extends RecyclerView.Adapter<FunctionAdapterOld.MyViewHolder> {
    private final LayoutInflater mLayoutInflater;
    private Context context;
    public FunctionAdapterOld(Context context){
        this.context=context;
        mLayoutInflater=LayoutInflater.from(context);
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=mLayoutInflater.inflate(R.layout.card_function_old,null);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        switch (position){
            case 0:
                holder.functionName.setText("健康");
                holder.functionImg.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite_border_white_48dp));
                holder.cardView.setCardBackgroundColor(Color.parseColor("#4FC3F7"));
                break;
            case 1:
                holder.functionName.setText("求救");
                holder.functionImg.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_report_problem_yellow_48dp));
                holder.cardView.setCardBackgroundColor(Color.parseColor("#81C784"));
                break;
            case 2:
                holder.functionName.setText("足迹分析");
                holder.functionImg.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_public_white_48dp));
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FF8A65"));
                break;
        }
    }


    @Override
    public int getItemCount() {
        return 3;
    }
    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView functionName;
        SimpleDraweeView functionImg;
        CardView cardView;

        MyViewHolder(View view) {
            super(view);
            this.cardView=(CardView)view.findViewById(R.id.cardFunction);
            this.functionName=(TextView)view.findViewById(R.id.functionName);
            this.functionImg=(SimpleDraweeView)view.findViewById(R.id.functionImg);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (getAdapterPosition()){
                        case 0:
                            Toast.makeText(context,"健康",Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(context,"求救",Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(context,"足迹分析",Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }


    }
}
