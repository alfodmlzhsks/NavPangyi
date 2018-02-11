package com.noriter.sunghyun.navpangyi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.noriter.sunghyun.navpangyi.Network.NavpangiAsyncTask;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.noriter.sunghyun.navpangyi.ActivityRealTimeInfo.mService;

public class ActivityNavigation extends Activity implements MapView.MapViewEventListener, MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    private TextToSpeech read = null, pair = null;
    private SpeechRecognizer mRecognizer = null;
    private boolean isInit = true;
    private Intent recog = null;
    private String tempResult = null;
    private String prevResult = null;
    private Location location = null;
    private TextView tvInputHere = null;
    private LinearLayout map = null;
    private MapView mapView = null;
    private MapPOIItem poi = null;
    private MapPOIItem ePoi = null;
    private MapReverseGeoCoder reverseGeoCoder = null;
    private TextView tvInputDistance = null, tvInputDestination = null;
    private MapPoint ePoint = null;
    private Location temp = null;
    private NavpangiAsyncTask task = null;
    private HashMap<String, String> reqRoad = null;
    public JSONArray road = null;
    private boolean isRoadEmpty = true;
    private int distance = 0;
    private int prevDistance = 0;
    private ArrayList<MapPOIItem> naviPOI = null;
    private ArrayList<String> ttsFull = null;
    private ArrayList<JSONObject> pointJSON = null;
    private ArrayList<JSONObject> stringJSON = null;
    private MapPolyline poly = null;
    private boolean isOnlyOne = false;
    private int iNextTTSCount = 0;
    private boolean isSumDistanceSpeak = true;
    static int poiCount = 10;
    private boolean isPangTaxi = true;
    private boolean isCheck = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_navigation);
        tvInputDistance = (TextView) findViewById(R.id.tvInputDistance);
        tvInputDestination = (TextView) findViewById(R.id.tvInputDestination);
        tvInputHere = (TextView) findViewById(R.id.tvInputHere);

        reqRoad = new HashMap<String, String>();

        map = (LinearLayout) findViewById(R.id.Map2);

        mapView = new MapView(this);
        mapView.setDaumMapApiKey("b431e2d4f3ca4c2cf87327d73f9471ae");
        mapView.setCurrentLocationEventListener(this);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
        mapView.setShowCurrentLocationMarker(true);
        mapView.setZoomLevel(1, true);
        mapView.fitMapViewAreaToShowAllPOIItems();
        map.addView(mapView);

        read = new TextToSpeech(getApplicationContext(), onTTSInitListener);
        pair = new TextToSpeech(getApplicationContext(), onTTSInitListenerPair);

        recog = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recog.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recog.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        RecognitionListener recognitionListener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                String key = "";
                key = SpeechRecognizer.RESULTS_RECOGNITION;
                ArrayList<String> mResults = bundle.getStringArrayList(key);
                String[] rs = new String[mResults.size()];
                mResults.toArray(rs);
                String nextKey = mResults.get(0).toString();
//                mService.setAlive(true);

