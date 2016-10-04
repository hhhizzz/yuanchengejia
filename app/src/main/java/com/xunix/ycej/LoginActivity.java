package com.xunix.ycej;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;

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
                            String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
                            user.put("installationId",installationId);
                            user.saveInBackground();
                            if((int)user.get("userType")==1){
                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            }
                            else if((int)user.get("userType")==0){
                                startActivity(new Intent(LoginActivity.this,MainActivityYoung.class));
                            }
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
