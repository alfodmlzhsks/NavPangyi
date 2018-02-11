package com.noriter.sunghyun.navpangyi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

public class ActivityRealTimeInfo extends Activity implements MapView.MapViewEventListener, MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener{

    private LocationManager lm = null;
    TextView tvInputLocation = null;
    private boolean isInit = true;
    private TextToSpeech read = null, pair = null;
    private double longitude = 0;
    private double latitude = 0;
    private String provider = null;
    private LinearLayout Map1 = null;
    private MapView mapView = null;
    private MapView.MapViewEventListener mapListener = null;
    private SpeechRecognizer mRecognizer = null;
    private Intent recog = null;
    private MapPOIItem poi = null;
    private MapReverseGeoCoder reverseGeoCoder = null;
    public static RealService mService = null;
    public static boolean mBound = false;
    public static boolean isUsed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_real_time_info);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //화면 세로고정

        Button imb_Navigation = (Button)findViewById(R.id.imb_Navigation);
        Button imb_BookMark = (Button)findViewById(R.id.imb_BookMark);
        Button imb_Help = (Button)findViewById(R.id.imb_Help);

        tvInputLocation = (TextView) findViewById(R.id.tvInputLocation);
        Map1 = (LinearLayout) findViewById(R.id.Map1);

        mapView = new MapView(this);
        mapView.setDaumMapApiKey("b431e2d4f3ca4c2cf87327d73f9471ae"); //다음 api사용하기위한 나만의 고유키, api사이트 참조
        mapView.setCurrentLocationEventListener(this);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
        mapView.setShowCurrentLocationMarker(true);
        mapView.setZoomLevel(0, true);
        Map1.addView(mapView);
        // -------------음성 말하기 읽기 설정
        read = new TextToSpeech(getApplicationContext(), onTTSInitListener); //얘는 한번 읽어주고 아무 역할도 안하는애로 하나생성한거고
        pair = new TextToSpeech(getApplicationContext(), onTTSInitListenerPair); //얘는 한번 읽어주고 대답을 받는애로 하나생성한거임

        //ActivityMembership 액티비티 참고
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
                Log.i("daum", nextKey);
                mService.setAlive(true);

                //이거 닐본, 닐번 이렇게한이유는
                //꼭 일번이라고 말한다고 그렇게 인식될거라는 보장이없으니까 예외처리해준느낌임
                if (nextKey.equals("닐본") || nextKey.equals("닐번") || nextKey.equals("1본") || nextKey.equals("1번") || nextKey.equals("일번") || nextKey.equals("일") || nextKey.equals("일본") || nextKey.equals("길찾기")) {
                    Map1.removeAllViews();
                    mapView = null;
                    mService.setAlive(false);
//                    unbindService(mConnection);
                    Intent navi_open = new Intent(getBaseContext(), ActivityNavigation.class);
                    //navigation액비비티에서도 지도 띄워주기위해서함. MapView는 모든 액티비티 통틀어서 하나밖에 안됨. 그니까 저거 메소드 이용해서 캐쉬지워줌
                    startActivity(navi_open);
                    finish();
                }
                else if(nextKey.equals("2번") || nextKey.equals("이번") || nextKey.equals("이본") || nextKey.equals("2본") || nextKey.equals("이") || nextKey.equals("2") || nextKey.equals("이벙") || nextKey.equals("즐겨찾기")) {
                    Map1.removeAllViews();
                    mapView = null;
                    Intent book_open = new Intent(getBaseContext(), ActivityBookMarks.class);
                    startActivity(book_open);
                    finish();
                }
                else if(nextKey.equals("3번") || nextKey.equals("삼번") || nextKey.equals("삼본") || nextKey.equals("도움말")){
                    runTTS("도움말.. 실시간 감지모드란. 보행시 장애물을 감지하여 알려드림으로써 안전한 보행을 유도합니다.. 길찾기란. 보행자용 길찾기를 구현하여 목적지를 말씀하시면 경로안내가 시작됩니다.. 즐겨찾기란. 길찾기의 목적지 입력전에 미리 경로명을 저장하여 사용할 수 있는 메뉴입니다..", read);
                }
                else {
                    mService.setAlive(true);
                }
