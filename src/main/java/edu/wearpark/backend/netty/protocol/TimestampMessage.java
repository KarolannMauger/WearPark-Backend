package edu.wearpark.backend.netty.protocol;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TimestampMessage extends Message{
    private Instant timestamp;
    public TimestampMessage() {
        this.messageType = MessageType.TIMESTAMP;
    }
}
