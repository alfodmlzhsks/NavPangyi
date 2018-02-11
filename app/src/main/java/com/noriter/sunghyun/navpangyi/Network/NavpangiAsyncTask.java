package com.noriter.sunghyun.navpangyi.Network;

import android.os.AsyncTask;
import android.util.Log;

import com.noriter.sunghyun.navpangyi.ActivityNavigation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by gugu on 2017-08-14.
 */

public class NavpangiAsyncTask extends AsyncTask<String, Integer, String> {

    private LoadManager load = null;
    private String startRoad = null;
    private int jsonCount = 0;
    private ActivityNavigation navigation = null;

    public NavpangiAsyncTask(HashMap<String, String> reqRoad, ActivityNavigation navigation) {
        load = new LoadManager(reqRoad);
        this.navigation = navigation;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


    }

    @Override
    protected String doInBackground(String... strings) {

        startRoad = load.connect();
        Log.i("json결과", startRoad);

        return startRoad;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONObject total = new JSONObject(s);
            System.out.println(s);
            JSONArray features = total.getJSONArray("features");
            navigation.setRoad(features);

            Log.i("구구구구구구", "" + features);
        } catch (JSONException e) {
            Log.e("json_error", "보행자 경로요청 에러");
        }


    }
}
