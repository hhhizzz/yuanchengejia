package com.xunix.ycej;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.radaee.reader.R;
import com.xunix.ycej.fragment.AttentionFragment;
import com.xunix.ycej.fragment.FunctionFragmentOld;
import com.xunix.ycej.fragment.HomeworkFragment;
import com.xunix.ycej.fragment.StoryFragment;
import com.xunix.ycej.utils.TabEntity;

import java.util.ArrayList;

/**
 * Created by xunixhuang on 05/10/2016.
 */

public class ActionActivity extends AppCompatActivity {
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private String[] mTitles = {"作业批改", "故事聆听","消息提醒"};
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
        setContentView(R.layout.activity_action);
        initView();
    }
    private void initView(){
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        mFragments.add(new HomeworkFragment());
        mFragments.add(new StoryFragment());
        mFragments.add(new AttentionFragment());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setTitle("互动");
        final ViewPager viewPager=(ViewPager)findViewById(R.id.vp);
        tabLayout=(CommonTabLayout)findViewById(R.id.tl);
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        for (int i = 0; i < mTitles.length; i++) {
            tabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }
        tabLayout.setTabData(tabEntities);
        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setCurrentItem(0);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }
}
