package edu.wearpark.backend.netty.protocol;

import edu.wearpark.backend.util.MotionDataWrapper;
import lombok.Getter;
import lombok.Setter;
import org.bson.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Getter
@Setter
public class SingleMessage extends Message{
    private MotionDataWrapper wrapper;
    public SingleMessage(){
        super();
        wrapper = new MotionDataWrapper();
        this.messageType = MessageType.SINGLE_DATA;
    }
}
