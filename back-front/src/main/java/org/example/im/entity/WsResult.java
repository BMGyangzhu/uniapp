package org.example.im.entity;

/**
 * @author bgmyangzhu
 * @date 2025/12/5 2:30
 */
public class WsResult<T> {

    private String type;   // 消息类型：CHAT_MESSAGE / CONTACT_UPDATE 
    private T data;        // 具体数据

    public WsResult() {
    }

    public WsResult(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
