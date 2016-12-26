package com.vise.udp.core;

import com.vise.log.ViseLog;
import com.vise.udp.command.DiscoverHost;
import com.vise.udp.command.KeepAlive;
import com.vise.udp.common.UdpConstant;
import com.vise.udp.config.UdpConfig;
import com.vise.udp.core.inter.IData;
import com.vise.udp.core.inter.IListener;
import com.vise.udp.core.inter.IThread;
import com.vise.udp.handler.ClientDiscoveryHandler;
import com.vise.udp.mode.PacketBuffer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-21 16:17
 */
public class Client implements IThread {

    private UdpOperate udpOperate;
    private Selector selector;
    private int emptySelects;
    private volatile boolean shutdown;
    private final Object updateLock = new Object();
    private Thread updateThread;
    private InetAddress connectHost;
    private int connectUdpPort;
    private boolean isClosed;
    private UdpConfig udpConfig;

    static {
        try {
            // Needed for NIO selectors on Android 2.2.
            System.setProperty("java.net.preferIPv6Addresses", "false");
        } catch (AccessControlException ignored) {
        }
    }

    public Client() {
        udpConfig = UdpConfig.getInstance();
        if (udpConfig.getDataDispose() == null) {
            udpConfig.setDataDispose(IData.DEFAULT);
        }
        if (udpConfig.getBufferSize() == 0) {
            udpConfig.setBufferSize(UdpConstant.OBJECT_BUFFER_SIZE);
        }
        if (udpConfig.getClientDiscoveryHandler() == null) {
            udpConfig.setDiscoveryHandler(ClientDiscoveryHandler.DEFAULT);
        }
        udpOperate = new UdpOperate(udpConfig.getDataDispose(), udpConfig.getBufferSize());
        try {
            selector = Selector.open();
        } catch (IOException ex) {
            throw new RuntimeException("Error opening selector.", ex);
        }
    }

    public UdpOperate getUdpOperate() {
        return udpOperate;
    }

    public void connect(String host, int udpPort) throws IOException {
        connect(InetAddress.getByName(host), udpPort);
    }

    public void connect(InetAddress host, int udpPort) throws IOException {
        if (host == null) throw new IllegalArgumentException("host cannot be null.");
        if (Thread.currentThread() == getUpdateThread())
            throw new IllegalStateException("Cannot connect on the connection's update thread.");
        this.connectHost = host;
        this.connectUdpPort = udpPort;
        close();
        ViseLog.i("Connecting: " + host + ":" + udpPort);
        try {
            InetSocketAddress udpAddress = new InetSocketAddress(host, udpPort);
            synchronized (updateLock) {
                selector.wakeup();
                udpOperate.connect(selector, udpAddress);
            }

        } catch (IOException ex) {
            close();
            throw ex;
        }
    }

    public void reconnect() throws IOException {
        if (connectHost == null) throw new IllegalStateException("This client has never been connected.");
        connect(connectHost, connectUdpPort);
    }

