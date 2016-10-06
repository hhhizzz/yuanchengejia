package com.xunix.ycej;

import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.avos.avoscloud.AVUser;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.sevenheaven.iosswitch.ShSwitchView;
import com.xunix.ycej.service.MapService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xunixhuang on 05/10/2016.
 */

public class PathActivity extends AppCompatActivity {
    private int need;
    private Toolbar toolbar;
    private MapService.ServiceBinder binder;
    private ShSwitchView shSwitchView;
    private MapView mapView;
    private BaiduMap mBaiduMap;
    private MsgReceiver msgReceiver;
    private Marker marker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);
        initView();
        mBaiduMap.setMyLocationEnabled(true);
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("mapService");
        registerReceiver(msgReceiver, intentFilter);
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mapView = (MapView) findViewById(R.id.mapView);
        mBaiduMap = mapView.getMap();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        shSwitchView = (ShSwitchView) findViewById(R.id.switch_view);
        Intent intent = new Intent(PathActivity.this, MapService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        shSwitchView.setOnSwitchStateChangeListener(new ShSwitchView.OnSwitchStateChangeListener() {
            @Override
            public void onSwitchStateChange(boolean isOn) {
                if (isOn) {
                    binder.setNeed(1);
                } else {
                    binder.setNeed(-1);
                }
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(marker!=null) {
                    marker.remove();
                }
                switch (item.getItemId()) {
                    case R.id.findPathWeek:
                        Log.i("PathActivity", "findpathweek");
                        TaskWeek task = new TaskWeek();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, AVUser.getCurrentUser().getUsername());
                        break;
                    case R.id.findPathMonth:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (MapService.ServiceBinder) iBinder;
            need = binder.getNeed();
            if (need == 1) {
                shSwitchView.setOn(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            float radius = intent.getFloatExtra("radius", 0);
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(radius)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(latitude)
                    .longitude(longitude).build();
            // 设置定位数据
            if (mBaiduMap != null) {
                mBaiduMap.setMyLocationData(locData);
                LatLng cenpt = new LatLng(latitude, longitude);
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder()
                        .zoom(17)
                        .target(cenpt)
                        .build();
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                unregisterReceiver(msgReceiver);
            }
        }
    }

    class TaskWeek extends AsyncTask<String, Integer, List<MyLocation>> {
        @Override
        protected List<MyLocation> doInBackground(String... usernames) {
            List<MyLocation> location=new ArrayList<>();
            Log.i("taskweek", "start");
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://xunixapp.com/gps");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST"); // 可以根据需要 提交 GET、POST、DELETE、INPUT等http提供的功能
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("Content-Type", "application/json");  //设定 请求格式 json，也可以设定xml格式的
                connection.setRequestProperty("Accept", "application/json");//设定响应的信息的格式为 json，也可以设定xml格式的
                connection.connect();
                JSONObject object = new JSONObject();
                object.put("username", usernames[0]);
                object.put("longitude", 1);
                object.put("latitude", 2);
                object.put("week", 1);
                object.put("need", 0);
                object.put("t2", System.currentTimeMillis());
                long t1 = System.currentTimeMillis() - 7 * 24 * 3600 * 1000;
                object.put("t1", t1);
                OutputStream out = connection.getOutputStream();
                out.write(object.toString().getBytes());
                out.flush();
                out.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String lines;
                StringBuffer sb = new StringBuffer("");
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes(), "utf-8");
                    sb.append(lines);
                }
                Log.i("pathActivity", new String(sb));
                reader.close();
                connection.disconnect();
                JSONTokener jsonTokener = new JSONTokener(new String(sb));
                JSONObject jsonObject1 = new JSONObject(jsonTokener);
                String back = (String) jsonObject1.get("content");
                if (back.equals("success")) {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestMethod("POST"); // 可以根据需要 提交 GET、POST、DELETE、INPUT等http提供的功能
                    connection.setUseCaches(false);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestProperty("Content-Type", "application/json");  //设定 请求格式 json，也可以设定xml格式的
                    connection.setRequestProperty("Accept", "application/json");//设定响应的信息的格式为 json，也可以设定xml格式的
                    connection.connect();
                    object = new JSONObject();
                    object.put("username", usernames[0]);
                    object.put("longitude", 1);
                    object.put("latitude", 2);
                    object.put("week", 1);
                    object.put("need", 2);
                    object.put("t2", System.currentTimeMillis());
                    object.put("t1", t1);
                    out = connection.getOutputStream();
                    out.write(object.toString().getBytes());
                    out.flush();
                    out.close();
                    reader = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));
                    sb = new StringBuffer("");
                    while ((lines = reader.readLine()) != null) {
                        lines = new String(lines.getBytes(), "utf-8");
                        sb.append(lines);
                    }
                    reader.close();
                    connection.disconnect();
                    Log.i("FindTask",new String(sb));
                    JSONTokener tokener = new JSONTokener(new String(sb));
                    JSONObject object1 = new JSONObject(tokener);
                    JSONArray array=object1.getJSONArray("content");
                    for(int i=0;i<array.length();i++) {
                        JSONObject object2 = array.getJSONObject(i);
                        MyLocation location1=new MyLocation();
                        location1.longitude=object2.getDouble("Longtiude");
                        location1.latitude=object2.getDouble("Latitude");
                        location.add(location1);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return location;
        }

        @Override
        protected void onPostExecute(List<MyLocation> l) {

            super.onPostExecute(l);
            for(int i=0;i<l.size();i++) {
                LatLng point = new LatLng(l.get(i).latitude, l.get(i).longitude);
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_location_on_black_24dp);
                OverlayOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap);
                marker = (Marker) (mBaiduMap.addOverlay(option));
            }
        }

    }

    class MyLocation {
        double longitude;
        double latitude;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.path_menu, menu);
        return true;
    }
}
