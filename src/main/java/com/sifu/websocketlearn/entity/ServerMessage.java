package com.sifu.websocketlearn.entity;

/**
 * 服务端发送消息实体
 *
 * @author sifu
 * @version 1.0
 * @date 2018/5/12
 */
public class ServerMessage {
    private String responseMessage;

    public ServerMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
