package com.vise.udpapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.vise.common_base.utils.ToastUtil;
import com.vise.common_utils.utils.character.DateTime;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EmojiconsFragment.OnEmojiconBackspaceClickedListener,
        EmojiconGridFragment.OnEmojiconClickedListener {

    private Context mContext;
    private ListView mChatMsgLv;
    private ImageButton mMsgFaceIb;
    private EditText mMsgEditEt;
    private ImageButton mMsgSendIb;
    private FrameLayout mEmojiconFl;
    private ChatAdapter mChatAdapter;
    private List<ChatInfo> mChatInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BuildConfig.DEBUG) {
            ViseLog.getLogConfig().configAllowLog(true)
                    .configShowBorders(true);
            ViseLog.plant(new DefaultTree());
        }
        mContext = this;
        init();
    }

    private void init() {
        bindViews();
        initData();
        initEvent();
    }

    private void bindViews() {
        mChatMsgLv = (ListView) findViewById(R.id.chat_msg_show_list);
        mMsgFaceIb = (ImageButton) findViewById(R.id.chat_msg_face);
        mMsgEditEt = (EditText) findViewById(R.id.chat_msg_edit);
        mMsgSendIb = (ImageButton) findViewById(R.id.chat_msg_send);
        mEmojiconFl = (FrameLayout) findViewById(R.id.chat_emojicons);
    }

    private void initData() {
        mChatAdapter = new ChatAdapter(mContext);
        mChatMsgLv.setAdapter(mChatAdapter);

        ViseUdp.getInstance().getUdpConfig().setIp("192.168.1.100").setPort(8888);
        try {
            initUdpServer();
            initUdpClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                                ChatInfo chatInfo = new ChatInfo();
                                chatInfo.setReceiveMsg(data);
                                chatInfo.setReceiveTime(DateTime.getStringByFormat(new Date(), DateTime.DEFYMDHMS));
                                chatInfo.setSend(false);
                                chatInfo.setNickName("对方");
                                mChatInfoList.add(chatInfo);
                                mChatAdapter.setListAll(mChatInfoList);
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

    private void initEvent() {
        mMsgFaceIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmojiconFl.getVisibility() == View.GONE) {
                    hideSoftInput();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mEmojiconFl.setVisibility(View.VISIBLE);
                            setEmojiconFragment(false);
                        }
                    }, 100);
                } else {
                    mEmojiconFl.setVisibility(View.GONE);
                }
            }
        });
        mMsgSendIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMsgEditEt.getText() != null && mMsgEditEt.getText().toString().trim().length() > 0) {
                    sendMessage(mMsgEditEt.getText().toString());
                    mMsgEditEt.setText("");
                } else {
                    ToastUtil.showToast(mContext, "消息为空，请先输入要发送的消息！");
                }
            }
        });
        mMsgEditEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmojiconFl.setVisibility(View.GONE);
            }
        });
    }

    private void sendMessage(String data) {
        final PacketBuffer packetBuffer = new PacketBuffer();
        packetBuffer.setTargetInfo(new TargetInfo().setPort(8888));
        ViseLog.i("send data:" + data);
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setSendMsg(data);
        chatInfo.setSendTime(DateTime.getStringByFormat(new Date(), DateTime.DEFYMDHMS));
        chatInfo.setSend(true);
        chatInfo.setNickName("自己");
        mChatInfoList.add(chatInfo);
        mChatAdapter.setListAll(mChatInfoList);
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

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void setEmojiconFragment(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_emojicons, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mMsgEditEt);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mMsgEditEt, emojicon);
    }

    @Override
    protected void onDestroy() {
        ViseUdp.getInstance().stop();
        ViseLog.uprootAll();
        super.onDestroy();
    }
}
