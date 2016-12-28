package com.vise.udp.handler;

import com.vise.log.ViseLog;
import com.vise.udp.parser.IParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @Description:
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2016-12-19 19:41
 */
public interface ServerDiscoveryHandler {
    ServerDiscoveryHandler DEFAULT = new ServerDiscoveryHandler() {
        private ByteBuffer emptyBuffer = ByteBuffer.allocate(0);

        @Override
        public boolean onDiscoverHost(DatagramChannel datagramChannel, InetSocketAddress fromAddress, IParser
                parser) throws IOException {
            ViseLog.d("onDiscoverHost:" + datagramChannel + fromAddress + parser);
            if (datagramChannel == null || fromAddress == null || parser == null) {
                return false;
            }
            datagramChannel.send(emptyBuffer, fromAddress);
            return true;
        }
    };

    boolean onDiscoverHost(DatagramChannel datagramChannel, InetSocketAddress fromAddress, IParser parser)
            throws IOException;
}
