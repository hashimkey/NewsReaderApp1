package com.example.abdulqani.newsreaderapp1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ArticleActivity extends AppCompatActivity {

    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);


        WebView webView = (WebView) findViewById(R.id.webview);

        progressBar = ProgressDialog.show(this,"loading","please wait");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setDefaultTextEncodingName("utf-8");

        Intent intent = getIntent();
       webView.loadData(intent.getStringExtra("content"), "text/html","charset=UTF-8");


        //webView.getSettings().setDefaultTextEncodingName("utf-8");
        //webView.loadDataWithBaseURL(null, intent, "text/html", "utf-8", null);


    }
    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            if (progressBar != null && progressBar.isShowing()) {
                progressBar.dismiss();
            }
        }
    }

}
