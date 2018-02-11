package com.noriter.sunghyun.navpangyi;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.IntDef;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by gugu on 2017-08-15.
 */

public class RealService extends Service {
    private final int REQUEST_BLUETOOTH_ENABLE = 100;
    private TextToSpeech read = null;
    private TextView tv_connect;
    private TextView tv_receive;
    private String old = "13";
    private boolean run = true;

    ConnectedTask mConnectedTask = null;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    //private ArrayAdapter<String> mConversationArrayAdapter;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";
    private String receive_str = null;
    private String tempData = null;
    private boolean isInit = true;
    private int choumpa1Count = 0;
    private int choumpa2Count = 0;


    Thread mWorkerThread = null;
    byte[] readBuffer;
    int readBufferPosition;
    String mStrDelimiter = "\n";
    char mCharDelimiter = '\n';

    private final IBinder mBinder = new RealServiceBinder();

    public class RealServiceBinder extends Binder {
        RealService getService() {
            return RealService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        read = new TextToSpeech(getApplicationContext(), onTTSInitListener);
        Log.d(TAG, "Initalizing Bluetooth adapter...");
        //1.블루투스 사용 가능한지 검사합니다.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "This device is not implement Bluetooth.", Toast.LENGTH_LONG).show();
            return;
        }


//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
//        }
        Log.d(TAG, "Initialisation successful.");

        //2. 페어링 되어 있는 블루투스 장치들의 목록을 보여줍니다.
        //3. 목록에서 블루투스 장치를 선택하면 선택한 디바이스를 인자로 하여
        //   doConnect 함수가 호출됩니다.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    //    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//
//        return START_REDELIVER_INTENT;
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        showPairedDevicesListDialog();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (run) {
//                    Log.i("thread", "스레드");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
        return mBinder;
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

                read.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {

                    }

                    @Override
                    public void onDone(String s) {
                        Log.i("clear", "끝");
                    }

                    @Override
                    public void onError(String s) {

                    }
                });
            }
        }
    };

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName(); //상대방 기기 이름

            //SPP
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d(TAG, "create socket for " + mConnectedDeviceName);

            } catch (IOException e) {
                Log.e(TAG, "socket create failed " + e.getMessage());
            }

//            tv_connect.setText("connecting...");
        }


        //앱을 백그라운드로 했을 경우
        @Override
        protected Boolean doInBackground(Void... params) {

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();
            runTTS("연결중", read);
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mBluetoothSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " +
                            " socket during connection failure", e2);
                }

                return false;
            }

            return true;
        }


        //연결 완료 됬을 때 실행한다.
        @Override
        protected void onPostExecute(Boolean isSucess) {

            if (isSucess) {
                connected(mBluetoothSocket);
            } else {

                isConnectionError = true;
                Log.d(TAG, "Unable to connect device");
            }
        }
    }

    public void connected(BluetoothSocket socket) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.beginListenForData();
        mWorkerThread.start();
    }

    private class ConnectedTask {

        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket) {

            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "socket not created", e);
            }

            Log.d(TAG, "connected to " + mConnectedDeviceName);
