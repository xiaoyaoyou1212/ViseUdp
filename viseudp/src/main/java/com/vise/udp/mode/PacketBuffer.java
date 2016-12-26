package com.vise.udp.mode;

import com.vise.udp.command.Command;
import com.vise.udp.utils.HexUtil;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-21 16:14
 */
public class PacketBuffer {

    private TargetInfo targetInfo;
    private byte[] bytes;
    private Command command;

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }

    public PacketBuffer setTargetInfo(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
        return this;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public PacketBuffer setBytes(byte[] bytes) {
        this.bytes = bytes;
        return this;
    }

    public Command getCommand() {
        return command;
    }

    public PacketBuffer setCommand(Command command) {
        this.command = command;
        return this;
    }

    @Override
    public String toString() {
        return "PacketBuffer{" +
                "targetInfo=" + targetInfo +
                ", bytes=" + HexUtil.encodeHexStr(bytes) +
                ", command=" + command +
                '}';
    }
}
