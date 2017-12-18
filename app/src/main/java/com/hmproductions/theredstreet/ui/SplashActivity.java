package com.hmproductions.theredstreet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hmproductions.theredstreet.ui.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
