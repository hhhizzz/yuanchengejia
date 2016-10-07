package com.xunix.ycej.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.radaee.reader.R;
import com.xunix.ycej.utils.FileSave;

/**
 * Created by xunixhuang on 05/10/2016.
 */

public class HelpFragment extends Fragment {
    MapView mMapView = null;
    BaiduMap mBaiduMap = null;
    private MsgReceiver msgReceiver;
    private double longitude = 0;
    private double latitude = 0;
    private String poi;
    private float radius;
    private CardView helpButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, container, false);
        mMapView = (MapView) v.findViewById(R.id.bmapView);
        helpButton=(CardView)v.findViewById(R.id.helpCardView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("mapService");
        getActivity().registerReceiver(msgReceiver, intentFilter);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contact= FileSave.getContact();
                StringBuffer sb = new StringBuffer(256);
                sb.append("紧急求助:\n");
                sb.append("我的位置:\n");
                sb.append("经度:");
                sb.append(latitude);
                sb.append("\n纬度:");
                sb.append(longitude);
                sb.append("\n大致位置:");
                sb.append(poi);
                sendSMS(contact,sb.toString());
            }
        });
        return v;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        getActivity().unregisterReceiver(msgReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            latitude = intent.getDoubleExtra("latitude", 0);
            longitude = intent.getDoubleExtra("longitude", 0);
            poi = intent.getStringExtra("POI");
            radius = intent.getFloatExtra("radius", 0);
            Log.i("helpFragment",latitude+" "+longitude+" "+poi);
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(radius)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(latitude)
                    .longitude(longitude).build();
            // 设置定位数据
            if(mBaiduMap!=null) {
                mBaiduMap.setMyLocationData(locData);
                LatLng cenpt = new LatLng(latitude, longitude);
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder()
                        .zoom(17)
                        .target(cenpt)
                        .build();
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                mBaiduMap.setMapStatus(mMapStatusUpdate);
            }
        }
    }
    public void sendHelpMessage(){
        String contact=FileSave.getContact();
        StringBuffer sb = new StringBuffer(256);
        sb.append("紧急求助:\n");
        sb.append("我的位置:\n");
        sb.append("经度:");
        sb.append(latitude);
        sb.append("\n纬度:");
        sb.append(longitude);
        sb.append("\n大致位置:");
        sb.append(poi);
        sendSMS(contact,sb.toString());
    }
    private void sendSMS(String phoneNumber,String message){
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumber));
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }
}
