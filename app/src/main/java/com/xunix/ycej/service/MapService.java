package com.xunix.ycej.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.avos.avoscloud.AVUser;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.xunix.ycej.MainActivity;
import com.xunix.ycej.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by xunixhuang on 13/09/2016.
 */
public class MapService extends Service {
    private NotificationManager mNM;
    private String username;
    private double longitude = 0;
    private double latitude = 0;
    private int need = -1;
    private ServiceBinder binder=new ServiceBinder();
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private Task task;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        username = AVUser.getCurrentUser().getUsername();
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();
        mLocationClient.start();
        task = new Task();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.i("mapservice", "Oncreate");
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
    }

    class Task extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                while (true) {
                    appadd();
                    Thread.sleep(1000 * 30);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void appadd() throws IOException {
        HttpURLConnection connection = null;
        if (longitude<0.01&&longitude<=0.01) {
            return;
        }
        try {
            //创建连接
            URL url = new URL("http://xunixapp.com/gps");
            connection = (HttpURLConnection) url.openConnection();


            //设置http连接属性

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST"); // 可以根据需要 提交 GET、POST、DELETE、INPUT等http提供的功能
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);

            //设置http头 消息
            connection.setRequestProperty("Content-Type", "application/json");  //设定 请求格式 json，也可以设定xml格式的
            //connection.setRequestProperty("Content-Type", "text/xml");   //设定 请求格式 xml，
            connection.setRequestProperty("Accept", "application/json");//设定响应的信息的格式为 json，也可以设定xml格式的
//             connection.setRequestProperty("X-Auth-Token","xx");  //特定http服务器需要的信息，根据服务器所需要求添加
            connection.connect();


            JSONObject user = new JSONObject();
            Calendar c = Calendar.getInstance();
            int weekDay = c.get(Calendar.DAY_OF_WEEK);
            c.setTime(new Date());
            user.put("username", username);
            user.put("need", need);
            user.put("longitude", longitude);
            user.put("latitude", latitude);
            if (weekDay == 1) {
                user.put("week", 7);
            } else {
                user.put("week", weekDay - 1);
            }
            Log.i("mapService", username + " " + latitude + " " + longitude + " " + need);
            OutputStream out = connection.getOutputStream();
            out.write(user.toString().getBytes());
            out.flush();
            out.close();

//            读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                sb.append(lines);
            }
            String msg = new String(sb);
            Log.i("mapService", msg);
            reader.close();
////              断开连接
            connection.disconnect();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000 * 5;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Intent intent = new Intent("mapService");
            intent.putExtra("radius", location.getRadius());
            intent.putExtra("POI", location.getPoiList().get(0).getName());
            intent.putExtra("longitude", longitude);
            intent.putExtra("latitude", latitude);
            sendBroadcast(intent);
        }
    }

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.start_location);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.logo)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.start_location))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        startForeground(R.string.start_location, notification);
    }
    public class ServiceBinder extends Binder {
        public void setNeed(int needs){
            need=needs;
        }
        public int getNeed(){
            return need;
        }
    }
    @Override
    public void onDestroy() {
        mLocationClient.stop();
        task.cancel(true);
        super.onDestroy();
    }
}
