package edu.wearpark.backend.netty.handler;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.netty.Attributes;
import edu.wearpark.backend.netty.protocol.MessageType;
import edu.wearpark.backend.netty.protocol.SingleMessage;
import edu.wearpark.backend.netty.protocol.TimestampMessage;
import edu.wearpark.backend.repository.MotionEntryRepository;
import edu.wearpark.backend.util.MotionDataListWrapper;
import edu.wearpark.backend.util.MotionDataWrapper;
import edu.wearpark.backend.ws.WsMotionHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BusinessHandlerTest {

    private Logger log;
    private MotionEntryRepository repo;
    private WsMotionHandler wsMotionHandler;
    private BusinessHandler handler;
    private EmbeddedChannel channel;

    @BeforeEach
    void setup() {
        log = mock(Logger.class);
        repo = mock(MotionEntryRepository.class);
        wsMotionHandler = mock(WsMotionHandler.class);
        handler = new BusinessHandler(log, repo, wsMotionHandler);
        channel = new EmbeddedChannel(handler);
    }

    @Test
    void shouldHandleTimestampMessage() {
        Instant now = Instant.now();

        TimestampMessage msg = mock(TimestampMessage.class);
        when(msg.getMessageType()).thenReturn(MessageType.TIMESTAMP);
        when(msg.getTimestamp()).thenReturn(now);

        channel.writeInbound(msg);

        verify(log).info("RECEIVED TIMESTAMP");
        assertEquals("OK\n", channel.readOutbound());
    }

    @Test
    void shouldRejectSingleMessageWithoutTimestamp() {
        SingleMessage msg = mock(SingleMessage.class);
        when(msg.getMessageType()).thenReturn(MessageType.SINGLE_DATA);

        channel.attr(Attributes.DEVICE).set(Device.builder().userId(new ObjectId()).build());

        channel.writeInbound(msg);

        assertEquals("NO_TIMESTAMP", channel.readOutbound());
        assertFalse(channel.isOpen());
    }

    @Test
    void shouldSaveMotionEntryWhenBufferFull() {
        SingleMessage msg = mock(SingleMessage.class);
        when(msg.getMessageType()).thenReturn(MessageType.SINGLE_DATA);

        Device device = Device.builder().userId(new ObjectId()).build();
        Instant ts = Instant.now();

        MotionDataWrapper lastWrapper = mock(MotionDataWrapper.class);
        when(lastWrapper.offsetMs()).thenReturn(1);

        MotionDataListWrapper wrapper = mock(MotionDataListWrapper.class);
        when(wrapper.get()).thenReturn(null);
        when(wrapper.size()).thenReturn(5);
        when(wrapper.getBuffer()).thenReturn(ByteBuffer.allocate(28 * 5));
        when(wrapper.getLast()).thenReturn(lastWrapper);

        channel.attr(Attributes.DEVICE).set(device);
        channel.attr(Attributes.TIMESTAMP).set(ts);
        channel.attr(Attributes.LAST_ENTRY).set(ts);
        channel.attr(Attributes.DATA_LIST).set(wrapper);

        channel.writeInbound(msg);

        verify(repo).save(any());
    }

    @Test
    void shouldComputeEndRelativeToLastEntryNotTimestamp() {
        // Bug fix: end must be lastEntry + lastSampleOffset, not timestamp + lastSampleOffset.
        // If timestamp is used instead of lastEntry, the 2nd, 4th, 6th... batches get near-zero
        // or negative durations because their stored offsets are relative to lastEntry, not timestamp.
        Instant timestamp  = Instant.ofEpochMilli(1_000_000L);
        Instant lastEntry  = Instant.ofEpochMilli(1_020_000L); // 20s after timestamp (end of batch 1)
        int     lastOffset = 5_000;                             // 5s into current batch (batch 2)

        // With fix:  end = lastEntry + lastOffset = 1,025,000 ms
        // Without:   end = timestamp + lastOffset = 1,005,000 ms  (< lastEntry -> negative duration)
        Instant expectedEnd = Instant.ofEpochMilli(lastEntry.toEpochMilli() + lastOffset);

        SingleMessage msg = mock(SingleMessage.class);
        when(msg.getMessageType()).thenReturn(MessageType.SINGLE_DATA);

        MotionDataWrapper lastWrapper = mock(MotionDataWrapper.class);
        when(lastWrapper.offsetMs()).thenReturn(lastOffset);

        MotionDataListWrapper wrapper = mock(MotionDataListWrapper.class);
        when(wrapper.get()).thenReturn(null);
        when(wrapper.size()).thenReturn(1000);
        when(wrapper.getBuffer()).thenReturn(ByteBuffer.allocate(28 * 1000));
        when(wrapper.getLast()).thenReturn(lastWrapper);

        channel.attr(Attributes.DEVICE).set(Device.builder().userId(new ObjectId()).build());
        channel.attr(Attributes.TIMESTAMP).set(timestamp);
        channel.attr(Attributes.LAST_ENTRY).set(lastEntry);
        channel.attr(Attributes.DATA_LIST).set(wrapper);

        channel.writeInbound(msg);

        ArgumentCaptor<MotionEntry> captor = ArgumentCaptor.forClass(MotionEntry.class);
        verify(repo).save(captor.capture());

        MotionEntry saved = captor.getValue();
        assertEquals(lastEntry, saved.getStart());
        assertEquals(expectedEnd, saved.getEnd());
    }
}
