package com.example.place.woofinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ControlActivity extends AppCompatActivity {
    //서버주서
    public static final String sIP = "192.168.0.122";
    //사용할 통신 포트
    public static final int sPORT = 111;

    //데이터 보낼 클랙스
    public SendData mSendData = null;

    class SendData extends Thread{
        String data;
        public SendData(String data){
            this.data = data;
        }
        public void run(){
            try{
                //UDP 통신용 소켓 생성
                DatagramSocket socket = new DatagramSocket();
                //서버 주소 변수
                InetAddress serverAddr = InetAddress.getByName(sIP);

                //보낼 데이터 생성
                byte[] buf = data.getBytes();

                //패킷으로 변경
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, sPORT);

                //패킷 전송!
                socket.send(packet);

                //데이터 수신 대기
                socket.receive(packet);
                //데이터 수신되었다면 문자열로 변환
                String msg = new String(packet.getData());
                System.out.println(msg);
            }catch (Exception e){

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control);

        Button sound_btn = (Button) findViewById(R.id.button);
        Button send_btn = (Button) findViewById(R.id.send_btn);
        Switch LED = (Switch) findViewById(R.id.LED_switch);
        TextView t1 =(TextView)findViewById(R.id.LED_text);
        TextView t2 =(TextView)findViewById(R.id.sound_text);
        final EditText msg_sender = (EditText)findViewById(R.id.editText);

        final TextView console =(TextView) findViewById(R.id.tmp_console);

        t1.setText("LED");
        t2.setText("Sound");
        View.OnClickListener LED_sw = new View.OnClickListener() {
            boolean switch_set = false;

            @Override
            public void onClick(View view) {
                if (switch_set == false) {
                    mSendData = new SendData("on");
                    mSendData.start();
                    console.setText("on");
                    switch_set = true;
                }
                else{
                    mSendData = new SendData("off");
                    mSendData.start();
                    console.setText("off");
                    switch_set = false;
                }
            }
        };

        View.OnClickListener sound_play = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendData = new SendData("sound");
                mSendData.start();
                console.setText("play sound");
            }
        };

        View.OnClickListener send = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mmm = msg_sender.getText().toString();
                mSendData = new SendData("@TTL##"+mmm);
                mSendData.start();
                msg_sender.setText("");
                console.setText("@TTL##"+mmm);
            }
        };

        sound_btn.setOnClickListener(sound_play);
        LED.setOnClickListener(LED_sw);
        send_btn.setOnClickListener(send);
    }
}
