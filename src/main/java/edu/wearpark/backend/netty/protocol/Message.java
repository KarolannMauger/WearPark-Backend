package edu.wearpark.backend.netty.protocol;

public abstract class Message {
    protected MessageType messageType;
    public MessageType getMessageType() {
        return messageType;
    }
}