    @Override
    public void start() {
        if (updateThread != null) {
            shutdown = true;
            try {
                updateThread.join(5000);
            } catch (InterruptedException ignored) {
            }
        }
        updateThread = new Thread(this, "Client");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    @Override
    public void stop() {
        if (shutdown) return;
        shutdown = true;
        selector.wakeup();
        close();
        ViseLog.d("Client thread stopping.");
    }

    @Override
    public void close() {
        if (udpOperate != null) {
            udpOperate.close();
        }
        synchronized (updateLock) {
        }
        if (!isClosed) {
            isClosed = true;
            selector.wakeup();
            try {
                selector.selectNow();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void update(int timeout) throws IOException {
        updateThread = Thread.currentThread();
        synchronized (updateLock) { // Blocks to avoid a select while the selector is used to bind the server
            // connection.
        }
        long startTime = System.currentTimeMillis();
        int select = 0;
        if (timeout > 0) {
            select = selector.select(timeout);
        } else {
            select = selector.selectNow();
        }
        if (select == 0) {
            emptySelects++;
            if (emptySelects == 100) {
                emptySelects = 0;
                // NIO freaks and returns immediately with 0 sometimes, so try to keep from hogging the CPU.
                long elapsedTime = System.currentTimeMillis() - startTime;
                try {
                    if (elapsedTime < 25) Thread.sleep(25 - elapsedTime);
                } catch (InterruptedException ex) {
                }
            }
        } else {
            emptySelects = 0;
            isClosed = false;
            Set<SelectionKey> keys = selector.selectedKeys();
            synchronized (keys) {
                for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext(); ) {
                    keepAlive();
                    SelectionKey selectionKey = iter.next();
                    iter.remove();
                    try {
                        int ops = selectionKey.readyOps();
                        if ((ops & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                            if (udpOperate.readFromAddress() == null) continue;
                            PacketBuffer packetBuffer = udpOperate.readPacketBuffer();
                            if (packetBuffer == null) continue;
                            ViseLog.d(this + " received UDP: " + packetBuffer);
                        }
                    } catch (CancelledKeyException ignored) {
                        // Connection is closed.
                    }
                }
            }
        }
    }

    private void keepAlive() throws IOException {
        long time = System.currentTimeMillis();
        if (udpOperate != null && udpOperate.needsKeepAlive(time)) {
            PacketBuffer packetBuffer = new PacketBuffer();
            packetBuffer.setCommand(new KeepAlive(udpOperate.getId()));
            udpOperate.send(packetBuffer);
        }
    }

    @Override
    public void addListener(IListener listener) {
        if (udpOperate != null) {
            udpOperate.addListener(listener);
            ViseLog.d("Client listener added.");
        }
    }

    @Override
    public void removeListener(IListener listener) {
        if (udpOperate != null) {
            udpOperate.removeListener(listener);
            ViseLog.d("Client listener removed.");
        }
    }

    @Override
    public Thread getUpdateThread() {
        return updateThread;
    }

    @Override
    public IData getDataDispose() {
        return udpConfig.getDataDispose();
    }

    @Override
    public void run() {
        ViseLog.d("Client thread started.");
        shutdown = false;
        while (!shutdown) {
            try {
                update(250);
            } catch (IOException ex) {
                ViseLog.e("Unable to update connection: " + ex.getMessage());
                close();
            }
        }
        ViseLog.d("Client thread stopped.");
    }

    public void dispose() throws IOException {
        close();
        selector.close();
    }

    public InetAddress discoverHost(int udpPort, int timeoutMillis) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            broadcast(udpPort, socket);
            socket.setSoTimeout(timeoutMillis);
            DatagramPacket packet = udpConfig.getClientDiscoveryHandler().onRequestNewDatagramPacket();
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException ex) {
                ViseLog.i("Host discovery timed out.");
                return null;
            }
            ViseLog.i("Discovered server: " + packet.getAddress());
            udpConfig.getClientDiscoveryHandler().onDiscoveredHost(packet);
            return packet.getAddress();
        } catch (IOException ex) {
            ViseLog.e("Host discovery failed." + ex);
            return null;
        } finally {
            if (socket != null) socket.close();
            udpConfig.getClientDiscoveryHandler().onFinally();
        }
    }

    public List<InetAddress> discoverHosts(int udpPort, int timeoutMillis) {
        List<InetAddress> hosts = new ArrayList<InetAddress>();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            broadcast(udpPort, socket);
            socket.setSoTimeout(timeoutMillis);
            while (true) {
                DatagramPacket packet = udpConfig.getClientDiscoveryHandler().onRequestNewDatagramPacket();
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException ex) {
                    ViseLog.i("Host discovery timed out.");
                    return hosts;
                }
                ViseLog.i("Discovered server: " + packet.getAddress());
                udpConfig.getClientDiscoveryHandler().onDiscoveredHost(packet);
                hosts.add(packet.getAddress());
            }
        } catch (IOException ex) {
            ViseLog.e("Host discovery failed." + ex);
            return hosts;
        } finally {
            if (socket != null) socket.close();
            udpConfig.getClientDiscoveryHandler().onFinally();
        }
    }

    private void broadcast(int udpPort, DatagramSocket socket) throws IOException {
        ByteBuffer dataBuffer = ByteBuffer.allocate(64);
        PacketBuffer packetBuffer = new PacketBuffer();
        packetBuffer.setCommand(new DiscoverHost(udpPort));
        udpConfig.getDataDispose().write(udpOperate, dataBuffer, packetBuffer);
        dataBuffer.flip();
        byte[] data = new byte[dataBuffer.limit()];
        dataBuffer.get(data);
        for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            for (InetAddress address : Collections.list(iface.getInetAddresses())) {
                // Java 1.5 doesn't support getting the subnet mask, so try the two most common.
                byte[] ip = address.getAddress();
                ip[3] = -1; // 255.255.255.0
                try {
                    socket.send(new DatagramPacket(data, data.length, InetAddress.getByAddress(ip), udpPort));
                } catch (Exception ignored) {
                }
                ip[2] = -1; // 255.255.0.0
                try {
                    socket.send(new DatagramPacket(data, data.length, InetAddress.getByAddress(ip), udpPort));
                } catch (Exception ignored) {
                }
            }
        }
        ViseLog.d("Broadcasted host discovery on port: " + udpPort);
    }
}
