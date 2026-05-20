package edu.wearpark.backend.util;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class MotionDataListWrapperTest {

    @Test
    void constructor_shouldInitializeLittleEndianBuffer() {
        byte[] bytes = new byte[56];

        MotionDataListWrapper wrapper = new MotionDataListWrapper(bytes);

        ByteBuffer buffer = wrapper.getBuffer();

        assertEquals(ByteOrder.LITTLE_ENDIAN, buffer.order());
        assertSame(bytes, buffer.array());
    }

    @Test
    void size_shouldReturnNumberOfMotionDataEntries() {
        byte[] bytes = new byte[28 * 3];

        MotionDataListWrapper wrapper = new MotionDataListWrapper(bytes);

        assertEquals(3, wrapper.size());
    }

    @Test
    void getFirst_shouldReturnWrapperAtIndexZero() {
        byte[] bytes = new byte[28 * 2];

        MotionDataListWrapper wrapper = new MotionDataListWrapper(bytes);

        MotionDataWrapper result = wrapper.getFirst();

        assertNotNull(result);
    }

    @Test
    void getLast_shouldReturnWrapperAtLastIndex() {
        byte[] bytes = new byte[28 * 4];

        MotionDataListWrapper wrapper = new MotionDataListWrapper(bytes);

        MotionDataWrapper result = wrapper.getLast();

        assertNotNull(result);
    }

    @Test
    void get_shouldReturnWrapperAtSpecifiedIndex() {
        byte[] bytes = new byte[28 * 5];

        MotionDataListWrapper wrapper = new MotionDataListWrapper(bytes);

        MotionDataWrapper result = wrapper.get(3);

        assertNotNull(result);
    }

    @Test
    void get_withoutIndex_shouldIterateSequentially() {
        byte[] bytes = new byte[28 * 2];

        MotionDataListWrapper wrapper = new MotionDataListWrapper(bytes);

        MotionDataWrapper first = wrapper.get();
        MotionDataWrapper second = wrapper.get();
        MotionDataWrapper third = wrapper.get();

        assertNotNull(first);
        assertNotNull(second);

        assertNull(third);
    }

    @Test
    void reset_shouldResetPointerAndClearBuffer() {
        byte[] bytes = new byte[28 * 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) 0x7F;
        }

        MotionDataListWrapper wrapper = new MotionDataListWrapper(bytes);

        // Advance internal pointer
        assertNotNull(wrapper.get());
        assertNotNull(wrapper.get());

        wrapper.reset();

        // Buffer should be zeroed
        for (byte b : wrapper.getBuffer().array()) {
            assertEquals((byte) 0x00, b);
        }

        // Pointer should restart from beginning
        MotionDataWrapper resultAfterReset = wrapper.get();

        assertNotNull(resultAfterReset);
    }

    @Test
    void size_shouldReturnZeroForEmptyArray() {
        MotionDataListWrapper wrapper = new MotionDataListWrapper(new byte[0]);

        assertEquals(0, wrapper.size());
        assertNull(wrapper.get());
    }
}
