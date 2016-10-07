package com.xunix.ycej;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.radaee.reader.R;
import com.xunix.ycej.fragment.AttentionFragment;
import com.xunix.ycej.fragment.FunctionFragmentOld;
import com.xunix.ycej.fragment.HomeworkFragment;
import com.xunix.ycej.fragment.StoryFragment;
import com.xunix.ycej.utils.FileSave;
import com.xunix.ycej.utils.TabEntity;
import me.iwf.photopicker.PhotoPicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by xunixhuang on 05/10/2016.
 */

public class ActionActivity extends AppCompatActivity {
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ArrayList<String> files;
    private String[] mTitles = {"作业批改", "故事聆听", "消息提醒"};
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

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        final ViewPager viewPager = (ViewPager) findViewById(R.id.vp);
        tabLayout = (CommonTabLayout) findViewById(R.id.tl);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_story:
                return super.onOptionsItemSelected(item);
            case R.id.create_homework:
                goToAlbum();
                return super.onOptionsItemSelected(item);
            case android.R.id.home:
                finish();
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }


    private void goToAlbum() {
        PhotoPicker.builder()
                .setPhotoCount(20)
                .setShowCamera(true)
                .setShowGif(true)
                .setPreviewEnabled(false)
                .start(this, PhotoPicker.REQUEST_CODE);
        Toast.makeText(this, "请选择图片以制作作业", Toast.LENGTH_SHORT).show();
    }

    private void makepdf() throws IOException, DocumentException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒", Locale.CHINA);//设置日期格式

        File file = new File(FileSave.getHomeworkPath() + df.format(new Date()) + "的作业.pdf");
        Log.i("FileName", file.getName());
        file.createNewFile();
        String dest = file.getAbsolutePath();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();
        PdfContentByte canvas = writer.getDirectContentUnder();
        for (int i = 0; i < files.size(); i++) {
            Image image = Image.getInstance(files.get(i));
            image.scaleAbsolute(PageSize.A4);
            image.setAbsolutePosition(0, 0);
            canvas.addImage(image);
            document.newPage();
        }
        document.close();
        Toast.makeText(this, "制作完毕", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PhotoPicker.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                files = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                try {
                    makepdf();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }
}
