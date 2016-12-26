package com.vise.udp.core.inter;

import com.vise.udp.core.UdpOperate;
import com.vise.udp.exception.UdpException;
import com.vise.udp.mode.PacketBuffer;

/**
 * @Description:
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2016-12-21 16:23
 */
public interface IListener {
    void onStart(UdpOperate udpOperate);

    void onStop(UdpOperate udpOperate);

    void onSend(UdpOperate udpOperate, PacketBuffer packetBuffer);

    void onReceive(UdpOperate udpOperate, PacketBuffer packetBuffer);

    void onError(UdpOperate udpOperate, UdpException e);
}
