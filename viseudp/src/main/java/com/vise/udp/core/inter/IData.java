package com.vise.udp.core.inter;

import com.vise.udp.command.Command;
import com.vise.udp.config.UdpConfig;
import com.vise.udp.core.UdpOperate;
import com.vise.udp.mode.PacketBuffer;
import com.vise.udp.mode.TargetInfo;
import com.vise.udp.utils.ByteUtil;

import java.nio.ByteBuffer;

/**
 * @Description:
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2016-12-21 16:28
 */
public interface IData {
    void write(UdpOperate udpOperate, ByteBuffer buffer, PacketBuffer packetBuffer);

    PacketBuffer read(UdpOperate udpOperate, ByteBuffer buffer);

    IData DEFAULT = new IData() {
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
                packetBuffer.setTargetInfo(new TargetInfo().setIp(UdpConfig.getInstance().getIp()).setPort(UdpConfig
                        .getInstance().getPort()));
                packetBuffer.setCommand(new Command());
            }
            return packetBuffer;
        }

    };
}
