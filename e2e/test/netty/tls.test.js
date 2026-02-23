import tls from "tls";
import fs from "fs";
import { expect } from "chai";
import { eventToPromise } from "../utils.js";

const HOST = process.env.NETTY_TEST_HOST || 'localhost';
const PORT = parseInt(process.env.NETTY_TEST_PORT || '8443', 10);
const CA_CERT_PATH      = process.env.NETTY_TEST_CA_CERT_PATH
const DEVICE_KEY_PATH   = process.env.NETTY_TEST_DEVICE_KEY_PATH
const DEVICE_CERT_PATH  = process.env.NETTY_TEST_DEVICE_CERT_PATH

const makeClientOptions = (overrideOptions = {}) => ({
    host: HOST,
    port: PORT,
    rejectUnauthorized: false,
    key: fs.readFileSync(DEVICE_KEY_PATH),
    cert: fs.readFileSync(DEVICE_CERT_PATH),
    ca: [fs.readFileSync(CA_CERT_PATH)],
    ...overrideOptions
});

describe("netty TLS Handshake", function() {
    it("should complete the TLS handshake successfully", function(done) {
        const client = tls.connect(makeClientOptions(), ()=>{
            expect(client.authorized).to.be.true;
            client.on("data", (data)=>{
                expect(data.toString()).to.equal("OK\n");
                done();
                client.end();
            })
        });
    });
    it("Send timestampe to server", async function() {
        const client = tls.connect(makeClientOptions());
        await eventToPromise(client, "secureConnect");
        expect(client.authorized).to.be.true;
        /// first ok
        var data = await eventToPromise(client, "data");
        expect(data.toString()).to.equal("OK\n");

        var view = new DataView(new Uint8Array(9).buffer);
        view.setInt8(0, 0);
        view.setBigInt64(1, BigInt(Date.now()), true);
        await new Promise((res) => client.write(new Uint8Array(view.buffer), res));
        data = await eventToPromise(client, "data");
        expect(data.toString()).to.equal("OK\n");
        client.end();
    });
    it("Send data to server", async function() {
        const client = tls.connect(makeClientOptions());
        await eventToPromise(client, "secureConnect");
        expect(client.authorized).to.be.true;

        /// first ok
        var data = await eventToPromise(client, "data");
        expect(data.toString()).to.equal("OK\n");

        // timestamp
        var view = new DataView(new Uint8Array(9).buffer);
        view.setInt8(0, 0);
        view.setBigInt64(1, BigInt(Date.now()), true);
        await new Promise((res) => client.write(new Uint8Array(view.buffer), res));
        data = await eventToPromise(client, "data");
        expect(data.toString()).to.equal("OK\n");

        // motion data
        view = new DataView(new ArrayBuffer(2001*29));
        for(let i = 0; i<2001; i++) {
            view.setInt8(29*i, 1);
            view.setInt32(29*i+1, i*100, true);
            view.setFloat32(29*i+5, Math.random() * 10, true);
            view.setFloat32(29*i+9, Math.random() * 10, true);
            view.setFloat32(29*i+13, Math.random() * 10, true);
            view.setFloat32(29*i+17, Math.random() * 10, true);
            view.setFloat32(29*i+21, Math.random() * 10, true);
            view.setFloat32(29*i+25, Math.random() * 10, true);
        }
        client.write(new Uint8Array(view.buffer));
        client.end();
    });
});