package com.xunix.ycej.help;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 录像线程
 *
 * @author bcaiw
 *
 */
public class RecordThread extends Thread {

	private MediaRecorder mediarecorder;// 录制视频的类
	private SurfaceHolder surfaceHolder;
	private long recordTime;
	private SurfaceView surfaceview;// 显示视频的控件
	private Camera mCamera;
	private Boolean isStop = false;
	private File mSampleFile = null;

	public RecordThread(long recordTime, SurfaceView surfaceview,
						SurfaceHolder surfaceHolder) {
		this.recordTime = recordTime;
		this.surfaceview = surfaceview;
		this.surfaceHolder = surfaceHolder;
	}

	@Override
	public void run() {

		/**
		 * 开始录像
		 */
		startRecord();

		/**
		 * 启动定时器，到规定时间recordTime后执行停止录像任务
		 */
		Timer timer = new Timer();
		timer.schedule(new TimerThread(), recordTime);
	}

	/**
	 * 获取摄像头实例对象
	 *
	 * @return
	 */
	public Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
			// 打开摄像头错误
			Log.i("info", "打开摄像头错误");
		}
		return c;
	}

	/**
	 * 开始录像
	 */
	@SuppressLint("SdCardPath")
	public void startRecord() {
		if (mediarecorder != null) {
			mediarecorder.stop();
			// 释放资源
			mediarecorder.release();
			mediarecorder = null;

			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
		mediarecorder = new MediaRecorder();// 创建mediarecorder对象
		mCamera = getCameraInstance();
		// 解锁camera
		mCamera.unlock();
		mediarecorder.setCamera(mCamera);

		// 设置录制视频源为Camera(相机)
		mediarecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// 设置录制文件质量，格式，分辨率之类，这个全部包括了
		mediarecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_LOW));

		mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
		// 设置视频文件输出的路径
		File path = new File(Environment.getExternalStorageDirectory()
				.getAbsoluteFile() + "/yuanchengejia/");
		path.mkdir();
		try {

			String path1 = Environment.getExternalStorageDirectory()
					.getAbsoluteFile() + "/yuanchengejia/Video.mp4";

			mSampleFile = new File(path1);
			mSampleFile.createNewFile();

		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//mediarecorder.setOutputFile("/sdcard/Uh/Vedio.mp4");
		mediarecorder.setOutputFile(mSampleFile.getAbsolutePath());
		try {
			// 准备录制
			mediarecorder.prepare();
			mediarecorder.start();
			isStop = true;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 停止录制
	 */
	public void stopRecord() {
		if (mediarecorder != null) {
			// 停止录制
			if (isStop) {
			    mediarecorder.stop();
				mediarecorder.release();
				mediarecorder = null;
			}
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}

	}

	/**
	 * 定时器
	 *
	 * @author bcaiw
	 *
	 */
	class TimerThread extends TimerTask {
		/**
		 * 停止录像
		 */
		@Override
		public void run() {
			stopRecord();
			this.cancel();
		}
	}
}
