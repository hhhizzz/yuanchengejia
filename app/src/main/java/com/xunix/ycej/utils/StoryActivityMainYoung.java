package com.xunix.ycej.utils;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.radaee.reader.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by xunixhuang on 08/10/2016.
 */

public class StoryActivityMainYoung extends AppCompatActivity implements OnPageChangeListener,View.OnClickListener{
    private PDFView pdfView;
    private File pdfFile;
    private Button buttonPlay;
    private int currentPage=1;
    private File folder;
    private boolean recording = false;
    private boolean playing = false;
    private MediaRecorder recorder;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_young);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonPlay=(Button)findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(this);
        pdfView=(PDFView)findViewById(R.id.pdfView);
        folder=new File(getIntent().getStringExtra("filepath"));
        if (!folder.isDirectory()) {
            Toast.makeText(this, "文件损坏", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            File[] files = folder.listFiles();
            for (File theFile : files) {
                String name = theFile.getName();
                int dot = name.lastIndexOf(".");
                String last = name.substring(dot + 1, name.length());
                if (last.equals("PDF") || last.equals("pdf")) {
                    pdfFile = new File(theFile.getAbsolutePath());
                    break;
                }
            }
            if (pdfFile == null) {
                Toast.makeText(this, "文件损坏", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                pdfView.fromFile(pdfFile)
                        .enableSwipe(true)
                        .defaultPage(0)
                        .enableDoubletap(true)
                        .enableAnnotationRendering(false)
                        .password(null)
                        .scrollHandle(null)
                        .swipeHorizontal(true)
                        .onPageChange(this)
                        .load();
            }
        }
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playing=false;
                buttonPlay.setText("播放");
                mPlayer.stop();
                mPlayer.reset();
            }
        });
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        page+=1;
        currentPage=page;
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(folder.getName()+" "+page + "/" + pageCount);
        }
    }
    private void resetRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);     //设置录制设备
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);   //设置输出格式
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);   //设置编码器
        recorder.setOutputFile(folder.getAbsolutePath() + "/" + Integer.toString(currentPage) + ".wav");      //设置输出文件
        try {
            recorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonRecord:
                onRecord();
                break;
            case R.id.buttonPlay:
                onPlay();
                break;
        }
    }

    public void onRecord() {
        if (!recording) {
            resetRecorder();
            Toast.makeText(this,"开始录音",Toast.LENGTH_SHORT).show();
            buttonPlay.setEnabled(false);
            recorder.start();
            recording = true;
        } else {
            recording = false;
            buttonPlay.setEnabled(true);
            recorder.stop();
            Toast.makeText(this,"录音成功",Toast.LENGTH_SHORT).show();
        }
    }

    public void onPlay() {
        if (!playing) {
            try {
                Log.i("onPlay",folder.getAbsolutePath() + "/" + Integer.toString(currentPage) + ".wav");
                File wav=new File(folder.getAbsolutePath() + "/" + Integer.toString(currentPage) + ".wav");
                if(wav.exists()) {
                    playing = true;
                    buttonPlay.setText("停止");
                    mPlayer.setDataSource(wav.getAbsolutePath());
                    mPlayer.prepare();
                    mPlayer.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mPlayer.stop();
            mPlayer.reset();
            buttonPlay.setText("播放");
            playing = false;
        }
    }
}
