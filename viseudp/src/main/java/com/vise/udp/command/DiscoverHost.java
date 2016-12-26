package com.vise.udp.command;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-19 19:37
 */
public class DiscoverHost extends Command {
    private int port;

    public DiscoverHost(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public DiscoverHost setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public String toString() {
        return "DiscoverHost{" +
                "port=" + port +
                '}';
    }
}