//                else {
//                    if(!mService.getAlive()) {
//                        Intent i = new Intent(getBaseContext(), RealService.class);
//                        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
//                    }
//                }

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

        imb_BookMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(getApplicationContext(), ActivityBookMarks.class);
                Map1.removeAllViews();
                mapView = null;
                startActivity(intent1);
            }
        });

        imb_Navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(getApplicationContext(), ActivityNavigation.class);
                Map1.removeAllViews();
                mapView = null;
                startActivity(intent2);
            }
        });

        imb_Help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent3 = new Intent(getApplicationContext(), ActivityHelp.class);
                Map1.removeAllViews();
                mapView = null;
                startActivity(intent3);
            }
        });
//        getMyLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //바인드서비스 시작위한거임  (이 서비스는 라즈베리파이에서 실시간 감지값읽어오는 서비스임)
        //왜냐면 라즈베리에서 값 읽어! 라고한다음 그 값을 내가 받아와야하니까
        //일반 데몬서비스가아닌 바인드서비스를 사용함

        if(!isUsed) {
            mService.setAlive(true);
        }
        else {
            Intent i = new Intent(getBaseContext(), RealService.class);
            bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }
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
                Log.i("TTS", "성공");
                runTTS("실시간. 감지모드를 실행합니다!", read);
                read.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {

                    }

                    @Override
                    public void onDone(String s) {
                        Log.i("clear", "끝");
//                        if(!mService.getAlive()) {
//                            Intent i = new Intent(getBaseContext(), RealService.class);
//                            bindService(i, mConnection, Context.BIND_AUTO_CREATE);
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

            if (isInit) {
                Log.i("TTSPair", "성공");

                pair.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {
                        if(mBound) {
                            mService.setAlive(false);
                        }
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


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (read != null) {
            read.stop();
            read.shutdown();
        }
        if (pair != null) {
            pair.stop();
            pair.shutdown();
        }
    }

    //이거 직접구현할수도있는데 다음API의 인터페이스 구현한거임
    //실시간위치값이 mapView매개변수에 저장됨, 위치가 바뀔때마다 알아서 저장되는거임
    //콜백형식
    //여기부터 @Override된거는 다 다음API에서 제공하는 인터페이스임
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

        mapView.removeAllPOIItems();
        MapPoint.GeoCoordinate now = mapPoint.getMapPointGeoCoord();
        Log.i("현재", ""+now.latitude);
        Log.i("현재", ""+now.longitude);
        MapPoint current  = MapPoint.mapPointWithGeoCoord(now.latitude, now.longitude);

        mapView.setMapCenterPoint(current, true);

        poi = new MapPOIItem();
        poi.setItemName("현재위치");
        poi.setMapPoint(current);
        poi.setMarkerType(MapPOIItem.MarkerType.BluePin);

        mapView.addPOIItem(poi);

        reverseGeoCoder = new MapReverseGeoCoder("b431e2d4f3ca4c2cf87327d73f9471ae", current, this, this);

        //주소를 찾는데 오래걸릴수도있으니까 쓰레드돌려 백그라운드로 실행함
        new Thread(new Runnable() {
            @Override
            public void run() {
                reverseGeoCoder.startFindingAddress();
            }
        }).start();
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

//        if(mBound) {
//            mService.setAlive(false);
//            unbindService(mConnection);
//        }
        mService.setAlive(false);
        runTTS("네팡이 메뉴.. 일번. 길찾기.. 이번. 즐겨찾기.. 삼번. 도움말.. 번호를 말씀해주세요", pair);
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

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

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        tvInputLocation.setText(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        tvInputLocation.setText("주소 찾는중...");
    }
    //다음 API인터페이스 끝


    //바인드 서비스 코드
    public static ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RealService.RealServiceBinder binder = (RealService.RealServiceBinder)iBinder;
            mService = binder.getService();
            mBound = true;
            if(mService!=null) {
                Log.i("ttag","완료");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
            Log.i("ttag","실패");
        }
    };
}
