package com.example.administrator.controltest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fxy.hbebt.HBEBT;
import com.fxy.hbebt.HBEBTListener;

@SuppressLint("HandlerLeak")
public class MainActivity extends AppCompatActivity implements HBEBTListener {

    // 记录当前commond
    public int setcommand = 9;

    SmartCARControllerView mView;
    HBEBT mBluetooth;
    int mSpeed;

    byte[] mBuff = new byte[100];
    int mBuffLen = 0;

    boolean mThreadRun;
    int mLastCMD = -1;
    int mLastSendCMD = -1;

    boolean mSensor, mUltra;

    boolean mAvoid = true;
/*

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SmartMessage.AVOID:
                    */
/*AvoidTask avoidTask = new AvoidTask();
                    Log.i("tag","avoid task order...");
                    avoidTask.run();*//*

                    mUltra = true;
                    sendSensor();
                    break;
                case SmartMessage.TOUCH:
                    mBluetooth.conntect();
                    break;
                case SmartMessage.DIRECT:
                    mLastCMD = msg.arg1;
                    break;
                case SmartMessage.SPEED:
                    mSpeed = msg.arg1;
                    break;
                case SmartMessage.SENSOR_ON:
                    Log.i("mmm","sensor on...");
                    mSensor = true;
                    sendSensor();
                    break;
                case SmartMessage.SENSOR_OFF:
                    Log.i("tag","sensor off...");
                    mSensor = false;
                    sendSensor();
                    break;
                case SmartMessage.ULTRA_ON:
                    mUltra = true;
                    sendSensor();
                    break;
                case SmartMessage.ULTRA_OFF:
                    mUltra = false;
                    sendSensor();
                    break;
            }
        }
    };

*/

    private void sendSensor() {
        byte[] cmd = SmartMessage.CMD_SENSOR.clone();
        if (mUltra)
            cmd[4] = 0x01;
        if (mSensor)
            cmd[5] = (byte) 0xFF;
        cmd[6] = getCheckSum(cmd);
        mBluetooth.sendData(cmd);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /*mView = new SmartCARControllerView(this, mHandler);
        setContentView(mView);*/
       setContentView( R.layout.activity_main );

        mBluetooth = new HBEBT(this);
        mBluetooth.setListener(this);

        /*mSensor = false;
        mUltra = false;*/
        mSensor = true;
        mUltra = true;
        sendSensor();
    }

    public void clickForward(View view){
        sendCarPacket( SmartMessage.TOP_CENTER );
    }
    public void clickBackward(View view){
        sendCarPacket( SmartMessage.BOTTOM_CENTER );
    }
    public void clickConnect(View view){
        mBluetooth.conntect();
    }
    public void clickLeft(View view){
        sendCarPacket( SmartMessage.LEFT );
    }
    public void clickRight(View view){
        sendCarPacket( SmartMessage.RIGHT );
    }
    public void clickStop(View view){
        mAvoid = false;
        sendCarPacket( SmartMessage.CENTER );
    }
    public void clickAvoid(View view){
        AvoidTask avoidTask = new AvoidTask();
        avoidTask.run();
    }

