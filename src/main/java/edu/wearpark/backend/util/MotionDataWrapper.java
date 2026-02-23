package edu.wearpark.backend.util;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MotionDataWrapper {
    private final ByteBuffer buffer;
    private final int offset;
    public MotionDataWrapper() {
        buffer = ByteBuffer
                .allocate(28)
                .order(ByteOrder.LITTLE_ENDIAN);
        offset = 0;
    }
    public MotionDataWrapper(byte[] data) {
        buffer = ByteBuffer
                .wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN);
        offset = 0;
    }
    public MotionDataWrapper(byte[] data, int entryOffset) {
        buffer = ByteBuffer
                .wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN);
        offset = entryOffset*28;
    }
    public void copyFrom(final MotionDataWrapper src) {
        buffer.put(
                offset,
                src.buffer.array(),
                src.offset,
                28
        );
    }
    public int offsetMs(){
        return buffer.getInt(offset);
    }
    public float ax() {
        return buffer.getFloat(offset+4);
    }
    public float ay() {
        return buffer.getFloat(offset+8);
    }
    public float az() {
        return buffer.getFloat(offset+12);
    }
    public float gx() {
        return buffer.getFloat(offset+16);
    }
    public float gy() {
        return buffer.getFloat(offset+20);
    }
    public float gz() {
        return buffer.getFloat(offset+24);
    }
    public MotionDataWrapper setOffsetMs(int offsetMs) {
        buffer.putInt(offset, offsetMs);
        return this;
    }
    public MotionDataWrapper setAx(float ax) {
        buffer.putFloat(offset+4, ax);
        return this;
    }
    public MotionDataWrapper setAy(float ay) {
        buffer.putFloat(offset+8, ay);
        return this;
    }
    public MotionDataWrapper setAz(float az) {
        buffer.putFloat(offset+12, az);
        return this;
    }
    public MotionDataWrapper setGx(float gx) {
        buffer.putFloat(offset+16, gx);
        return this;
    }
    public MotionDataWrapper setGy(float gy) {
        buffer.putFloat(offset+20, gy);
        return this;
    }
    public MotionDataWrapper setGz(float gz) {
        buffer.putFloat(offset+24, gz);
        return this;
    }
    public float accGeometricMean() {
        var ax = ax();
        var ay = ay();
        var az = az();
        return (float) Math.sqrt(
                ((ax*ax) + (ay*ay) + (az*az))
        );
    }
}
