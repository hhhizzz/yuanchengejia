package com.xunix.ycej;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.baoyz.widget.PullRefreshLayout;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.radaee.reader.R;
import com.xunix.ycej.adapter.FriendAdapter;
import com.xunix.ycej.adapter.HomeWorkAdapter;
import com.xunix.ycej.message.AVIMPDFMessage;
import com.xunix.ycej.utils.FileSave;
import me.iwf.photopicker.PhotoPicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Created by xunixhuang on 08/10/2016.
 */

public class HomeworkActivityYoung extends AppCompatActivity {
    private RecyclerView homworkList;
    private HomeWorkAdapter adapter;
    private ArrayList<String> files;
    private PullRefreshLayout refreshLayout;
    private Toolbar toolbar;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_homework:
                goToAlbum();
                return super.onOptionsItemSelected(item);
            case android.R.id.home:
                finish();
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_young);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        homworkList = (RecyclerView) findViewById(R.id.homeworkList);
        refreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        homworkList.setLayoutManager(llm);
        adapter = new HomeWorkAdapter(this);
        homworkList.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.onRefresh();
                        Toast.makeText(HomeworkActivityYoung.this, "文件刷新成功", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        adapter.setClickListener(new HomeWorkAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(HomeworkActivityYoung.this, MyPDFReader.class);
                intent.putExtra("filepath", FileSave.getHomeworkFiles().get(position).getAbsolutePath());
                startActivity(intent);
            }
        });
        adapter.setLongClickListener(new HomeWorkAdapter.OnItemLongClickListener() {
            @Override
            public boolean onLongClick(View view, final int position) {
                Log.i("homeworkfragment", "longclick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(HomeworkActivityYoung.this);
                builder.setMessage("请选择对作业的操作")
                        .setPositiveButton("发送", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(HomeworkActivityYoung.this);
                                builder1.setTitle("请选择发送的好友");
                                String[] usernames = new String[FriendAdapter.getRemarks().size()];
                                for (int ii = 0; ii < FriendAdapter.getRemarks().size(); ii++) {
                                    usernames[ii] = FriendAdapter.getRemarks().get(ii);
                                }
                                builder1.setItems(usernames, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        sendHomework(position, i);
                                    }
                                });
                                builder1.create().show();
                            }
                        })
                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (adapter.deleteFile(position)) {
                                    Toast.makeText(HomeworkActivityYoung.this, "删除成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(HomeworkActivityYoung.this, "删除失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                builder.create().show();
                return false;
            }
        });

    }
    private void sendHomework(final int homeworkPosition, int friendPosition) {
        final String username = FriendAdapter.getUsers().get(friendPosition).getUsername();
        final String myname = AVUser.getCurrentUser().getUsername();
        AVIMClient theClient = AVIMClient.getInstance(myname);
        theClient.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if (e == null) {
                    avimClient.createConversation(Arrays.asList(username, myname), username + "&" + myname, null, false, true, new AVIMConversationCreatedCallback() {
                        @Override
                        public void done(final AVIMConversation avimConversation, AVIMException e) {
                            if (e == null) {
                                try {
                                    final File file = FileSave.getHomeworkFiles().get(homeworkPosition);
                                    final AVIMPDFMessage message = new AVIMPDFMessage(file);
                                    message.getAVFile().saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(AVException e) {
                                            message.setFileUrl(message.getAVFile().getUrl());
                                            message.setFileName(file.getName());
                                            avimConversation.sendMessage(message, new AVIMConversationCallback() {
                                                @Override
                                                public void done(AVIMException e) {
                                                    Toast.makeText(HomeworkActivityYoung.this, "发送成功", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });
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
        getMenuInflater().inflate(R.menu.young_homework_menu, menu);
        return true;
    }
}
