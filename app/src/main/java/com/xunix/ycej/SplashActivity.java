package com.xunix.ycej;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.avos.avoscloud.AVUser;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xunixhuang on 02/10/2016.
 */

public class SplashActivity extends Activity
{
    private static final long DELAY = 3000;
    private boolean scheduled = false;
    private Timer splashTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashTimer = new Timer();
        splashTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                SplashActivity.this.finish();
                if(AVUser.getCurrentUser()==null) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                else{
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                }
            }
        }, DELAY);
        scheduled = true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (scheduled)
            splashTimer.cancel();
        splashTimer.purge();
    }
}
