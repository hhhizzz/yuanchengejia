package com.xunix.ycej;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.avos.avoscloud.AVUser;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.xunix.ycej.fragment.FunctionFragmentOld;
import com.xunix.ycej.fragment.FriendFragmentOld;
import com.xunix.ycej.utils.ViewFindUtils;

import java.util.ArrayList;


/**
 * Created by xunixhuang on 04/10/2016.
 */

public class MainActivityOld extends AppCompatActivity {
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private String[] mTitles = {"功能", "家人"};
    private SegmentTabLayout mTabLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);
        initView();
    }

    private void initView() {
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFragments.add(new FunctionFragmentOld());
        mFragments.add(new FriendFragmentOld());
        View mDecorView = getWindow().getDecorView();
        mTabLayout = ViewFindUtils.find(mDecorView, R.id.tabLayout);
        final ViewPager vp = ViewFindUtils.find(mDecorView, R.id.viewPager);
        vp.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        mTabLayout.setTabData(mTitles);
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                vp.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vp.setCurrentItem(1);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.searchFridend:
                        startActivity(new Intent(MainActivityOld.this,SearchFriendActivity.class));
                        break;
                    case R.id.logout:
                        AVUser.logOut();
                        startActivity(new Intent(MainActivityOld.this,LoginActivity.class));
                        finish();
                        break;
                    case R.id.setting:
                        Toast.makeText(MainActivityOld.this, "账号设置", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
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
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}
