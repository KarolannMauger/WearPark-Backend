package edu.wearpark.backend.netty.protocol;

import lombok.Getter;
import lombok.Setter;
import org.bson.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Getter
@Setter
public class SingleMessage extends Message{
    private int offsetMs;
    private float ax;
    private float ay;
    private float az;
    private float gx;
    private float gy;
    private float gz;
    public SingleMessage(){
        super();
        this.messageType = MessageType.SINGLE_DATA;
    }
    public byte[] toBytes() {
        var result = ByteBuffer.allocate(28);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.putInt(offsetMs);
        result.putFloat(ax);
        result.putFloat(ay);
        result.putFloat(az);
        result.putFloat(gx);
        result.putFloat(gy);
        result.putFloat(gz);
        return result.array();
    }
}
