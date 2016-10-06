package com.radaee.reader;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import com.radaee.pdf.*;


public class PDFGLViewAct extends Activity
{
    private Document m_doc = new Document();
    private DIB m_dib = new DIB();
    private GLSurfaceView m_view;
    class SimpleRenderer implements Renderer
    {
        private int m_w;
        private int m_h;
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config)
        {
            m_w = m_view.getWidth();
            m_h = m_view.getHeight();
        }
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height)
        {
            m_w = width;
            m_h = height;
            int pageno = 0;
            float w = m_doc.GetPageWidth(pageno);
            float h = m_doc.GetPageHeight(pageno);
            if(w > 0 && h > 0)
            {
                float scale = m_w / w;
                int dibw = m_w;
                int dibh = (int) (h * scale);
                Page page = m_doc.GetPage(pageno);
                m_dib.CreateOrResize(dibw, dibh);
                page.RenderPrePare(m_dib);
                Matrix mat = new Matrix(scale, -scale, 0, dibh);
                page.Render(m_dib, mat);
                mat.Destroy();
                page.Close();
            }
            gl.glViewport(0, 0, m_w, m_h);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glOrthof(0, m_w, m_h, 0, 1, -1);
            gl.glEnable(GL10.GL_TEXTURE_2D);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        }
        @Override
        public void onDrawFrame(GL10 gl)
        {
            gl.glClearColor(0.75f, 0.75f, 0.75f, 1);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            m_dib.GLDraw(gl, 0, 0);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Global.Init( this );
        if( m_doc.Open("/sdcard/test00.pdf", null) != 0 )
        {
            m_dib.CreateOrResize(100, 100);
            m_dib.DrawRect(0xFFFF0000, 0, 0, 100, 100, 1);
        }
        m_view = new GLSurfaceView(this);
        m_view.setRenderer(new SimpleRenderer());
        setContentView(m_view);
    }
    @Override
    protected void onDestroy()
    {
        m_doc.Close();
        m_dib.Free();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pdfglview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
