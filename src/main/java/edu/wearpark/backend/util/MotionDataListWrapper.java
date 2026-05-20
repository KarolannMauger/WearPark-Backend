package edu.wearpark.backend.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MotionDataListWrapper {
    private final ByteBuffer buffer;
    private int ptr;
    public MotionDataListWrapper(byte[] bytes) {
        buffer = ByteBuffer
                    .wrap(bytes)
                    .order(ByteOrder.LITTLE_ENDIAN);
        ptr = 0;
    }
    public int size() {
        return buffer.array().length / 28;
    }
    public void reset() {
        ptr = 0;
        Arrays.fill(buffer.array(), (byte) 0x00);
    }
    public MotionDataWrapper getFirst() {
        return new MotionDataWrapper(buffer.array(), 0);
    }
    public MotionDataWrapper getLast() {
        return new MotionDataWrapper(buffer.array(), size()-1);
    }
    public MotionDataWrapper get(int index) {
        return new MotionDataWrapper(buffer.array(), index);
    }
    public MotionDataWrapper get() {
        if(ptr>=size())
            return null;
        return this.get(ptr++);
    }
    public ByteBuffer getBuffer() {
        return buffer;
    }
}
