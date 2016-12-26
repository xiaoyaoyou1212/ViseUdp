package com.vise.udp.core.inter;

import java.io.IOException;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-21 16:21
 */
public interface IThread extends Runnable {
    void start();

    void stop();

    void close();

    void update(int timeout) throws IOException;

    void addListener(IListener listener);

    void removeListener(IListener listener);

    Thread getUpdateThread();

    IData getDataDispose();
}
