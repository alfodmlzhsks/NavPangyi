package com.noriter.sunghyun.navpangyi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class ActivityPermissionCheck extends AppCompatActivity {

    int iPerCount = 1;

    //내가만든 addPermission메소드에 인수로 넘길 권한이름들
    static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    static final String CALL_PHONE = Manifest.permission.CALL_PHONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_check);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //계속적으로 권한체크요청하는 다이얼로그임
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addPermission(READ_PHONE_STATE);
            addPermission(RECORD_AUDIO);
            addPermission(ACCESS_FINE_LOCATION);
            addPermission(CALL_PHONE);
        }
        System.out.println(""+iPerCount);
        if(iPerCount==17) {
            //권한체크 다되면 여기 실행됨
            Intent open_memberShip = new Intent(getBaseContext(), ActivityMembership.class);
            startActivity(open_memberShip);
            finish();
        }

    }

    //권한체크요청하는 메소드임
    private void addPermission(String permission) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, iPerCount);
            }
        }
        iPerCount++;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
