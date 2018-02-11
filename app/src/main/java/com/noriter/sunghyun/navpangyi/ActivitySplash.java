package com.noriter.sunghyun.navpangyi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.noriter.sunghyun.navpangyi.DataBase.DBManager;

public class ActivitySplash extends Activity{

    DBManager manager = null; //DB클래스 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Bundle save = null;
        manager = new DBManager(this);
        manager.dbOpen(); //db오픈

        Handler handler = new Handler(); //db에 사용자정보가 있는지없는지 판단후 화면전환위함

        //if문을 try-catch구문으로 사용했음
        //db에 정보없으면 어차피 RuntimeException이 발생하기때문임
        try{
            save = manager.selectDB("select * from information");
            Log.i("db", save.getString("phone")); //이거 로그캣 두개가있기 때문에 RuntimeException을 발생시킬수 있던거임
            Log.i("db", save.getString("name"));
            //db에 정보있으면 이거실행됨
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getBaseContext(), ActivityRealTimeInfo.class);

                    startActivity(intent);
                    finish();
                }
            }, 2000);
        }catch(RuntimeException e) {

            //db에 정보없으면 이거실행됨
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getBaseContext(), ActivityPermissionCheck.class);

                    startActivity(intent);
                    finish();
                }
            }, 2000);
        }
    }
}
