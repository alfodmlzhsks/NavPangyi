package com.noriter.sunghyun.navpangyi.Network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import static android.R.attr.start;

/**
 * Created by gugu on 2017-08-14.
 */

public class LoadManager {

    private URL url = null;
    private HttpURLConnection conn = null;
    private BufferedReader bis = null;
    private String data = "", params = "";
    private HashMap<String, String> reqRoad = null;
    static final String REQUEST_ROAD_URL = "https://apis.skplanetx.com/tmap/routes/pedestrian?version=1?";

    public LoadManager(HashMap<String, String> reqRoad) {
        this.reqRoad = reqRoad;
        double startX = Double.parseDouble(reqRoad.get("startX").toString());
        double startY = Double.parseDouble(reqRoad.get("startY").toString());
        double endX = Double.parseDouble(reqRoad.get("endX").toString());
        double endY = Double.parseDouble(reqRoad.get("endY").toString());
        String type = "WGS84GEO";
        String startName = reqRoad.get("startName").toString();
        String endName = reqRoad.get("endName").toString();

        params = "&startX="+startX+"&startY="+startY+"&endX="+endX+"&endY="+endY+"&reqCoordType="+type+"&resCoordType="+type+"&startName="+startName+"&endName="+endName;
        try {
            url = new URL(REQUEST_ROAD_URL+params);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("appKey", "162abb42-0997-3f66-bf86-402b6894c4f6");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String connect() {
        String line = "";

        try {
            conn.connect();

            InputStream is = conn.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            while((line=bis.readLine())!=null) {
                data+=line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bis!=null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }
}
