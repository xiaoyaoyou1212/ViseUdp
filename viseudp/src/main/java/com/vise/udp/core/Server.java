package com.vise.udp.core;

import com.vise.log.ViseLog;
import com.vise.udp.common.UdpConstant;
import com.vise.udp.config.UdpConfig;
import com.vise.udp.core.inter.IListener;
import com.vise.udp.core.inter.IThread;
import com.vise.udp.exception.UdpException;
import com.vise.udp.handler.ServerDiscoveryHandler;
import com.vise.udp.mode.PacketBuffer;
import com.vise.udp.parser.IParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-21 16:17
 */
public class Server implements IThread {

    private final Selector selector;
    private int emptySelects;
    private UdpOperate udpOperate;
    private volatile boolean shutdown;
    private Object updateLock = new Object();
    private Thread updateThread;
    private UdpConfig udpConfig;

    public Server() {
        udpConfig = UdpConfig.getInstance();
        if (udpConfig.getParser() == null) {
            udpConfig.setParser(IParser.DEFAULT);
        }
        if (udpConfig.getBufferSize() == 0) {
            udpConfig.setBufferSize(UdpConstant.OBJECT_BUFFER_SIZE);
        }
        if (udpConfig.getServerDiscoveryHandler() == null) {
            udpConfig.setDiscoveryHandler(ServerDiscoveryHandler.DEFAULT);
        }
        try {
            selector = Selector.open();
            udpOperate = new UdpOperate(udpConfig.getParser(), udpConfig.getBufferSize());
        } catch (IOException ex) {
            ViseLog.e(new UdpException().setException(ex).setExceptionMsg("Error opening selector."));
            throw new RuntimeException("Error opening selector.", ex);
        }
    }

    public UdpOperate getUdpOperate() {
        return udpOperate;
    }

    public void bind(int udpPort) throws IOException {
        bind(new InetSocketAddress(udpPort));
    }

    public void bind(InetSocketAddress udpPort) throws IOException {
        close();
        synchronized (updateLock) {
            selector.wakeup();
            try {
                if (udpPort != null) {
                    udpOperate.bind(selector, udpPort);
                    ViseLog.i("Accepting connections on port: " + udpPort + "/UDP");
                }
            } catch (IOException ex) {
                close();
                throw ex;
            }
        }
        ViseLog.d("Server opened.");
    }

    @Override
    public void start() {
        new Thread(this, "Server").start();
    }

    @Override
    public void stop() {
        if (shutdown) return;
        shutdown = true;
        close();
        ViseLog.d("Server thread stopping.");
    }

    @Override
    public void close() {
        if (udpOperate != null) {
            udpOperate.close();
        }
        synchronized (updateLock) {
        }
        selector.wakeup();
        try {
            selector.selectNow();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void update(int timeout) throws IOException {
        updateThread = Thread.currentThread();
        synchronized (updateLock) {
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
                long elapsedTime = System.currentTimeMillis() - startTime;
                try {
                    if (elapsedTime < 25) Thread.sleep(25 - elapsedTime);
                } catch (InterruptedException ex) {
                }
            }
        } else {
            emptySelects = 0;
            Set<SelectionKey> keys = selector.selectedKeys();
            synchronized (keys) {
                outer:
                for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext(); ) {
                    SelectionKey selectionKey = iter.next();
                    iter.remove();
                    UdpOperate fromUdpOperate = (UdpOperate) selectionKey.attachment();
                    try {
                        if (udpOperate == null) {
                            selectionKey.channel().close();
                            continue;
                        }
                        InetSocketAddress fromAddress;
                        try {
                            fromAddress = udpOperate.readFromAddress();
                        } catch (IOException ex) {
                            ViseLog.e(new UdpException().setException(ex).setExceptionMsg("Error reading UDP data."));
                            continue;
                        }
                        if (fromAddress == null) continue;
                        PacketBuffer packetBuffer;
                        try {
                            int ops = selectionKey.readyOps();
                            if ((ops & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                                packetBuffer = udpOperate.readPacketBuffer();
                                ViseLog.i(this + " received UDP: " + packetBuffer);
                            }
                        } catch (IOException ex) {
                            ViseLog.e(new UdpException().setException(ex).setExceptionMsg("Error reading UDP from connection."));
                            continue;
                        }
                    } catch (CancelledKeyException ex) {
                        ViseLog.e(new UdpException().setException(ex));
                        selectionKey.channel().close();
                    }
                }
            }
        }
    }

    @Override
    public void addListener(IListener listener) {
        if (udpOperate != null) {
            udpOperate.addListener(listener);
            ViseLog.d("Server listener added.");
        }
    }

    @Override
    public void removeListener(IListener listener) {
        if (udpOperate != null) {
            udpOperate.removeListener(listener);
            ViseLog.d("Server listener removed.");
        }
    }

    @Override
    public Thread getUpdateThread() {
        return updateThread;
    }

    @Override
    public IParser getParser() {
        return udpConfig.getParser();
    }

    @Override
    public void run() {
        ViseLog.d("Server thread started.");
        shutdown = false;
        while (!shutdown) {
            try {
                update(250);
            } catch (IOException ex) {
                ViseLog.e(new UdpException().setException(ex).setExceptionMsg("Error updating server connections."));
                close();
            }
        }
        ViseLog.d("Server thread stopped.");
    }

    public void dispose() throws IOException {
        close();
        selector.close();
    }
}
