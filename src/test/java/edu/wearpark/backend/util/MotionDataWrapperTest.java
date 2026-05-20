package edu.wearpark.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MotionDataWrapperTest {

    @Test
    void shouldWriteAndReadAllFields() {
        MotionDataWrapper wrapper = new MotionDataWrapper();

        wrapper
                .setOffsetMs(100)
                .setAx(1f)
                .setAy(2f)
                .setAz(3f)
                .setGx(4f)
                .setGy(5f)
                .setGz(6f);

        assertEquals(100, wrapper.offsetMs());
        assertEquals(1f, wrapper.ax());
        assertEquals(2f, wrapper.ay());
        assertEquals(3f, wrapper.az());
        assertEquals(4f, wrapper.gx());
        assertEquals(5f, wrapper.gy());
        assertEquals(6f, wrapper.gz());
    }

    @Test
    void shouldComputeAccGeometricMean() {
        MotionDataWrapper wrapper = new MotionDataWrapper();

        wrapper.setAx(3f)
                .setAy(4f)
                .setAz(12f);

        float expected = (float) Math.sqrt(3*3 + 4*4 + 12*12);

        assertEquals(expected, wrapper.accGeometricMean(), 0.0001);
    }

    @Test
    void shouldCopyFromAnotherWrapper() {
        MotionDataWrapper source = new MotionDataWrapper();
        source.setOffsetMs(50)
                .setAx(1f)
                .setAy(2f)
                .setAz(3f)
                .setGx(4f)
                .setGy(5f)
                .setGz(6f);

        MotionDataWrapper target = new MotionDataWrapper();

        target.copyFrom(source);

        assertEquals(50, target.offsetMs());
        assertEquals(1f, target.ax());
        assertEquals(2f, target.ay());
        assertEquals(3f, target.az());
        assertEquals(4f, target.gx());
        assertEquals(5f, target.gy());
        assertEquals(6f, target.gz());
    }

    @Test
    void shouldWorkWithOffsetConstructor() {
        byte[] data = new byte[28 * 2];

        MotionDataWrapper first = new MotionDataWrapper(data, 0);
        MotionDataWrapper second = new MotionDataWrapper(data, 1);

        first.setOffsetMs(111);
        second.setOffsetMs(222);

        assertEquals(111, first.offsetMs());
        assertEquals(222, second.offsetMs());
    }

    @Test
    void defaultConstructorShouldInitializeCorrectly() {
        MotionDataWrapper wrapper = new MotionDataWrapper();

        assertEquals(0, wrapper.offsetMs());
        assertEquals(0f, wrapper.ax());
        assertEquals(0f, wrapper.ay());
        assertEquals(0f, wrapper.az());
        assertEquals(0f, wrapper.gx());
        assertEquals(0f, wrapper.gy());
        assertEquals(0f, wrapper.gz());
    }
}