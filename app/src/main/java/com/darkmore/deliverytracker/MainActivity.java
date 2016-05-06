package com.darkmore.deliverytracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private ImageView mImgView;
    private EditText editText;
    private Bitmap bitmap;
    private Button sendButtom;
    private String sessionIdKeeper;
    private static final int MSG_UPLOAD_OK= 0x00000001;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendButtom = (Button)findViewById(R.id.checkButton);
        editText = (EditText)findViewById(R.id.editText);
        mWebView = (WebView) findViewById(R.id.webView);
        mImgView = (ImageView)findViewById(R.id.imageView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.requestFocus();
        mWebView.setWebViewClient(new MyWebViewClient());
        final String  url = "http://postserv.post.gov.tw/webpost/CSController";
        Thread captchaGetter = new Thread(new Runnable()
        {
            public void run(){
                try {
                    String  postUrl = "http://postserv.post.gov.tw/webpost/CSController?cmd=POS4001_1&_ACTIVE_ID=190";
                    Connection conn = Jsoup.connect(postUrl);

                    Connection.Response res = conn
                            .method(Connection.Method.GET)
                            .timeout(10000)
                            .execute();

                    String sessionID = res.cookie("JSESSIONID");
                    sessionIdKeeper = sessionID;
                    Log.d("dtest",sessionID);


                    Document document = conn.get();
                    Element captcha = document.select("img#checkcodeimg").first();
                    Date date = new Date();
                    String imgChar = "http://postserv.post.gov.tw"+captcha.attr("src")+String.valueOf(date.getTime());
                    Log.d("dtest",imgChar);

                    InputStream input = new java.net.URL(imgChar).openStream();
                    bitmap = BitmapFactory.decodeStream(input);
                    if (captcha == null) {
                        throw new RuntimeException("Unable to find captcha...");
                    }

                    input.close();

                    mUI_Handler.sendEmptyMessage(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        captchaGetter.start();
        sendButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postData = url+"?cmd=POS4001_2&sid="+sessionIdKeeper+"&MAILNO1=00000000000000&MAILNO2=&MAILNO3=&MAILNO4=&MAILNO5=&j_captcha_response="+editText.getText();
                Log.d("dtest",postData);
                try {
                    //mWebView.postUrl(url,URLEncoder.encode(postData, "UTF-8").getBytes());
                    mWebView.loadUrl(postData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }



    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
    private Handler mUI_Handler = new Handler()

    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    mImgView.setImageBitmap(bitmap);
                    break;
            }
        }
    };
}
