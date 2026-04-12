package edu.wearpark.backend.util;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class MotionDataListWrapperTest {

    private static byte[] makeBuffer(int entries) {
        return new byte[entries * 28];
    }

    private static byte[] makeBufferWithEntry(int index, int offsetMs, float ax, float ay, float az) {
        byte[] data = new byte[28];
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(0, offsetMs);
        buf.putFloat(4, ax);
        buf.putFloat(8, ay);
        buf.putFloat(12, az);
        // gx, gy, gz default to 0
        byte[] full = new byte[(index + 1) * 28];
        System.arraycopy(data, 0, full, index * 28, 28);
        return full;
    }

    @Test
    void size_returnsCorrectCount() {
        MotionDataListWrapper wrapper = new MotionDataListWrapper(makeBuffer(5));
        assertEquals(5, wrapper.size());
    }

    @Test
    void size_returnsZero_whenEmptyBuffer() {
        MotionDataListWrapper wrapper = new MotionDataListWrapper(new byte[0]);
        assertEquals(0, wrapper.size());
    }

    @Test
    void get_returnsEntriesSequentially() {
        MotionDataListWrapper wrapper = new MotionDataListWrapper(makeBuffer(3));
        assertNotNull(wrapper.get());
        assertNotNull(wrapper.get());
        assertNotNull(wrapper.get());
    }

    @Test
    void get_returnsNull_whenExhausted() {
        MotionDataListWrapper wrapper = new MotionDataListWrapper(makeBuffer(2));
        wrapper.get();
        wrapper.get();
        assertNull(wrapper.get());
    }

    @Test
    void reset_resetsPointerAndZeroesBuffer() {
        byte[] data = makeBufferWithEntry(0, 1234, 1.0f, 2.0f, 3.0f);
        MotionDataListWrapper wrapper = new MotionDataListWrapper(data);
        wrapper.get(); // advance pointer

        wrapper.reset();

        // pointer reset: can get again
        assertNotNull(wrapper.get());
        // buffer zeroed: offsetMs should be 0
        MotionDataListWrapper fresh = new MotionDataListWrapper(wrapper.getBuffer().array().clone());
        assertEquals(0, fresh.getFirst().offsetMs());
    }

    @Test
    void getFirst_returnsFirstEntry() {
        byte[] data = makeBufferWithEntry(0, 999, 1.1f, 2.2f, 3.3f);
        // extend to 2 entries
        byte[] full = new byte[56];
        System.arraycopy(data, 0, full, 0, data.length);
        MotionDataListWrapper wrapper = new MotionDataListWrapper(full);

        assertEquals(999, wrapper.getFirst().offsetMs());
    }

    @Test
    void getLast_returnsLastEntry() {
        byte[] full = new byte[56]; // 2 entries
        ByteBuffer buf = ByteBuffer.wrap(full).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(28, 5000); // second entry offsetMs = 5000

        MotionDataListWrapper wrapper = new MotionDataListWrapper(full);
        assertEquals(5000, wrapper.getLast().offsetMs());
    }

    @Test
    void getByIndex_returnsCorrectEntry() {
        byte[] full = new byte[84]; // 3 entries
        ByteBuffer buf = ByteBuffer.wrap(full).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(56, 7777); // third entry offsetMs = 7777

        MotionDataListWrapper wrapper = new MotionDataListWrapper(full);
        assertEquals(7777, wrapper.get(2).offsetMs());
    }

    @Test
    void getBuffer_returnsSameBackingArray() {
        byte[] data = makeBuffer(3);
        MotionDataListWrapper wrapper = new MotionDataListWrapper(data);
        assertSame(data, wrapper.getBuffer().array());
    }
}
