package com.vise.udpapp;

import com.vise.common_utils.utils.character.DateTime;
import com.vise.udp.config.UdpConfig;
import com.vise.udp.core.UdpOperate;
import com.vise.udp.mode.PacketBuffer;
import com.vise.udp.mode.TargetInfo;
import com.vise.udp.parser.IParser;
import com.vise.udp.utils.ByteUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 16/12/28 21:35.
 */
public class ChatParser implements IParser {
    @Override
    public void write(UdpOperate udpOperate, ByteBuffer buffer, PacketBuffer packetBuffer) {
        if (packetBuffer != null && buffer != null) {
            buffer.put(packetBuffer.getBytes());
        }
    }

    @Override
    public PacketBuffer read(UdpOperate udpOperate, ByteBuffer buffer) {
        PacketBuffer packetBuffer = new PacketBuffer();
        if (buffer != null) {
            packetBuffer.setBytes(ByteUtil.bufferToBytes(buffer));
            packetBuffer.setTargetInfo(new TargetInfo().setIp(UdpConfig.getInstance().getIp()
            ).setPort(UdpConfig.getInstance().getPort()));
            String data = "";
            try {
                data = new String(packetBuffer.getBytes(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ChatInfo chatInfo = new ChatInfo();
            chatInfo.setReceiveMsg(data);
            chatInfo.setReceiveTime(DateTime.getStringByFormat(new Date(), DateTime.DEFYMDHMS));
            chatInfo.setSend(false);
            chatInfo.setNickName("对方");
            packetBuffer.setCommand(chatInfo);
        }
        return packetBuffer;
    }
}
