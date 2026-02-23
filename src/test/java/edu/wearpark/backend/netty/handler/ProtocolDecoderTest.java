package edu.wearpark.backend.netty.handler;

import edu.wearpark.backend.netty.protocol.SingleMessage;
import edu.wearpark.backend.netty.protocol.TimestampMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolDecoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setup() {
        channel = new EmbeddedChannel(new ProtocolDecoder());
    }

    @Test
    void shouldDecodeTimestampMessage() {
        long now = System.currentTimeMillis();

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0x00);              // message type
        buf.writeLongLE(now);             // timestamp

        assertTrue(channel.writeInbound(buf));

        Object decoded = channel.readInbound();
        assertNotNull(decoded);
        assertTrue(decoded instanceof TimestampMessage);

        TimestampMessage msg = (TimestampMessage) decoded;
        assertEquals(Instant.ofEpochMilli(now), msg.getTimestamp());
    }

    @Test
    void shouldDecodeSingleMessage() {
        ByteBuf buf = Unpooled.buffer();

        buf.writeByte(0x01);  // message type
        buf.writeIntLE(100);  // offset
        buf.writeIntLE(Float.floatToIntBits(1.0f));
        buf.writeIntLE(Float.floatToIntBits(2.0f));
        buf.writeIntLE(Float.floatToIntBits(3.0f));
        buf.writeIntLE(Float.floatToIntBits(4.0f));
        buf.writeIntLE(Float.floatToIntBits(5.0f));
        buf.writeIntLE(Float.floatToIntBits(6.0f));

        assertTrue(channel.writeInbound(buf));

        Object decoded = channel.readInbound();
        assertNotNull(decoded);
        assertTrue(decoded instanceof SingleMessage);

        SingleMessage msg = (SingleMessage) decoded;

        assertEquals(100, msg.getWrapper().offsetMs());
        assertEquals(1.0f, msg.getWrapper().ax());
        assertEquals(2.0f, msg.getWrapper().ay());
        assertEquals(3.0f, msg.getWrapper().az());
        assertEquals(4.0f, msg.getWrapper().gx());
        assertEquals(5.0f, msg.getWrapper().gy());
        assertEquals(6.0f, msg.getWrapper().gz());
    }

    @Test
    void shouldNotDecodeIfNotEnoughBytesForSingleMessage() {
        ByteBuf buf = Unpooled.buffer();

        buf.writeByte(0x01);  // single message
        buf.writeIntLE(100);  // only 4 bytes (needs 28 total)

        assertFalse(channel.writeInbound(buf));

        assertNull(channel.readInbound());
        assertTrue(channel.isOpen());
    }

    @Test
    void shouldCloseChannelOnUnknownMessageType() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0x7F);  // unknown type

        channel.writeInbound(buf);

        assertFalse(channel.isOpen());
    }
}