package com.vise.udpapp;

/**
 * @Description:
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2016-12-27 13:30
 */
public class ChatInfo {
    private int chatId;
    private String nickName;
    private boolean isSend;
    private String sendMsg;
    private String receiveMsg;
    private String sendTime;
    private String receiveTime;

    public int getChatId() {
        return chatId;
    }

    public ChatInfo setChatId(int chatId) {
        this.chatId = chatId;
        return this;
    }

    public String getNickName() {
        return nickName;
    }

    public ChatInfo setNickName(String nickName) {
        this.nickName = nickName;
        return this;
    }

    public boolean isSend() {
        return isSend;
    }

    public ChatInfo setSend(boolean send) {
        isSend = send;
        return this;
    }

    public String getSendMsg() {
        return sendMsg;
    }

    public ChatInfo setSendMsg(String sendMsg) {
        this.sendMsg = sendMsg;
        return this;
    }

    public String getReceiveMsg() {
        return receiveMsg;
    }

    public ChatInfo setReceiveMsg(String receiveMsg) {
        this.receiveMsg = receiveMsg;
        return this;
    }

    public String getSendTime() {
        return sendTime;
    }

    public ChatInfo setSendTime(String sendTime) {
        this.sendTime = sendTime;
        return this;
    }

    public String getReceiveTime() {
        return receiveTime;
    }

    public ChatInfo setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
        return this;
    }

    @Override
    public String toString() {
        return "ChatInfo{" +
                "chatId=" + chatId +
                ", nickName='" + nickName + '\'' +
                ", isSend=" + isSend +
                ", sendMsg='" + sendMsg + '\'' +
                ", receiveMsg='" + receiveMsg + '\'' +
                ", sendTime='" + sendTime + '\'' +
                ", receiveTime='" + receiveTime + '\'' +
                '}';
    }
}
