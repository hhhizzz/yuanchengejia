package com.radaee.reader;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.radaee.pdf.Document;
import com.radaee.view.PDFVPage;
import com.radaee.view.PDFView;
import com.radaee.view.PDFViewVert;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by radaee on 2015/5/11.
 */
public class PDFSurfaceView extends SurfaceView implements SurfaceHolder.Callback, PDFView.PDFViewListener {
    private PDFView m_view = null;
    private Document m_doc = null;
    private void init()
    {
        m_holder = this.getHolder();
        m_holder.addCallback(this);
    }
    public PDFSurfaceView(Context context) {
        super(context);
        init();
    }
    public PDFSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private SurfaceHolder m_holder;
    private DrawThread m_draw_thread;
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        m_draw_thread = new DrawThread();
        m_draw_thread.start();
        if(m_view != null)
            m_draw_thread.invalidate();
    }
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        m_draw_thread.close();
        m_draw_thread = null;
    }
    private Handler m_hand_ui = new Handler(Looper.myLooper()) {
        public void handleMessage(Message msg) {
            if (msg != null) {
                computeScroll();
            }
        }
    };
    private class DrawThread extends Thread
    {
        private Handler m_hand = null;
        private boolean is_notified = false;
        private boolean is_waitting = false;
        private synchronized void wait_init()
        {
            try
            {
                if( is_notified )
                    is_notified = false;
                else
                {
                    is_waitting = true;
                    wait();
                    is_waitting = false;
                }
            }
            catch(Exception e)
            {
            }
        }
        private synchronized void notify_init()
        {
            if( is_waitting )
                notify();
            else
                is_notified = true;
        }
        @Override
        public void start()
        {
            super.start();
            wait_init();
        }
        @Override
        public void run()
        {
            Looper.prepare();
            m_hand = new Handler(Looper.myLooper())
            {
                public void handleMessage(Message msg)
                {
                    if( msg != null )
                    {
                        if( msg.what == 0 )//render function
                        {
                            m_hand_ui.sendEmptyMessage(0);
                            if(m_view != null) {
                                Canvas canvas = m_holder.lockCanvas();
                                m_view.vDraw(canvas);
                                m_holder.unlockCanvasAndPost(canvas);
                            }
                            super.handleMessage(msg);
                        }
                        else if( msg.what == 100 )//quit
                        {
                            super.handleMessage(msg);
                            getLooper().quit();
                        }
                    }
                    else
                        getLooper().quit();
                }
            };
            notify_init();
            Looper.loop();
        }
        public void invalidate()
        {
            m_hand.sendEmptyMessage(0);
        }
        public synchronized void close()
        {
            try
            {
                m_hand.sendEmptyMessage(100);
                join();
                m_hand = null;
            }
            catch(InterruptedException e)
            {
            }
        }
    }
    public void PDFOpen(Document doc)
    {
        PDFViewVert view = new PDFViewVert(this.getContext());
        view.vSetPageAlign(1);//center;
        m_doc = doc;
        m_view = view;
        m_view.vOpen(m_doc, 4, 0xFFCCCCCC, this);
        m_view.vResize(getWidth(), getHeight());
    }
    public void PDFClose()
    {
        if( m_view != null )
        {
            m_view.vClose();
            m_view = null;
        }
        if( m_doc != null )
            m_doc = null;
    }
    @Override
    protected void onSizeChanged( int w, int h, int oldw, int oldh )
    {
        super.onSizeChanged(w,h,oldw,oldh);
        if( m_view == null ) return;
        if( w <= 0 || h <= 0 )
        {
            w = getWidth();
            h = getHeight();
        }
        PDFView.PDFPos pos = m_view.vGetPos(w/2, h/2);
        m_view.vResize(w, h);
        m_view.vSetScale(0, 0, 0);//fit page while resizing.
        if( pos != null )
        {
            m_view.vSetPos(pos, w/2, h/2);
            //m_view.vCenterPage(pos.pageno);
        }
    }
    @Override
    public void computeScroll()
    {
        if( m_view == null ) return;
        m_view.vComputeScroll();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if( m_view == null ) return false;
        return m_view.vTouchEvent(event);
    }
    @Override
    public void OnPDFPageChanged(int pageno)
    {
    }

    @Override
    public boolean OnPDFDoubleTapped(float x, float y) {
        return false;
    }

    @Override
    public boolean OnPDFSingleTapped(float x, float y) {
        return false;
    }

    @Override
    public void OnPDFLongPressed(float x, float y) {

    }

    @Override
    public void OnPDFShowPressed(float x, float y) {

    }

    @Override
    public void OnPDFSelectEnd() {

    }

    @Override
    public void OnPDFFound(boolean found) {

    }

    @Override
    public void OnPDFInvalidate(boolean post)
    {
        if(m_draw_thread == null) return;
        m_draw_thread.invalidate();
    }

    @Override
    public void OnPDFPageDisplayed(Canvas canvas, PDFVPage vpage)//from backing thread
    {
    }

    @Override
    public void OnPDFSelecting(Canvas canvas, int[] rect1, int[] rect2) {

    }

    @Override
    public void OnPDFZoomStart() {

    }

    @Override
    public void OnPDFZoomEnd() {

    }
}
