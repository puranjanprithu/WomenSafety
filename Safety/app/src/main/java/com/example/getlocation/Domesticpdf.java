package com.example.getlocation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class Domesticpdf extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domesticpdf);

        webView = (WebView) findViewById(R.id.webpdfView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl("https://drive.google.com/file/d/1u-Uh7azxR_strSJzmQuiDb6h3VwsaIc_/view?usp=sharing") ;

    }
}