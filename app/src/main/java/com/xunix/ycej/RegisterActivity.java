package com.xunix.ycej;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;
import com.rey.material.widget.Spinner;

/**
 * Created by xunixhuang on 02/10/2016.
 */

public class RegisterActivity extends AppCompatActivity {
    private int currentType=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final Spinner spinner=(Spinner)findViewById(R.id.spinnerType);
        Button buttonRegister=(Button)findViewById(R.id.buttonRegister);
        final MaterialEditText usernameEdit=(MaterialEditText)findViewById(R.id.usernameEditText);
        final MaterialEditText passwdEdit=(MaterialEditText)findViewById(R.id.passwordEditText);
        final MaterialEditText passwdRepeat=(MaterialEditText)findViewById(R.id.passwordRepeatEditText);
        final ProgressView progressView=(ProgressView)findViewById(R.id.progressView);
        String[] items = new String[3];
        items[1]="家长";
        items[0]="儿童";
        items[2]="老人";
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_spn, items);
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                currentType=position;
            }
        });
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username=usernameEdit.getText().toString();
                String passwd=passwdEdit.getText().toString();
                String passRepeat=passwdRepeat.getText().toString();
                if(passwd.equals(passRepeat)){
                    progressView.start();
                    AVUser avUser=new AVUser();
                    avUser.setUsername(username);
                    avUser.put("userType",currentType);
                    avUser.setPassword(passwd);
                    avUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if(e==null){
                                Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                                progressView.stop();
                                finish();
                            }
                            else{
                                Toast.makeText(RegisterActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
