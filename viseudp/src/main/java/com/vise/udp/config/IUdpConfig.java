package com.vise.udp.config;

import com.vise.udp.core.inter.IData;
import com.vise.udp.handler.ClientDiscoveryHandler;
import com.vise.udp.handler.ServerDiscoveryHandler;

/**
 * @Description:
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2016-12-21 15:56
 */
public interface IUdpConfig {

    IUdpConfig setIp(String ip);

    IUdpConfig setPort(int port);

    IUdpConfig setKeepAliveMillis(long time);

    IUdpConfig setBufferSize(int bufferSize);

    IUdpConfig setDataDispose(IData dataDispose);

    IUdpConfig setDiscoveryHandler(ServerDiscoveryHandler serverDiscoveryHandler);

    IUdpConfig setDiscoveryHandler(ClientDiscoveryHandler clientDiscoveryHandler);
}
