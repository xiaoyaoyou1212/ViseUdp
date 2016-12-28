package com.vise.udp.handler;

import com.vise.log.ViseLog;

import java.net.DatagramPacket;

/**
 * @Description:
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2016-12-19 19:41
 */
public interface ClientDiscoveryHandler {
    ClientDiscoveryHandler DEFAULT = new ClientDiscoveryHandler() {

        @Override
        public DatagramPacket onRequestNewDatagramPacket() {
            ViseLog.d("onRequestNewDatagramPacket");
            return new DatagramPacket(new byte[0], 0);
        }

        @Override
        public void onDiscoveredHost(DatagramPacket datagramPacket) {
            ViseLog.d("onDiscoveredHost:" + datagramPacket);
        }

        @Override
        public void onFinally() {
            ViseLog.d("onFinally");
        }

    };

    DatagramPacket onRequestNewDatagramPacket();

    void onDiscoveredHost(DatagramPacket datagramPacket);

    void onFinally();
}
