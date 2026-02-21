package edu.wearpark.backend.netty.handler;

import edu.wearpark.backend.netty.protocol.SingleMessage;
import edu.wearpark.backend.netty.protocol.TimestampMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        message.setOffsetMs(in.readIntLE());
        message.setAx(readFloatLE(in));
        message.setAy(readFloatLE(in));
        message.setAz(readFloatLE(in));
        message.setGx(readFloatLE(in));
        message.setGy(readFloatLE(in));
        message.setGz(readFloatLE(in));

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
            //default -> ctx.close();
        }
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
}
