package com.xunix.ycej.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.avos.avoscloud.*;
import com.radaee.reader.R;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xunix.ycej.adapter.FriendAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by xunixhuang on 06/10/2016.
 */

public class AttentionFragment extends Fragment implements View.OnClickListener{
    private MaterialEditText editText;
    private Calendar calendar = Calendar.getInstance();
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private String[] username;
    private List<AVUser> users;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_attention, container, false);
        editText = (MaterialEditText) v.findViewById(R.id.attentionEdit);
        username = new String[FriendAdapter.getRemarks().size()];
        Button buttonTime=(Button)v.findViewById(R.id.time_choose);
        Button buttonDate=(Button)v.findViewById(R.id.date_choose);
        Button buttonSend=(Button)v.findViewById(R.id.object_choose);
        buttonTime.setOnClickListener(this);
        buttonDate.setOnClickListener(this);
        buttonSend.setOnClickListener(this);
        users=FriendAdapter.getUsers();
        for (int i = 0; i < FriendAdapter.getRemarks().size(); i++) {
            username[i] = FriendAdapter.getRemarks().get(i);
        }
        return v;
    }

    public void showTimePickerDialog() {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
                hour = hourOfDay;
                minute = minutes;
            }
        };
        Dialog dialog = new TimePickerDialog(getActivity(), timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);   //是否为二十四制
        dialog.show();
    }

    public void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int years, int monthOfYear, int dayOfMonth) {
                year = years;
                month = monthOfYear;
                day = dayOfMonth;
            }
        };
        Dialog dialog = new DatePickerDialog(getActivity(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    public void sendMessage() {
        if(editText.getText().toString().equals("")){
            Toast.makeText(getActivity(),"请输入提醒消息",Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("请选择好友");
        builder.setItems(username, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AVQuery pushQuery = AVInstallation.getQuery();
                String THE_INSTALLATION_ID = (String) users.get(which).get("installationId");
                pushQuery.whereEqualTo("installationId", THE_INSTALLATION_ID);
                AVPush push = new AVPush();
                push.setQuery(pushQuery);
                GregorianCalendar cal = new GregorianCalendar();
                cal.set(year, month, day, hour, minute);
                push.setPushDate(new Date(cal.getTimeInMillis()));
                Log.i("AttentionActivity", "time" + hour + " " + minute);
                push.setMessage(AVUser.getCurrentUser().getUsername() + "提醒你");
                push.sendInBackground(new SendCallback() {
                    @Override
                    public void done(AVException e) {
                        Toast.makeText(getActivity(), "发送成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.create().show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.time_choose:
                showTimePickerDialog();
                break;
            case R.id.date_choose:
                showDatePickerDialog();
                break;
            case R.id.object_choose:
                sendMessage();
                break;
        }
    }
}
