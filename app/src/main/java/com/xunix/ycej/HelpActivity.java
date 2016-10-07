package com.xunix.ycej;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.radaee.reader.R;
import com.xunix.ycej.fragment.FunctionFragmentOld;
import com.xunix.ycej.fragment.HelpFragment;
import com.xunix.ycej.fragment.LightFragment;
import com.xunix.ycej.fragment.VideoFragment;
import com.xunix.ycej.help.RecordActivity;
import com.xunix.ycej.utils.FileSave;
import com.xunix.ycej.utils.TabEntity;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by xunixhuang on 05/10/2016.
 */

public class HelpActivity extends AppCompatActivity {
    private HelpFragment helpFragment=new HelpFragment();
    private long firstClickTime = 0;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private String[] mTitles = {"求救", "录像","手电筒"};
    private ArrayList<CustomTabEntity> tabEntities = new ArrayList<>();
    private CommonTabLayout tabLayout;
    private int[] mIconUnselectIds = {
            R.mipmap.tab_home_unselect,
            R.mipmap.tab_speech_unselect,
            R.mipmap.tab_contact_unselect};
    private int[] mIconSelectIds = {
            R.mipmap.tab_home_select,
            R.mipmap.tab_speech_select,
            R.mipmap.tab_contact_select};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        initView();
    }
    private void initView(){
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        mFragments.add(helpFragment);
        mFragments.add(new VideoFragment());
        mFragments.add(new LightFragment());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setTitle("求救");
        tabLayout=(CommonTabLayout)findViewById(R.id.tl);
        for (int i = 0; i < mTitles.length; i++) {
            tabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }
        tabLayout.setTabData(tabEntities,this,R.id.fl,mFragments);
    }
    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.contact:
                LayoutInflater li = LayoutInflater.from(this);
                View view = li.inflate(R.layout.dialog_contact ,null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("设置电话紧急联系人");
                //之前inflate的View 放到dialog中
                builder.setView(view);
                EditText editText=(EditText)view.findViewById(R.id.editText_prompt);
                editText.setText(FileSave.getContact());
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog ad = (AlertDialog) dialog;
                        EditText editText=(EditText)ad.findViewById(R.id.editText_prompt);
                        String contact=editText.getText().toString();
                        if(!contact.equals("")){
                            try {
                                FileSave.saveContact(contact);
                                Toast.makeText(HelpActivity.this,"存储成功",Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.create().show();
                return true;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                helpFragment.sendHelpMessage();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                clickEvent();
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void clickEvent(){
        Log.i("HelpActivity","volume down click "+firstClickTime);
        long secondClickTime = System.currentTimeMillis();
        long dtime = secondClickTime - firstClickTime;
        if(dtime < 500){
            //实现双击
            Intent intent=new Intent(this, RecordActivity.class);
            startActivity(intent);
        } else{
            firstClickTime = System.currentTimeMillis();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
