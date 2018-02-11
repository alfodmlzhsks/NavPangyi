package com.noriter.sunghyun.navpangyi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.noriter.sunghyun.navpangyi.DataBase.DBManager;

import java.util.ArrayList;


public class ActivityMembership extends Activity {

    private SpeechRecognizer mRecognizer = null;
    private TextToSpeech read = null;
    private String phoneNum = null;
    private Intent recog = null;
    private TextView edt_inputnum = null;
    private TextView edt_inputName = null;
    private boolean isInit = true;
    private String tempResult = null;
    private Intent real_open = null;
    private DBManager dbManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //가로, 세로 이거는 화면을 가로 세로로 돌렸을때 처리를 다르게 하기위함
        //가로일때는 그냥 빈화면만 나오게하며, 세로일때는 전체 화면이 나오게함
        //즉 세로화면에서 입력잘못했거나 오류발생시 화면 가로 -> 세로하면 다시 입력할수 있게하였음
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("ori", "가로");
            setContentView(R.layout.activity_membership);
        }
        else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("ori", "세로");
            setContentView(R.layout.activity_membership);

            ImageButton imgbtn_Singin = (ImageButton) findViewById(R.id.imb_SignIn1);

            edt_inputnum = (TextView) findViewById(R.id.edt_InputNum);
            edt_inputName = (TextView) findViewById(R.id.edt_InputName);
            read = new TextToSpeech(getApplicationContext(), onTTSInitListener); //TTS생성 생성자에 context, listener


            //여기부터
            recog = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recog.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            recog.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            //여기까지 STT를 위한 세팅. Intent임

            dbManager = new DBManager(this);
            dbManager.dbOpen(); //db오픈


            //전화번호는 자동완성
            //사용자 이름은 음성인식
            //이름 확인 후 확인버튼 자동 클릭


            //여기 try-catch는 사용자 전화번호 얻어오는 클래스이며 메소드임
            //getLine1Number가 전화번호 얻어오는 메소드
            try {
                TelephonyManager idenNum = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                phoneNum = idenNum.getLine1Number();
                Log.i("num", phoneNum);
            } catch (SecurityException e) {
                Log.i("error", "오류가 발생했습니다. 번호가져오기");
            }

            edt_inputnum.setText(phoneNum);
//        imgbtn_Singin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String phone = edt_inputnum.getText().toString();
//                String name = edt_inputName.getText().toString();
//
//                real_open = new Intent(getApplicationContext(), ActivityRealTimeInfo.class);
//                real_open.putExtra("phoneNum", phone);
//                real_open.putExtra("name", name);
//                startActivity(real_open);
//                finish();
//            }
//        });

            //STT의 리스너(Interface)정의임. 안드로이드 참조문서 참고
            RecognitionListener listener = new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                //onResults부분이 내가 말한후에 자동으로 실행되는 메소드 영역임
                @Override
                public void onResults(Bundle results) {

                    //여기부터
                    String key = "";
                    key = SpeechRecognizer.RESULTS_RECOGNITION;
                    ArrayList<String> mResults = results.getStringArrayList(key);
                    String[] rs = new String[mResults.size()];
                    mResults.toArray(rs);
                    //여기까지가 내가말한음성을 String으로 가져오는 부분이라 생각하면됨 그냥 복붙해도 무관
                    //즉 내가말한 음성은 mResults.get(0)하면 가져올 수 있음

                    //tempResult는 누구누구 맞는지 말하기위한 진입로의 열쇠
                    System.out.println(tempResult);
                    if (tempResult == null) {
                        tempResult = mResults.get(0);
                        edt_inputName.setText(tempResult);

                        runTTS(tempResult + ". 씨 맞습니까? ");
                    } else {
                        Log.i("isAvaliable", "예/아니요");

                        tempResult = mResults.get(0);
                        if(tempResult.equals("예") || tempResult.equals("에") || tempResult.equals("네") || tempResult.equals("네에") || tempResult.equals("예에") || tempResult.equals("넵") || tempResult.equals("네엡") || tempResult.equals("네네") || tempResult.equals("맞습니다") || tempResult.equals("맞아요") || tempResult.equals("맞아여") || tempResult.equals("맞습니다아")) {

                            String phone = edt_inputnum.getText().toString();
                            String name  = edt_inputName.getText().toString();

                            real_open = new Intent(getApplicationContext(), ActivityRealTimeInfo.class);
                            real_open.putExtra("phoneNum", phone);
                            real_open.putExtra("name", name);

                            //사용자 정보를 db에 넣고 db종료까지하고 intent로 화면전환
                            dbManager.insertDB("insert into information (phone, name) values('"+phone+"', '"+name+"');");
                            dbManager.dbClose();

                            startActivity(real_open);
                            finish();
                        }
                        else {
                            Log.i("isAvailable", "아니");
                            runTTS("음성신호가 끝난 후. 이름을 다시말씀해주세요");
                            tempResult = null;
                        }
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            };

            //STT생성 및 리스너 등록
            //STT는 SpeechRecognizer mRecognizer = new SpeechRecognizer()뭐 이러식으로 생성하는게 아님 생성자 안씀
            //이렇게 SpeechRecognizer클래스 내부에 static선언된 createSpeechRecognizer(Conetext context)메소드를 사용함
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
            mRecognizer.setRecognitionListener(listener);


//        voiceHandle.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                tryRecognize();
//            }
//        }, 6500);
        }
    }


    //액비티티 onDestroy시에 TTS종료, 꼭 해주기
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (read != null) {
            read.stop();
            read.shutdown();
        }
    }

    //얘는 TTS엔진 초기화하는애임, 이새끼때문에 얼마나개고생한건지
    //TTS엔진의 리스너임
    private TextToSpeech.OnInitListener onTTSInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int i) {
            isInit = (i == TextToSpeech.SUCCESS ? true : false);

            if (isInit) {
                Log.i("TTS", "성공");
                runTTS("환영합니다! 네팡이에 말씀하실때는. 음성신호가.. 들린후에 말씀해주세요.........처음 이용하시는군요.. 제 말이 끝난 후. 이름만 말씀해주세요");

                //setOnUtteranceProgressLister 얘는 TTS의 상태를 알수있는 인터페이스임
                //즉 TTS시작, 끝, 에러 상황에따라 내가 처리해주면됨
                //난 이거를 음성신호가 끝난 시점을 알고싶어서 사용했음
                read.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {

                    }

                    @Override
                    public void onDone(String s) {
                        Log.d("TTS_onDone", "끝남");


                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecognizer.startListening(recog); //내가 말할수 있는 상태가 되게 해주는 메소드
                                    }
                                });
                            }
                        }).start();
                        //얘는 main Thread에서밖에 호출불가능
                        //그러므로 runOnUiThread를 쓰레드로 호출시킴

                    }

                    @Override
                    public void onError(String s) {

                    }
                });
            } else {
                Log.i("TTS", "실패");
            }
        }
    };

    //runTTS는 내가 따로만든 메소드임
    public void runTTS(String con) {
        String content = con;
        String utteranceId = this.hashCode()+"";
//        HashMap<String, String> ttsMap = new HashMap<String, String>();
//        ttsMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "unique_id");
        read.speak(content, TextToSpeech.QUEUE_FLUSH, null, utteranceId); //얘가 폰에서 읽어주는 메소드
    }

    //권한 사용약관 화면인데 숨겨놨음
    public void personInformation(View v) {
        Uri uri = Uri.parse("http://calliharang.tistory.com/28");
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(i);
    }

}
