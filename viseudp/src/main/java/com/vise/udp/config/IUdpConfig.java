package com.vise.udp.config;

import com.vise.udp.handler.ClientDiscoveryHandler;
import com.vise.udp.handler.ServerDiscoveryHandler;
import com.vise.udp.parser.IParser;

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

    IUdpConfig setParser(IParser parser);

    IUdpConfig setDiscoveryHandler(ServerDiscoveryHandler serverDiscoveryHandler);

    IUdpConfig setDiscoveryHandler(ClientDiscoveryHandler clientDiscoveryHandler);
}
