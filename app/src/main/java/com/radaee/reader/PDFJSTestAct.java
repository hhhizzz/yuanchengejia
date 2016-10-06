package com.radaee.reader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.radaee.pdf.Document;
import com.radaee.pdf.Global;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by radaee on 2015/9/23.
 */
public class PDFJSTestAct extends Activity
{
    private EditText m_text;
    private static int tmp_idx = 0;
    class PDFMyDel implements Document.PDFJSDelegate
    {
        @Override
        public void OnConsole(int cmd, String para) {
            switch(cmd)
            {
                case 0://clear
                    m_text.setText("");
                    break;
                case 1://hide
                    m_text.setVisibility(View.INVISIBLE);
                    break;
                case 2://println
                {
                    String val = m_text.getText().toString();
                    val += para;
                    val += "\r\n";
                    m_text.setText(val);
                }
                    break;
                case 3://show
                    m_text.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public int OnAlert(int btn, String msg, String title)
        {
            String txt = "Alert {title:\"" + title + "\",message:\"" + msg + "\",button:" + btn + ",return:1}\r\n";
            String val = m_text.getText().toString();
            val += txt;
            m_text.setText(val);
            return 1;
        }

        @Override
        public boolean OnDocClose()
        {
            return true;
        }

        @Override
        public String OnTmpFile() {
            tmp_idx++;
            return Global.tmp_path + "/" + tmp_idx + ".tmp";
        }

        @Override
        public void OnUncaughtException(int code, String msg) {
            String val = m_text.getText().toString();
            val += msg;
            val += "\r\n";
            m_text.setText(val);
        }
    }
    private void test_js(String path, String js)
    {
        Document doc = new Document();
        PDFMyDel del = new PDFMyDel();
        doc.Open(path, null);//no password
        doc.SetCache(del.OnTmpFile());
        try {
            doc.RunJS(js, del);
            doc.Save();
        }
        catch(Exception e)
        {
            String val = m_text.getText().toString();
            val += "Exception:" + e.getMessage();
            m_text.setText(val);
        }
        String val = doc.GetMeta("Author");
        doc.Close();
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //initialize PDF lib
        Global.Init(this);
        //initialize layout
        setContentView(R.layout.pdf_js);
        m_text = (EditText)this.findViewById(R.id.txt_console);
        m_text.setCursorVisible(false);
        m_text.setFocusable(false);
        m_text.setFocusableInTouchMode(false);

        try
        {
            InputStream is = getAssets().open("test.PDF");
            String save_path = Global.tmp_path + "/js_test.pdf";

            //delete PDF file first.
            File file = new File(save_path);
            file.delete();

            //then save asset file to storage.
            FileOutputStream os = new FileOutputStream(save_path);
            byte data[] = new byte[4096];
            int read;
            while((read = is.read(data)) > 0)
            {
                os.write(data,0, read);
            }
            os.close();
            is.close();

            String js = "app.alert({cMsg:\"Hello World\", cTitle:\"Testing\", nIcon: 1, nType: 1});";
            test_js(save_path, js);
            //test set author and save PDF file.
            js = "console.println(\"Author before set:\" + this.author);this.author='radaee';console.println(\"Author after set:\" + this.author);";
            test_js(save_path, js);
            //test add and set annotation.
            js = "var sqannot = this.addAnnot({type: \"Square\", rect: [10, 10, 110, 110], page: 1, width: 10, strokeColor: [\"RGB\", 0, 0, 1]});console.println(\"fill color before set:\" + sqannot.fillColor);sqannot.fillColor=[\"RGB\", 1, 0, 0];console.println(\"fill color after set:\" + sqannot.fillColor);console.println(\"page no is:\" + sqannot.page);sqannot.page = 2;console.println(\"page set to:\" + sqannot.page);console.println(\"rect before set:\" + sqannot.rect);sqannot.rect=[50, 50, 150, 150];console.println(\"rect after set:\" + sqannot.rect);";
            test_js(save_path, js);
            //test bookmarks
            js = "console.println(\"bookmark label:\" + this.bookmarkRoot.children[0].name);this.bookmarkRoot.children[0].name='123';console.println(\"after set:\" + this.bookmarkRoot.children[0].name);";
            test_js(save_path, js);

            //test Exception catch
            js = "try{app.alert();}catch(e){console.println(e);}";
            test_js(save_path, js);

            //test Uncaught Exception.
            js = "console.print('test');";
            test_js(save_path, js);
        }
        catch(Exception e)
        {
        }
    }
}