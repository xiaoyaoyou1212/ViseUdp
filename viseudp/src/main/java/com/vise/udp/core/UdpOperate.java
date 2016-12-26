package com.vise.udp.core;

import com.vise.log.ViseLog;
import com.vise.udp.common.UdpConstant;
import com.vise.udp.core.inter.IData;
import com.vise.udp.core.inter.IListener;
import com.vise.udp.exception.UdpException;
import com.vise.udp.mode.PacketBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-21 16:24
 */
public class UdpOperate {

    private int id;
    private String name;
    private InetSocketAddress connectedAddress;
    private DatagramChannel datagramChannel;
    private SelectionKey selectionKey;
    private int keepAliveMillis = UdpConstant.KEEP_ALIVE_MILLIS;
    private final ByteBuffer readBuffer, writeBuffer;
    private final IData dataDispose;
    private final Object writeLock = new Object();
    private long lastCommunicationTime;
    private List<IListener> listenerList = new ArrayList<>();

    public UdpOperate(IData dataDispose, int bufferSize) {
        this.dataDispose = dataDispose;
        readBuffer = ByteBuffer.allocate(bufferSize);
        writeBuffer = ByteBuffer.allocateDirect(bufferSize);
    }

    public void bind(Selector selector, InetSocketAddress localPort) throws IOException {
        close();
        readBuffer.clear();
        writeBuffer.clear();
        try {
            datagramChannel = selector.provider().openDatagramChannel();
            datagramChannel.socket().bind(localPort);
            datagramChannel.configureBlocking(false);//设置为非阻塞
            selectionKey = datagramChannel.register(selector, SelectionKey.OP_READ);
            selectionKey.attach(this);
            lastCommunicationTime = System.currentTimeMillis();
        } catch (IOException ex) {
            close();
            throw ex;
        }
    }

    public void connect(Selector selector, InetSocketAddress remoteAddress) throws IOException {
        close();
        readBuffer.clear();
        writeBuffer.clear();
        try {
            datagramChannel = selector.provider().openDatagramChannel();
            datagramChannel.socket().bind(null);
            datagramChannel.socket().connect(remoteAddress);
            datagramChannel.configureBlocking(false);//设置为非阻塞
            selectionKey = datagramChannel.register(selector, SelectionKey.OP_READ);
            selectionKey.attach(this);
            lastCommunicationTime = System.currentTimeMillis();
            connectedAddress = remoteAddress;
        } catch (IOException ex) {
            close();
            IOException ioEx = new IOException("Unable to connect to: " + remoteAddress);
            ioEx.initCause(ex);
            throw ioEx;
        }
    }

    public InetSocketAddress readFromAddress() throws IOException {
        DatagramChannel datagramChannel = this.datagramChannel;
        if (datagramChannel == null) throw new SocketException("Connection is closed.");
        lastCommunicationTime = System.currentTimeMillis();
        return (InetSocketAddress) datagramChannel.receive(readBuffer);
    }

    public PacketBuffer readPacketBuffer() throws IOException {
        readBuffer.flip();
        try {
            try {
                PacketBuffer packetBuffer = dataDispose.read(this, readBuffer);
                if (readBuffer.hasRemaining())
                    throw new IOException("Incorrect number of bytes (" + readBuffer.remaining()
                            + " remaining) used to deserialize object: " + packetBuffer);
                notifyReceiveListener(packetBuffer);
                return packetBuffer;
            } catch (Exception ex) {
                notifyErrorListener(new UdpException().setException(ex));
                throw new IOException("Error during deserialization.", ex);
            }
        } finally {
            readBuffer.clear();
        }
    }

    public int send(PacketBuffer packetBuffer) throws IOException {
        if (packetBuffer == null) throw new IllegalArgumentException("packetBuffer cannot be null.");
        if (datagramChannel == null || connectedAddress == null) throw new SocketException("Connection is closed.");
        synchronized (writeLock) {
            try {
                try {
                    notifySendListener(packetBuffer);
                    dataDispose.write(this, writeBuffer, packetBuffer);
                } catch (Exception ex) {
                    notifyErrorListener(new UdpException().setException(ex));
                    throw new IOException("Error serializing object of type: " + packetBuffer.getClass().getName(), ex);
                }
                writeBuffer.flip();
                int length = writeBuffer.limit();
                datagramChannel.send(writeBuffer, connectedAddress);
                lastCommunicationTime = System.currentTimeMillis();
                boolean wasFullWrite = !writeBuffer.hasRemaining();
                return wasFullWrite ? length : -1;
            } finally {
                writeBuffer.clear();
            }
        }
    }

    public void close() {
        connectedAddress = null;
        try {
            if (datagramChannel != null) {
                datagramChannel.close();
                datagramChannel = null;
                if (selectionKey != null) selectionKey.selector().wakeup();
            }
        } catch (IOException ex) {
            ViseLog.e("Unable to close UDP connection." + ex);
        }
    }

    public boolean needsKeepAlive(long time) {
        return connectedAddress != null && keepAliveMillis > 0 && time - lastCommunicationTime > keepAliveMillis;
    }

    public void addListener(IListener listener) {
        if (listenerList.contains(listener)) {
            return;
        }
        this.listenerList.add(listener);
    }

    public void removeListener(IListener listener) {
        this.listenerList.remove(listener);
    }

    private void notifyReceiveListener(final PacketBuffer packetBuffer) {
        for (IListener listener : listenerList) {
            if (listener != null) {
                listener.onReceive(this, packetBuffer);
            }
        }
    }

    private void notifyStartListener() {
        for (IListener listener : listenerList) {
            if (listener != null) {
                listener.onStart(this);
            }
        }
    }

    private void notifyStopListener() {
        for (IListener listener : listenerList) {
            if (listener != null) {
                listener.onStop(this);
            }
        }
    }

    private void notifySendListener(final PacketBuffer packetBuffer) {
        for (IListener listener : listenerList) {
            if (listener != null) {
                listener.onSend(this, packetBuffer);
            }
        }
    }

    private void notifyErrorListener(final UdpException e) {
        for (IListener listener : listenerList) {
            if (listener != null) {
                listener.onError(this, e);
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetSocketAddress getConnectedAddress() {
        return connectedAddress;
    }

    public void setConnectedAddress(InetSocketAddress connectedAddress) {
        this.connectedAddress = connectedAddress;
    }

    public DatagramChannel getDatagramChannel() {
        return datagramChannel;
    }

    public void setDatagramChannel(DatagramChannel datagramChannel) {
        this.datagramChannel = datagramChannel;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public int getKeepAliveMillis() {
        return keepAliveMillis;
    }

    public void setKeepAliveMillis(int keepAliveMillis) {
        this.keepAliveMillis = keepAliveMillis;
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }
}
