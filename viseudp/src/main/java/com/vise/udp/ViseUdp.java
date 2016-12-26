package com.vise.udp;

import com.vise.udp.config.UdpConfig;
import com.vise.udp.core.Client;
import com.vise.udp.core.Server;
import com.vise.udp.core.inter.IListener;
import com.vise.udp.mode.PacketBuffer;

import java.io.IOException;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-21 16:07
 */
public class ViseUdp {

    private static ViseUdp instance;
    private UdpConfig udpConfig = UdpConfig.getInstance();
    private IListener clientListener, serverListener;
    private Client client;
    private Server server;

    private ViseUdp() {
        init();
    }

    public static ViseUdp getInstance() {
        if (instance == null) {
            synchronized (ViseUdp.class) {
                if (instance == null) {
                    instance = new ViseUdp();
                }
            }
        }
        return instance;
    }

    private void init() {
        client = new Client();
        server = new Server();
    }

    public ViseUdp startClient(IListener listener) throws IOException {
        this.clientListener = listener;
        client.start();
        client.addListener(listener);
        return instance;
    }

    public ViseUdp startServer(IListener listener) throws IOException {
        this.serverListener = listener;
        server.addListener(listener);
        server.bind(udpConfig.getPort());
        server.start();
        return instance;
    }

    public ViseUdp connect() throws IOException {
        client.connect(udpConfig.getIp(), udpConfig.getPort());
        return instance;
    }

    public ViseUdp send(PacketBuffer packetBuffer) throws IOException {
        client.getUdpOperate().send(packetBuffer);
        return instance;
    }

    public ViseUdp stop() {
        server.removeListener(serverListener);
        client.removeListener(clientListener);
        server.stop();
        client.stop();
        return instance;
    }

    public UdpConfig getUdpConfig() {
        return udpConfig;
    }

    public Client getClient() {
        return client;
    }

    public Server getServer() {
        return server;
    }
}
