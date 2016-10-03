package com.xunix.ycej;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import com.rey.material.widget.Spinner;

/**
 * Created by xunixhuang on 02/10/2016.
 */

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Spinner spinner=(Spinner)findViewById(R.id.spinnerType);
        String[] items = new String[3];
        items[0]="家长";
        items[1]="儿童";
        items[2]="老人";
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_spn, items);
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        spinner.setAdapter(adapter);
    }
}
