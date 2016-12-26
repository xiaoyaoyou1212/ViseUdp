package com.vise.udpapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vise.log.ViseLog;
import com.vise.log.inner.DefaultTree;
import com.vise.udp.ViseUdp;
import com.vise.udp.core.UdpOperate;
import com.vise.udp.core.inter.IListener;
import com.vise.udp.exception.UdpException;
import com.vise.udp.mode.PacketBuffer;
import com.vise.udp.mode.TargetInfo;
import com.vise.udp.utils.HexUtil;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private EditText mEdit_udp;
    private Button mSend_udp;
    private TextView mShow_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BuildConfig.DEBUG) {
            ViseLog.getLogConfig().configAllowLog(true)
                    .configShowBorders(true);
            ViseLog.plant(new DefaultTree());
        }
        init();
    }

    private void init() {
        bindViews();
        ViseUdp.getInstance().getUdpConfig().setIp("192.168.1.106").setPort(8888);
        try {
            initUdpServer();
            initUdpClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSend_udp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PacketBuffer packetBuffer = new PacketBuffer();
                packetBuffer.setTargetInfo(new TargetInfo().setIp("192.168.1.106").setPort(8888));
                StringBuilder data = new StringBuilder(mEdit_udp.getText().toString());
                ViseLog.i("send data:" + data);
                if (data.length() % 2 != 0) {
                    data.insert(0, "0");
                }
                packetBuffer.setBytes(HexUtil.decodeHex(data.toString().toCharArray()));
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            ViseUdp.getInstance().send(packetBuffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    private void initUdpClient() throws IOException {
        ViseUdp.getInstance().startClient(new IListener() {
            @Override
            public void onStart(UdpOperate udpOperate) {

            }

            @Override
            public void onStop(UdpOperate udpOperate) {

            }

            @Override
            public void onSend(UdpOperate udpOperate, PacketBuffer packetBuffer) {
                ViseLog.i(packetBuffer);
            }

            @Override
            public void onReceive(UdpOperate udpOperate, PacketBuffer packetBuffer) {
                ViseLog.i(packetBuffer);
            }

            @Override
            public void onError(UdpOperate udpOperate, UdpException e) {
                ViseLog.i(e);
            }
        });
        new Thread() {
            @Override
            public void run() {
                try {
                    ViseUdp.getInstance().connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initUdpServer() throws IOException {
        ViseUdp.getInstance().startServer(new IListener() {
            @Override
            public void onStart(UdpOperate udpOperate) {

            }

            @Override
            public void onStop(UdpOperate udpOperate) {

            }

            @Override
            public void onSend(UdpOperate udpOperate, PacketBuffer packetBuffer) {
                ViseLog.i(packetBuffer);
            }

            @Override
            public void onReceive(UdpOperate udpOperate, final PacketBuffer packetBuffer) {
                ViseLog.i(packetBuffer);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mShow_msg.setText(packetBuffer.toString());
                    }
                });
            }

            @Override
            public void onError(UdpOperate udpOperate, UdpException e) {
                ViseLog.i(e);
            }
        });
    }

    private void bindViews() {
        mEdit_udp = (EditText) findViewById(R.id.edit_udp);
        mSend_udp = (Button) findViewById(R.id.send_udp);
        mShow_msg = (TextView) findViewById(R.id.show_msg);
    }

    @Override
    protected void onDestroy() {
        ViseUdp.getInstance().stop();
        super.onDestroy();
    }
}