//            tv_connect.setText("connected to " + mConnectedDeviceName);
        }


        //문자열 수신 쓰레드
        void beginListenForData() {
            final Handler handler = new Handler();

            readBufferPosition = 0;                 // 버퍼 내 수신 문자 저장 위치.
            readBuffer = new byte[1024];            // 수신 버퍼.

            // 문자열 수신 쓰레드.
            mWorkerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // interrupt() 메소드를 이용 스레드를 종료시키는 예제이다.
                    // interrupt() 메소드는 하던 일을 멈추는 메소드이다.
                    // isInterrupted() 메소드를 사용하여 멈추었을 경우 반복문을 나가서 스레드가 종료하게 된다.
                    while ((!Thread.currentThread().isInterrupted())) {
                        try {
                            while(run) {
                                // InputStream.available() : 다른 스레드에서 blocking 하기 전까지 읽은 수 있는 문자열 개수를 반환함.
                                int byteAvailable = mInputStream.available();   // 수신 데이터 확인
                                if (byteAvailable > 0) {                        // 데이터가 수신된 경우.
                                    byte[] packetBytes = new byte[byteAvailable];
                                    // read(buf[]) : 입력스트림에서 buf[] 크기만큼 읽어서 저장 없을 경우에 -1 리턴.
                                    mInputStream.read(packetBytes);
                                    for (int i = 0; i < byteAvailable; i++) {
                                        byte b = packetBytes[i];
                                        if (b == mCharDelimiter) {
                                            byte[] encodedBytes = new byte[readBufferPosition];
                                            //  System.arraycopy(복사할 배열, 복사시작점, 복사된 배열, 붙이기 시작점, 복사할 개수)
                                            //  readBuffer 배열을 처음 부터 끝까지 encodedBytes 배열로 복사.
                                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                            final String data = new String(encodedBytes, "US-ASCII");
                                            Log.i("라즈베리에서 넘어온 센서값", data);
                                            final String[] tTemp = data.split(",");
                                            readBufferPosition = 0;
                                            Log.i("초음파1", tTemp[0]);
                                            Log.i("초음파2", tTemp[1]);
                                            Log.i("인체열감지", tTemp[2]);

                                            handler.post(new Runnable() {
                                                // 수신된 문자열 데이터에 대한 처리.
                                                //네팡이 값읽고 판단해서 말하는 부분
                                                @Override
                                                public void run() {
                                                    int choumpa1 = Integer.parseInt(tTemp[0]);
                                                    int choumpa2 = Integer.parseInt(tTemp[1]);
                                                    int human = Integer.parseInt(tTemp[2].trim());

                                                    if(choumpa1>150) {
                                                        choumpa1=200;
                                                    }
                                                    if(choumpa2>150) {
                                                        choumpa2 =200;
                                                    }

                                                    if(((choumpa1<150) || (choumpa2<150))) {
                                                        if(choumpa1<150) {
                                                            runTTS("전방에 장애물이 있습니다", read);
                                                            choumpa1Count++;
                                                        }
                                                        else if(choumpa2<150) {
                                                            choumpa2Count++;
                                                            runTTS("위쪽에 장애물이 있습니다", read);
                                                        }
                                                    }


                                                    // mStrDelimiter = '\n';
//                                                tv_receive.setText(data);
//                                                callback.sendData(data);



                                                }
                                            });
                                        } else {
                                            readBuffer[readBufferPosition++] = b;
                                        }
                                    }
                                }
                            }

                        } catch (Exception e) {    // 데이터 수신 중 오류 발생.
                            Toast.makeText(getApplicationContext(), "데이터 수신 중 오류가 발생 했습니다.", Toast.LENGTH_LONG).show();
//                            finish();            // App 종료.
                        }
                    }
                }
            });

        }

        void closeSocket() {

            try {

                mBluetoothSocket.close();
                Log.d(TAG, "close socket()");

            } catch (IOException e2) {

                Log.e(TAG, "unable to close() " +
                        " socket during connection failure", e2);
            }
        }


        //데이터를 쓰다.
        void write(String msg) {

            msg += "\n";
//            tv_receive.setText(msg);
            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during send", e);
            }

            //mInputEditText.setText(" "); //??
        }

    }

    @Override
    public void onDestroy() {
        Thread.interrupted();
        super.onDestroy();
    }

    public void showPairedDevicesListDialog() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if (pairedDevices.length == 0) {
            Toast.makeText(this, "페어링을 먼저해주세요!", Toast.LENGTH_LONG).show();
            return;
        }

        String[] items;
        items = new String[pairedDevices.length];
        for (int i = 0; i < pairedDevices.length; i++) {
            items[i] = pairedDevices[i].getName();
        }
        ConnectTask task = new ConnectTask(pairedDevices[0]);
        task.execute();

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Select device");
//        builder.setCancelable(false);
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//
////                 Attempt to connect to the device
////                ConnectTask task = new ConnectTask(pairedDevices[which]);
////                task.execute();
//            }
//        });
//        builder.create().show();
    }

    public void setAlive(boolean alive) {
        run = alive;
    }

    public boolean getAlive() {
        return run;
    }
}