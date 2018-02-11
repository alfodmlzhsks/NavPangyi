package com.noriter.sunghyun.navpangyi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ActivityTest extends AppCompatActivity {

    View dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Uri uri = Uri.parse("daummaps://route?sp=37.537229,127.005515&ep=37.4979502,127.0276368&by=FOOT");

        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(i);
    }
}
