package com.vise.udp.exception;

import java.io.Serializable;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-21 16:06
 */
public class UdpException implements Serializable {
    private int exceptionCode;
    private String exceptionMsg;
    private Exception exception;

    public int getExceptionCode() {
        return exceptionCode;
    }

    public UdpException setExceptionCode(int exceptionCode) {
        this.exceptionCode = exceptionCode;
        return this;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }

    public UdpException setExceptionMsg(String exceptionMsg) {
        this.exceptionMsg = exceptionMsg;
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public UdpException setException(Exception exception) {
        this.exception = exception;
        return this;
    }

    @Override
    public String toString() {
        return "UdpException{" +
                "exceptionCode=" + exceptionCode +
                ", exceptionMsg='" + exceptionMsg + '\'' +
                ", exception=" + exception +
                '}';
    }
}
