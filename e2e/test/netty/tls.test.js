import "dotenv/config";

import tls from "tls";
import fs from "fs";
import { expect } from "chai";

const HOST = process.env.NETTY_TEST_HOST || 'localhost';
const PORT = parseInt(process.env.NETTY_TEST_PORT || '8443', 10);
const CA_CERT_PATH      = process.env.NETTY_TEST_CA_CERT_PATH
const DEVICE_KEY_PATH   = process.env.NETTY_TEST_DEVICE_KEY_PATH
const DEVICE_CERT_PATH  = process.env.NETTY_TEST_DEVICE_CERT_PATH

describe("netty TLS Handshake", function() {
    it("should complete the TLS handshake successfully", function(done) {
        const client = tls.connect({
            host: HOST,
            port: PORT,
            rejectUnauthorized: false,
            key: fs.readFileSync(DEVICE_KEY_PATH),
            cert: fs.readFileSync(DEVICE_CERT_PATH),
            ca: [fs.readFileSync(CA_CERT_PATH)]
        }, ()=>{
            expect(client.authorized).to.be.true;
            done();
            client.end();
        });
    });
});