//                if (distance >= 3000 && (nextKey.equals("예") || nextKey.equals("에") || nextKey.equals("네") || nextKey.equals("넵"))) {
//                    isPangTaxi = true;
//                } else if (distance >= 3000 && (nextKey.equals("아니어") || nextKey.equals("아니요") || nextKey.equals("아니여") || nextKey.equals("아니오"))) {
//                    isPangTaxi = false;
//                }
                try {
                    if (nextKey.equals("닐본") || nextKey.equals("닐번") || nextKey.equals("1본") || nextKey.equals("1번") || nextKey.equals("일번") || nextKey.equals("일") || nextKey.equals("일본") || nextKey.equals("길찾기")) {
                        map.removeAllViews();
                        mapView = null;
                        mService.setAlive(false);
                        ActivityRealTimeInfo.isUsed = false;
                        Intent back = new Intent(getBaseContext(), ActivityRealTimeInfo.class);
                        startActivity(back);
                        finish();
                    } else if (nextKey.equals("2번") || nextKey.equals("이번") || nextKey.equals("이본") || nextKey.equals("2본") || nextKey.equals("이") || nextKey.equals("2") || nextKey.equals("이벙") || nextKey.equals("즐겨찾기")) {
                        //가까운 봉사단체나, 검증된 개인차량에 연결
                        Intent intent_call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:010-5933-8498"));
                        try {
                            startActivity(intent_call);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }

                    } else if (tempResult == null) {
                        tempResult = mResults.get(0).toString();
                        prevResult = tempResult.replaceAll(" ", "");

                        runTTS(tempResult + ". 맞습니까? ", pair);
//                        mService.setAlive(false);
                    } else if (tempResult != null) {
                        Log.i("isAvaliable", "예/아니요");
                        tempResult = mResults.get(0);

//                        mService.setAlive(true);

                        System.out.println(prevResult);
                        tvInputDestination.setText(prevResult);
                        if ((tempResult.equals("예") || tempResult.equals("에") || tempResult.equals("네") || tempResult.equals("넵")) && isPangTaxi) {
                            //이제 여기다가 길찾기 시작하는 코드적으면됨
//                            mService.setAlive(false);
                            temp = showPoint(prevResult); //내장 Location클래스
                            Log.i("gtecc", "" + temp.getLatitude());
                            Log.i("gtecc", "" + temp.getLongitude());

                            ePoint = MapPoint.mapPointWithGeoCoord(temp.getLatitude(), temp.getLongitude());
                            ePoi = new MapPOIItem();
                            ePoi.setItemName("도착");
                            ePoi.setTag(1);
                            ePoi.setMarkerType(MapPOIItem.MarkerType.RedPin);
                            ePoi.setMapPoint(ePoint);


                            isRoadEmpty = true;

                            //TMap의 REST API를 사용하기위해 매개변수 설정해서 인터넷연결시도하는 코드
                            reqRoad.put("startX", "" + location.getLongitude());
                            reqRoad.put("startY", "" + location.getLatitude());
                            reqRoad.put("endX", "" + temp.getLongitude());
                            reqRoad.put("endY", "" + temp.getLatitude());
                            reqRoad.put("startName", "now");
                            reqRoad.put("endName", "destination");

                            //인터넷 연결 시도
                            connectCheck(reqRoad);

                        }
                        if (distance >= 3000 && (nextKey.equals("예") || nextKey.equals("에") || nextKey.equals("네") || nextKey.equals("넵")) && !isPangTaxi) {
                            isPangTaxi = true;
                            Intent intent_call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:010-2594-3341"));
                            try {
                                startActivity(intent_call);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                        } else if (distance >= 3000 && (nextKey.equals("아니어") || nextKey.equals("아니요") || nextKey.equals("아니여") || nextKey.equals("아니오")) && !isPangTaxi) {
                            isPangTaxi = true;
                            isCheck = true;
                            isOnlyOne = true;
                            //runTTS("총 거리 " + distance + "미터. 안내를 시작합니다....", read);
                        } else if((nextKey.equals("아니어") || nextKey.equals("아니요") || nextKey.equals("아니여") || nextKey.equals("아니오"))) {
                            Log.i("isAvailable", "아니");
                            runTTS("음성신호가 끝난 후. 목적지를 다시말씀해주세요", pair);
                            tempResult = null;
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    runTTS("장소가 존재하지 않습니다. 다시 말씀해주세요", pair);
                    tempResult = null;
                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        };
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        mRecognizer.setRecognitionListener(recognitionListener);

    }


    @Override
    protected void onStart() {
        super.onStart();

        mService.setAlive(true);
        //바인드서비스 시작위한거임  (이 서비스는 라즈베리파이에서 실시간 감지값읽어오는 서비스임)
        //왜냐면 라즈베리에서 값 읽어! 라고한다음 그 값을 내가 받아와야하니까
        //일반 데몬서비스가아닌 바인드서비스를 사용함
//        Intent i = new Intent(getBaseContext(), RealService.class);
//        bindService(i, ActivityRealTimeInfo.mConnection, Context.BIND_AUTO_CREATE);
    }

    private void runTTS(String con, TextToSpeech common) {
        String content = con;
        String utteranceId = this.hashCode() + "";


        common.speak(content, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private TextToSpeech.OnInitListener onTTSInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int i) {
            isInit = (i == TextToSpeech.SUCCESS) ? true : false;

            if (isInit) {
                Log.i("TTSS", "성공");
                runTTS("길찾기 모드를 실행합니다. 화면 터치후 말씀해주세요", read);

                read.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {

                    }

                    @Override
                    public void onDone(String s) {
                        Log.i("1500", "천오백");
//                        if(!mBound) {
//                            Intent i = new Intent(getBaseContext(), RealService.class);
//                            bindService(i, mConnection, Context.BIND_AUTO_CREATE);
//                        }

                        if (isOnlyOne && !isSumDistanceSpeak) {
                            String tt = null;
                            tt = ttsFull.get(0);
                            runTTS(tt + "하세요.", read);
                            mService.setAlive(true);
                            isOnlyOne = false;
                        }
//                        if(!mService.getAlive()) {
////                            mService.setAlive(true);
//                        }


                    }

                    @Override
                    public void onError(String s) {

                    }
                });
            }
        }
    };

    private TextToSpeech.OnInitListener onTTSInitListenerPair = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int i) {
            isInit = (i == TextToSpeech.SUCCESS) ? true : false;
            Log.i("init", "" + isInit);
            if (isInit) {
                Log.i("TTSPair", "성공");

//                runTTS("길찾기 모드를 실행합니다.. 목적지를 말씀해주세요", pair);

                //이거 리스너는 onCreate로 빼도됨
                pair.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {
//                        if(mBound) {
//                            mService.setAlive(false);
//                        }
                    }

                    @Override
                    public void onDone(String s) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecognizer.startListening(recog);
                                    }
                                });
                            }
                        }).start();
                    }

                    @Override
                    public void onError(String s) {

                    }
                });
            }
        }
    };

    private Location showPoint(String addr) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> nowAddress = null;
        Location location = new Location("");
        try {
            nowAddress = geocoder.getFromLocationName(addr, 3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (nowAddress != null) {
            Address address = nowAddress.get(0);
            double latitude = address.getLatitude();
            double longitude = address.getLongitude();

            location.setLatitude(latitude);
            location.setLongitude(longitude);
        }

        return location;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (read != null) {
            read.stop();
            read.shutdown();
        }
        map.removeAllViews();
    }

    //------------------현재위치 리스너시작
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        location = new Location("현재위치");

        if ((road != null) && isRoadEmpty) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    introduceRoad(road);
                    isRoadEmpty = false;
                }
            }).start();

        }
        mapView.removeAllPOIItems();
        mapView.removeAllPolylines();
        MapPoint.GeoCoordinate now = mapPoint.getMapPointGeoCoord();
        Log.i("현재", "" + now.latitude);
        Log.i("현재", "" + now.longitude);
        MapPoint current = MapPoint.mapPointWithGeoCoord(now.latitude, now.longitude);

        mapView.setMapCenterPoint(current, true);

        location.setLatitude(now.latitude);
        location.setLongitude(now.longitude);


        poi = new MapPOIItem();
        poi.setItemName("현재위치");
        poi.setTag(0);
        poi.setMapPoint(current);
        poi.setMarkerType(MapPOIItem.MarkerType.BluePin);
        mapView.addPOIItem(poi);
        if (ePoi != null) {
            mapView.addPOIItem(ePoi);
        }
        if (naviPOI != null) { //경로안내시작했다는 뜻임

            mapView.addPolyline(poly);
            Log.i("tts", "" + ttsFull.size());
            Log.i("거리", "" + distance);
            Log.i("prev", ""+prevDistance);
            if((prevDistance-3)>distance) {
                runTTS(distance +"미터 남았습니다.", read);
                prevDistance = distance;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    distance = (int) location.distanceTo(temp);
                    tvInputDistance.setText("" + distance + "m");
                }
            });

            if(isSumDistanceSpeak && isCheck) {
                prevDistance = distance;
                isSumDistanceSpeak = false;
                mService.setAlive(false);
                Log.i("aa", "asd");
                runTTS("총 거리 " + distance + "미터. 안내를 시작합니다....", read);
            }
            if (ttsFull != null || distance != 0) {
                if ((ttsFull.size() == pointJSON.size()) && (iNextTTSCount < pointJSON.size())) {

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            distance = (int) location.distanceTo(temp);
//                            tvInputDistance.setText("" + distance + "m");
//                        }
//                    });
                    final Location glocation = new Location("경유");
                    Log.i("navTag", "확인");
                    try {
                        glocation.setLatitude(pointJSON.get(iNextTTSCount).getJSONObject("geometry").getJSONArray("coordinates").getDouble(1));
                        glocation.setLongitude(pointJSON.get(iNextTTSCount).getJSONObject("geometry").getJSONArray("coordinates").getDouble(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ((int) location.distanceTo(glocation) <= 5) {
                                        runTTS(ttsFull.get(iNextTTSCount), read);
                                    }
                                }
                            });
                        }
                    }).start();
                    iNextTTSCount++;
                }
            }
        }


        reverseGeoCoder = new MapReverseGeoCoder("b431e2d4f3ca4c2cf87327d73f9471ae", current, this, this);


        new Thread(new Runnable() {
            @Override
            public void run() {
                reverseGeoCoder.startFindingAddress();
            }
        }).start();
