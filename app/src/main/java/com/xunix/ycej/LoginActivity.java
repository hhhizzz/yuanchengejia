package com.xunix.ycej;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.avos.avoscloud.*;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;
import com.xunix.ycej.service.MapService;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialEditText usernameEditText;
    private MaterialEditText passwordEditText;
    private ProgressView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.buttonRegister:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.buttonLogin:
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                progressView.start();
                AVUser.logInInBackground(username, password, new LogInCallback<AVUser>() {
                    @Override
                    public void done(AVUser user, AVException e) {
                        progressView.stop();
                        if (e == null) {
                            if((int)user.get("userType")==1){
                                PushService.setDefaultPushCallback(LoginActivity.this, MainActivity.class);       //设置推送
                                PushService.subscribe(LoginActivity.this, AVUser.getCurrentUser().getUsername(), MainActivity.class);
                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            }
                            else if((int)user.get("userType")==0){
                                PushService.setDefaultPushCallback(LoginActivity.this, MainActivityYoung.class);       //设置推送
                                PushService.subscribe(LoginActivity.this, AVUser.getCurrentUser().getUsername(), MainActivityYoung.class);
                                startActivity(new Intent(LoginActivity.this,MainActivityYoung.class));
                            }
                            else if((int)user.get("userType")==2){
                                PushService.setDefaultPushCallback(LoginActivity.this, MainActivityOld.class);       //设置推送
                                PushService.subscribe(LoginActivity.this, AVUser.getCurrentUser().getUsername(), MainActivityOld.class);
                                startActivity(new Intent(LoginActivity.this,MainActivityOld.class));
                            }
                            AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
                                public void done(AVException e) {
                                    if (e == null) {
                                        // 保存成功
                                        String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
                                        AVUser.getCurrentUser().put("installationId",installationId);
                                        AVUser.getCurrentUser().saveInBackground();
                                    } else {
                                        // 保存失败，输出错误信息
                                    }
                                }
                            });
                            finish();
                        }
                        else{
                            Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
        }
    }

    private void initView() {
        usernameEditText = (MaterialEditText) findViewById(R.id.usernameEditText);
        passwordEditText = (MaterialEditText) findViewById(R.id.passwordEditText);
        Button registButton = (Button) findViewById(R.id.buttonRegister);
        Button loginButton = (Button) findViewById(R.id.buttonLogin);
        progressView = (ProgressView) findViewById(R.id.progressView);
        registButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
    }
}
