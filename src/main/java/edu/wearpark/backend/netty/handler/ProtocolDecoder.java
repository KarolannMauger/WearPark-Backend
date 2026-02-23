package edu.wearpark.backend.netty.handler;

import edu.wearpark.backend.netty.protocol.SingleMessage;
import edu.wearpark.backend.netty.protocol.TimestampMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ProtocolDecoder extends ByteToMessageDecoder {
    private float readFloatLE(ByteBuf in) {
        int intBits = in.readIntLE();
        return Float.intBitsToFloat(intBits);
    }
    private void decodeSingleMessage(ByteBuf in, List<Object> out) {
        if(in.readableBytes() < 28) {
            in.resetReaderIndex();
            return;
        }

        var message = new SingleMessage();
        var wrapper = message.getWrapper();
        wrapper.setOffsetMs(in.readIntLE());
        wrapper.setAx(readFloatLE(in));
        wrapper.setAy(readFloatLE(in));
        wrapper.setAz(readFloatLE(in));
        wrapper.setGx(readFloatLE(in));
        wrapper.setGy(readFloatLE(in));
        wrapper.setGz(readFloatLE(in));

        out.add(message);
    }
    private void decodeTimestampMessage(ByteBuf in, List<Object> out) {
        if(in.readableBytes() < 8) {
            in.resetReaderIndex();
            return;
        }
        var message = new TimestampMessage();
        message.setTimestamp(Instant.ofEpochMilli(in.readLongLE()));

        out.add(message);
    }
    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(!in.isReadable())
            return;
        in.markReaderIndex();
        byte messageType = in.readByte();
        switch (messageType) {
            case 0x00 -> decodeTimestampMessage(in, out);
            case 0x01 -> decodeSingleMessage(in, out);
            default -> ctx.close();
        }
    }
}
