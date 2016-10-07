package com.xunix.ycej.fragment;

import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.radaee.reader.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xunixhuang on 08/10/2016.
 */

public class LightFragment extends Fragment {
    private CardView button1;
    private CardView button2;
    private TextView textView1;
    private TextView textView2;
    boolean isPreview = false;
    private int cnum;
    private Camera camera;
    private TimerTask mTimerTask = null;
    private Timer mTimer = null;
    private boolean isSOSOn = false;
    private boolean isOn = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_light, container, false);
        button1 = (CardView) v.findViewById(R.id.cardViewLight);
        button2 = (CardView) v.findViewById(R.id.cardViewLight2);
        textView1 = (TextView) v.findViewById(R.id.textViewLight);
        textView2 = (TextView) v.findViewById(R.id.textViewLight2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isOn){
                    isOn=true;
                    textView1.setText("关闭手电");
                    camera = Camera.open();
                    Camera.Parameters p = camera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(p);
                    camera.startPreview();
                }
                else{
                    textView1.setText("打开手电");
                    Camera.Parameters mParameters=camera.getParameters();
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.stopPreview();
                    camera.release();
                    isOn=false;
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isC();
                if (isSOSOn) {
                    textView2.setText("打开闪光");
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                    if (mTimerTask != null) {
                        mTimerTask.cancel();
                        mTimerTask = null;
                    }
                    Camera.Parameters p = camera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                    camera.stopPreview();
                    isSOSOn = false;
                    cnum = 0;

                } else {
                    textView2.setText("关闭闪光");
                    if (mTimer == null) {
                        mTimer = new Timer();
                    }
                    if (mTimerTask == null) {
                        SetTimerTask();
                    }
                    if (mTimer != null && mTimerTask != null) {
                        mTimer.schedule(mTimerTask, 0, 50);
                        isSOSOn = true;
                        cnum = 0;

                    }
                }
            }
        });
        return v;
    }

    public void isC() {
        FeatureInfo[] feature = getActivity().getPackageManager()
                .getSystemAvailableFeatures();
        boolean hasFlashLight = false;
        for (FeatureInfo featureInfo : feature) {
            if (PackageManager.FEATURE_CAMERA_FLASH.equals(featureInfo.name)) {
                hasFlashLight = true;
                break;
            }
        }
    }

    void SetTimerTask() {
        if (camera != null) {
            if (isPreview)
                camera.stopPreview();
            camera.release();
            camera = null;
        }

        camera = Camera.open();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Camera.Parameters p = camera.getParameters();
                switch (cnum) {
                    case 0:
                    case 4:
                    case 8:
                    case 14:
                    case 22:
                    case 30:
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(p);
                        camera.startPreview();
                        break;
                    case 2:
                    case 6:
                    case 10:
                    case 18:
                    case 26:
                    case 34:
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        camera.setParameters(p);
                        camera.startPreview();
                        break;
                    default:
                        break;
                }
                if (cnum == 35)
                    cnum = 0;
                else
                    cnum++;
            }
        };
    }
}