//            center.setItemName("중간");
//            center.setMapPoint(centerPoint);
//            center.setMarkerType(MapPOIItem.MarkerType.YellowPin);
    }


    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }
    //------------현재위치 리스너 끝

    //----------맵뷰 리스너 시작

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        Log.i("터치", "touch");
        mService.setAlive(false);
        tempResult = null;
        runTTS("목적지를 말씀해주세요", pair);
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
        mService.setAlive(false);
        runTTS("네팡이 메뉴.. 일번. 실시간감지.. 이번. 팡택시..  번호를 말씀해주세요", pair);
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }
    // -----------------맵뷰 리스너 끝

    //주소변환리스너 시작


    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        tvInputHere.setText(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        tvInputHere.setText("주소 찾는중...");
    }

    //주소변환리스너 끝

    private void connectCheck(HashMap<String, String> reqRoad) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if ((networkInfo != null) && networkInfo.isConnected()) {
            task = new NavpangiAsyncTask(reqRoad, this);
            task.execute("");
        } else {
            Toast.makeText(getBaseContext(), "인터넷 연결상태를 확인해주세요", Toast.LENGTH_LONG).show();
        }

    }

    public void setRoad(JSONArray road) {
        this.road = road;
    }

    private void introduceRoad(final JSONArray road) {
        int tCount = 0;
        int roadCount = road.length();
        pointJSON = new ArrayList<JSONObject>();
        stringJSON = new ArrayList<JSONObject>();
        naviPOI = new ArrayList<MapPOIItem>();
        ttsFull = new ArrayList<String>();
        poly = new MapPolyline();

        try {
            for (tCount = 0; tCount < roadCount; tCount++) {
                JSONObject tObject = road.getJSONObject(tCount);
                JSONObject gObject = tObject.getJSONObject("geometry");

                if (gObject.getString("type").equals("Point")) {
                    pointJSON.add(tObject);
                } else if (gObject.getString("type").equals("LineString")) {
                    stringJSON.add(tObject);
                }
            }

            Log.i("정렬완료", "comple");
            Log.i("포인트", "" + pointJSON.size());
            Log.i("스트링", "" + stringJSON.size());

            //미터 안내
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        distance = Integer.parseInt(pointJSON.get(0).getJSONObject("properties").getString("totalDistance"));
                        tvInputDistance.setText("" + distance + "m");
                        //isOnlyOne = true;
//                        runTTS("총 거리 " + distance + "미터. 안내를 시작합니다....", read);

                        if (isPangTaxi) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (distance >= 3000) {
                                        runTTS(prevResult+"까지는 거리가 멉니다.. 팡택시를 이용하시겠습니까?", pair);
                                    }
                                    isPangTaxi = false;
                                }
                            }).start();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            //poi찍기

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int sCount = 0; sCount < stringJSON.size(); sCount++) {
                        try {
                            JSONArray temp = stringJSON.get(sCount).getJSONObject("geometry").getJSONArray("coordinates");

                            for (int ssCount = 0; ssCount < temp.length(); ssCount++) {
                                MapPoint point = MapPoint.mapPointWithGeoCoord(temp.getJSONArray(ssCount).getDouble(1), temp.getJSONArray(ssCount).getDouble(0));
                                naviPOI.add(createPOI(point));

                                poly.addPoint(point);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int sCount = 0; sCount < pointJSON.size(); sCount++) {
                        try {
                            JSONObject temp = pointJSON.get(sCount).getJSONObject("properties");

                            ttsFull.add(temp.getString("description"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            Log.i("roadVoice", "음성저장완료");
            Log.i("coor", "" + pointJSON.size());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private MapPOIItem createPOI(MapPoint point) {
        MapPOIItem poi = new MapPOIItem();
        poi.setMapPoint(point);
        poi.setMarkerType(MapPOIItem.MarkerType.YellowPin);
        poi.setItemName("경유" + poiCount);
        poi.setTag(poiCount);
        poiCount++;

        return poi;
    }

//    private ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//            RealService.RealServiceBinder binder = (RealService.RealServiceBinder)iBinder;
//            mService = binder.getService();
//            mBound = true;
//            if(mService!=null) {
//                Log.i("tttag","완료");
//            }
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBound = false;
//            Log.i("ttag","실패");
//        }
//    };


    @Override
    public void onBackPressed() {
        map.removeAllViews();
        this.mapView = null;
        mService.setAlive(false);
        ActivityRealTimeInfo.isUsed = false;
        Intent back = new Intent(getBaseContext(), ActivityRealTimeInfo.class);
        startActivity(back);
        finish();
    }
}