    @Override
    protected void onPause() {
        mThreadRun = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        mThreadRun = true;
        new ControlThread().start();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mBluetooth.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected() {
//        mView.setConnected();
        Toast.makeText( MainActivity.this,"连接成功",Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onConnecting() {
//        mView.setConnecting();
        Toast.makeText( MainActivity.this,"正在连接",Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onConnectionFailed() {
        Toast.makeText( MainActivity.this,"连接失败",Toast.LENGTH_SHORT ).show();
        mBluetooth.disconnect();
    }

    @Override
    public void onConnectionLost() {
        Toast.makeText( MainActivity.this,"找不到连接的蓝牙",Toast.LENGTH_SHORT ).show();
        mBluetooth.disconnect();
    }

    @Override
    public void onDisconnected() {
//        mView.setConnect();
    }

    public void setSensor(byte[] buff) {

        short acc_x, acc_y, acc_z;
        short gyro_x, gyro_y, gyro_z;
        int encoder_l, encoder_r;

        acc_x = (short) ((buff[5] & 0xFF));
        acc_x |= (short) ((buff[4] & 0xFF) << 8);
        acc_y = (short) ((buff[7] & 0xFF));
        acc_y |= (short) ((buff[6] & 0xFF) << 8);
        acc_z = (short) ((buff[9] & 0xFF));
        acc_z |= (short) ((buff[8] & 0xFF) << 8);
//        mView.setAccel(acc_x, acc_y, acc_z);

        gyro_x = (short) ((buff[11] & 0xFF));
        gyro_x |= (short) ((buff[10] & 0xFF) << 8);
        gyro_y = (short) ((buff[13] & 0xFF));
        gyro_y |= (short) ((buff[12] & 0xFF) << 8);
        gyro_z = (short) ((buff[15] & 0xFF));
        gyro_z |= (short) ((buff[14] & 0xFF) << 8);
//        mView.setGyro(gyro_x, gyro_y, gyro_z);

        encoder_l = (int) ((buff[17] & 0xFF));
        encoder_l |= (int) ((buff[16] & 0xFF) << 8);
        encoder_r = (int) ((buff[19] & 0xFF));
        encoder_r |= (int) ((buff[18] & 0xFF) << 8);
//        mView.setEncoder(encoder_l, encoder_r);

//        mView.setInfrared(buff[20]);
    }

    public void setUltra(byte[] buff) {

        mView.setUltra(buff[4] & 0xFF, buff[5] & 0xFF, buff[6] & 0xFF,
                buff[7] & 0xFF, buff[8] & 0xFF, buff[9] & 0xFF,
                buff[10] & 0xFF, buff[11] & 0xFF, buff[12] & 0xFF,
                buff[13] & 0xFF, buff[14] & 0xFF, buff[15] & 0xFF);
    }

    @Override
    public void onReceive(byte[] buffer) {

        for (int n = 0; n < buffer.length; n++) {
            byte buff = buffer[n];

            if (mBuffLen == 0 && buff != 0x76) {
                return;
            } else if (mBuffLen == 1 && buff != 0x00) {
                mBuffLen = 0;
                return;
            } else if (mBuffLen > 1 && buff == 0x00
                    && mBuff[mBuffLen - 1] == 0x76) {
                mBuffLen = 2;
                mBuff[0] = 0x76;
                mBuff[1] = 0x00;
            } else {
                mBuff[mBuffLen++] = buff;

                if (mBuffLen == 22 && mBuff[2] == 0x33) {
                    byte[] send = new byte[mBuffLen];

                    for (int i = 0; i < mBuffLen; i++) {
                        send[i] = mBuff[i];
                    }
                    /**
                     * 打包成Msg---
                     */
                    setSensor(send);
                    mBuffLen = 0;
                } else if (mBuffLen == 17 && mBuff[2] == 0x3C) {
                    byte[] send = new byte[mBuffLen];

                    for (int i = 0; i < mBuffLen; i++) {
                        send[i] = mBuff[i];
                    }

                    /**
                     * 这里打包成MsgUltrasonic 把这个禁掉不卡
                     */
//                    setUltra(send);
                    mBuffLen = 0;
                    // 在这里将声纳数据存入数据库，通过调用方法saveSensordata方法来实现
                    saveSensorData(buffer);
                }

            }
        }
    }

    /**
     * 测试数据库的方法
     *
     */
    /*
     * public void testsql() { SensorData sensorData = new SensorData();
	 * sensorData.setAngle(11); sensorData.setCommand(2);
	 * daoHelper.addSensorData(sensorData); }
	 */

    /**
     * 用于匹配声呐数据到数据库的方法
     */
    public void checkFuzzyData(byte[] buffer) {

    }
    /**
     * 保存模糊化的数据到数据库
     */

    /**
     * 用于保存声呐数据到数据库的方法
     */

    public void saveSensorData(byte[] buffer) {

    }

    public void sendCarPacket(int array) {
        byte[] cmd;

        switch (array) {
            case SmartMessage.TOP_LEFT:
                cmd = SmartMessage.CMD_FORWARD_LEFT.clone();
                setcommand = 1;
                cmd[5] = (byte) mSpeed;
                cmd[6] = getCheckSum(cmd);
                mBluetooth.sendData(cmd);
                break;
            case SmartMessage.TOP_CENTER:
                cmd = SmartMessage.CMD_FORWARD.clone();
                setcommand = 2;
                cmd[5] = (byte) mSpeed;
                cmd[6] = getCheckSum(cmd);
                mBluetooth.sendData(cmd);
                break;
            case SmartMessage.TOP_RIGHT:
                cmd = SmartMessage.CMD_FORWARD_RIGHT.clone();
                setcommand = 3;
                cmd[5] = (byte) mSpeed;
                cmd[6] = getCheckSum(cmd);
                mBluetooth.sendData(cmd);
                break;
            case SmartMessage.LEFT:
                cmd = SmartMessage.CMD_LEFT.clone();
                setcommand = 4;
                cmd[5] = (byte) mSpeed;
                cmd[6] = getCheckSum(cmd);
                mBluetooth.sendData(cmd);
                break;
            case SmartMessage.RIGHT:
                cmd = SmartMessage.CMD_RIGHT.clone();
                setcommand = 5;
                cmd[5] = (byte) mSpeed;
                cmd[6] = getCheckSum(cmd);
                mBluetooth.sendData(cmd);
                break;
            case SmartMessage.BOTTOM_LEFT:
                cmd = SmartMessage.CMD_BACKWARD_LEFT.clone();
                setcommand = 6;
                cmd[5] = (byte) mSpeed;
                cmd[6] = getCheckSum(cmd);
                mBluetooth.sendData(cmd);
                break;
            case SmartMessage.BOTTOM_CENTER:
                cmd = SmartMessage.CMD_BACKWARD.clone();
                setcommand = 7;
                cmd[5] = (byte) mSpeed;
                cmd[6] = getCheckSum(cmd);
                mBluetooth.sendData(cmd);
                break;
            case SmartMessage.BOTTOM_RIGHT:
                cmd = SmartMessage.CMD_BACKWARD_RIGHT.clone();
                setcommand = 8;
                cmd[5] = (byte) mSpeed;
                cmd[6] = getCheckSum(cmd);
                mBluetooth.sendData(cmd);
                break;
            case SmartMessage.CENTER:
                cmd = SmartMessage.CMD_STOP.clone();
                setcommand = 9;
                cmd[5] = 0;
                cmd[6] = getCheckSum(cmd);
                mBluetooth.sendData(cmd);
                break;
        }
    }

    public byte getCheckSum(byte[] buff) {
        int ret = (buff[2] & 0xFF) + (buff[4] & 0xFF) + (buff[5] & 0xFF);

        return (byte) ret;
    }

    class ControlThread extends Thread {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.i("tag","normal task...\n\n\n\n\n\n\n\n\n\n");
            while (mThreadRun) {
                Log.i("tag","normal task...");
                try {
                    if (mLastCMD != mLastSendCMD) {
                        mLastSendCMD = mLastCMD;
                        sendCarPacket(mLastSendCMD);
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }

    }

    public class AvoidTask extends Thread {
        public void run(){
            while (mAvoid) {
                try {
                    sendCarPacket( SmartMessage.TOP_CENTER );
                    Thread.sleep( 3000 );
                    sendCarPacket( SmartMessage.RIGHT );
                    Thread.sleep( 2000 );
                    sendCarPacket( SmartMessage.TOP_CENTER );
                    Thread.sleep( 3000 );
                    sendCarPacket( SmartMessage.RIGHT );
                    Thread.sleep( 2000 );
                    sendCarPacket( SmartMessage.TOP_CENTER );
                    Thread.sleep( 3000 );
                    sendCarPacket( SmartMessage.RIGHT );
                    Thread.sleep( 2000 );
                    sendCarPacket( SmartMessage.TOP_CENTER );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }
    }

}
