import { ObjectId } from "mongodb";
import { EventEmitter } from "stream";

export const generateMotionEntry = (count: number, userId: string, start: Date) => {
    var view = new DataView(new ArrayBuffer(28*count));
    var offset = 0;

    for(let i = 0; i < count; i++) {
        view.setInt32(i*28, i*400000, true); // timestamp;
        view.setFloat32(i*28 + 4, Math.random() * 10, true); // acc_x
        view.setFloat32(i*28 + 8, Math.random() * 10, true); // acc_y
        view.setFloat32(i*28 + 12, Math.random() * 10, true); // acc_z
        view.setFloat32(i*28 + 16, Math.random() * 10, true); // gyro_x
        view.setFloat32(i*28 + 20, Math.random() * 10, true); // gyro_y
        view.setFloat32(i*28 + 24, Math.random() * 10, true); // gyro_z
    }

    return {
        user_id: new ObjectId(userId.toString()),
        start,
        end: new Date(start.getTime() + offset),
        nb_entries: count,
        data: Buffer.from(view.buffer)
    }
}

export const base64_decode_dataview = (base64String: string) => {
    const binaryString = atob(base64String);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
    }
    return new DataView(bytes.buffer);
}

export const eventToPromise = (target: EventEmitter, eventName: string): Promise<Event> => {
    return new Promise((resolve, reject) => {
        const handleEvent = (event: any) => {
            target.removeListener(eventName, handleEvent);
            resolve(event);
        };
        target.addListener(eventName, handleEvent);
    });
}