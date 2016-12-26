package com.vise.udp.command;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-19 19:36
 */
public class KeepAlive extends Command {
    private int keepAliveId;

    public KeepAlive(int keepAliveId) {
        this.keepAliveId = keepAliveId;
    }

    public int getKeepAliveId() {
        return keepAliveId;
    }

    public KeepAlive setKeepAliveId(int keepAliveId) {
        this.keepAliveId = keepAliveId;
        return this;
    }

    @Override
    public String toString() {
        return "KeepAlive{" +
                "keepAliveId=" + keepAliveId +
                '}';
    }
}
