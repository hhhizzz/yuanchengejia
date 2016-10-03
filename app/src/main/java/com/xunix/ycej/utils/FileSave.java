package com.xunix.ycej.utils;

import android.net.Uri;
import android.os.Environment;
import com.xunix.ycej.message.AVIMPDFMessage;
import com.xunix.ycej.message.AVIMStoryMessage;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 用于存储文件
 *
 * @author Xunix Huang
 * @version 0.160720
 */
public class FileSave {
    private static File rootPath;
    private static File homeworkPath;
    private static File imagePath;
    private static File storyPath;


    public static void init() {
        rootPath = new File(Environment.getExternalStorageDirectory(), "yuanchengejia");
        if (!rootPath.exists()) {
            rootPath.mkdir();
        }
        homeworkPath = new File(rootPath.getAbsolutePath(), "homework");
        if (!homeworkPath.exists()) {
            homeworkPath.mkdir();
        }
        imagePath = new File(rootPath.getAbsolutePath(), "image");
        if (!imagePath.exists()) {
            imagePath.mkdir();
        }
        storyPath = new File(rootPath.getAbsolutePath(), "story");
        if (!storyPath.exists()) {
            storyPath.mkdir();
        }
    }

    public static String getHomeworkPath() {
        return homeworkPath.getAbsolutePath() + "/";
    }

    public static String getStoryPath() {
        return storyPath.getAbsolutePath() + "/";
    }

    public static String getRootPath(){
        return rootPath.getAbsolutePath()+"/";
    }
    public static String getImagePath(){
        return imagePath.getAbsolutePath()+"/";
    }
    public static String getDefaultPortrait(){
        return getImagePath()+"/portrait.png";
    }



    /**
     * 获得故事文件夹里的文件
     */
    public static LinkedList<File> getStoryFiles() {
        File[] files = new File(storyPath.getAbsolutePath()).listFiles();
        if (files == null) {
            return null;
        }
        LinkedList<File> fileLinkedList = new LinkedList<>();
        Collections.addAll(fileLinkedList, files);
        return fileLinkedList;
    }

    /**
     * 获得作业文件夹里的文件
     */
    public static LinkedList<File> getHomeworkFiles() {
        File[] files = new File(homeworkPath.getAbsolutePath()).listFiles();
        if (files == null) {
            return null;
        }
        LinkedList<File> fileLinkedList = new LinkedList<>();
        Collections.addAll(fileLinkedList, files);
        return fileLinkedList;
    }

    /*
    *   获得图像的uri
    */
    public static Uri getImageUri(final String URL){
        if(URL==null){
            File file=new File(getImagePath()+"/portrait.png");
            return Uri.fromFile(file);
        }
        int dot=URL.lastIndexOf("/");
        String fileName=URL.substring(dot+1);
        fileName+=".jpg";
        final File file=new File(getImagePath()+fileName);
        if(file.exists()){
            return Uri.fromFile(file);
        }
        else{
            new Thread() {

                public void run() {
                    try {
                        java.net.URL urlImage = new URL(URL);
                        InputStream is = urlImage.openStream();
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        is.close();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            return Uri.fromFile(file);
        }
    }

    /**
     * 从消息中下载作业
     */
    public static void saveHomework(AVIMPDFMessage msg) {
        final String url = msg.getFileUrl();
        final String name = msg.getFileName();

        final File pdfFile = new File(homeworkPath, name);
        if (pdfFile.exists()) {
            return;
        }
        new Thread() {

            public void run() {
                try {
                    URL urlImage = new URL(url);
                    InputStream is = urlImage.openStream();
                    FileOutputStream fos = new FileOutputStream(pdfFile);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public static void saveStory(AVIMStoryMessage msg) {
        final String url = msg.getFileUrl();
        final String name = msg.getFileName();
        final File storyFile = new File(storyPath, name);
        if (storyFile.exists()) {
            return;
        }
        new Thread() {

            public void run() {
                try {
                    URL urlImage = new URL(url);
                    InputStream is = urlImage.openStream();
                    FileOutputStream fos = new FileOutputStream(storyFile);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                    unzip(storyFile.getAbsolutePath(), storyPath.getAbsolutePath());
                    storyFile.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static File zipStory(File file) {
        File desFile = new File(storyPath, file.getName() + ".zip");
        if (desFile.exists()) {
            desFile.delete();
        }
        try {
            zip(file, desFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return desFile;
    }


    private static void zip(File inputFile, File outPutFile) throws Exception {
        InputStream inputStream = null;
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outPutFile));
        int temp = 0;
        File[] lists = inputFile.listFiles();
        for (File thefile : lists) {
            inputStream = new FileInputStream(thefile);
            zipOutputStream.putNextEntry(new ZipEntry(inputFile.getName() + File.separator + thefile.getName()));
            while ((temp = inputStream.read()) != -1) {
                zipOutputStream.write(temp);
            }
            inputStream.close();
        }
        zipOutputStream.close();
    }

    public static File unzip(String zipPath, String filePath) {
        File Fout = null;
        try {
            ZipInputStream Zin = new ZipInputStream(new FileInputStream(
                    zipPath));//输入源zip路径
            BufferedInputStream Bin = new BufferedInputStream(Zin);
            String Parent = filePath; //输出路径（文件夹目录）

            ZipEntry entry;
            try {
                while ((entry = Zin.getNextEntry()) != null && !entry.isDirectory()) {
                    Fout = new File(Parent, entry.getName());
                    if (!Fout.exists()) {
                        (new File(Fout.getParent())).mkdirs();
                    }
                    FileOutputStream out = new FileOutputStream(Fout);
                    BufferedOutputStream Bout = new BufferedOutputStream(out);
                    int b;
                    while ((b = Bin.read()) != -1) {
                        Bout.write(b);
                    }
                    Bout.close();
                    out.close();
                }
                Bin.close();
                Zin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        return Fout;
    }
    public static void saveContact(String contactNumber) throws IOException {
        File contactFile = new File(getRootPath(),"contact.txt");
        if(!contactFile.exists()){
            contactFile.createNewFile();
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(contactFile));
        out.write(contactNumber);
        out.flush();
        out.close();
    }
    public static void saveContact_Friend(String name) throws IOException {
        File contactFile = new File(getRootPath(),"contact_friend.txt");
        if(!contactFile.exists()){
            contactFile.createNewFile();
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(contactFile));
        out.write(name);
        out.flush();
        out.close();
    }

    public static String getContact() {
        File contactFile=new File(getRootPath(),"contact.txt");
        if(!contactFile.exists()){
            return null;
        }
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(contactFile));
            BufferedReader br = new BufferedReader(reader);
            String contact = br.readLine();
            return contact;
        }
        catch (IOException e){
            return null;
        }
    }
    public static String getContact_friend() {
        File contactFile=new File(getRootPath(),"contact_friend.txt");
        if(!contactFile.exists()){
            return null;
        }
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(contactFile));
            BufferedReader br = new BufferedReader(reader);
            String contact = br.readLine();
            return contact;
        }
        catch (IOException e){
            return null;
        }
    }
}
