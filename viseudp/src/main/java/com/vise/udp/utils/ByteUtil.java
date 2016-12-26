package com.vise.udp.utils;

import java.nio.ByteBuffer;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-21 16:03
 */
public class ByteUtil {

    public static byte[] bufferToBytes(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    public static ByteBuffer bytesToBuffer(byte[] bytes) {
        ByteBuffer buff = ByteBuffer.wrap(bytes);
        return buff;
    }
}
