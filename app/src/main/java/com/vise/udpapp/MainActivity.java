package com.vise.udpapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private EditText mEdit_udp;
    private Button mSend_udp;
    private TextView mShow_msg;
    private StringBuilder mShow = new StringBuilder();

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
                String data = mEdit_udp.getText().toString();
                ViseLog.i("send data:" + data);
                mShow.append("自己：" + data);
                mShow.append("\n");
                try {
                    packetBuffer.setBytes(data.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
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
                        if (packetBuffer != null) {
                            try {
                                String data = new String(packetBuffer.getBytes(), "UTF-8");
                                mShow.append("对方：" + data);
                                mShow.append("\n");
                                mShow_msg.setText(mShow);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
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
        ViseLog.uprootAll();
        super.onDestroy();
    }
}
