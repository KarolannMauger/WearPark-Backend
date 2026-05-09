import { expect } from "chai";
import { axiosInst } from "../setup.js";
import { base64_decode_dataview } from "../utils.js";

describe("Motion View Route", function() {
    let jwt;
    this.beforeAll(async function() {
        const response = await axiosInst.post("auth/login", {
            email: "admin@admin.com",
            password: "Admin123$"
        });
        jwt = response.data.jwt;
    });
    describe("GET /motion/view/day", function() {
        it("Get graph data to show a day's motion entries", async function() {
            const response = await axiosInst.get("motion/view/day", {
                params: {
                    date: new Date("2024-01-01").toISOString(),
                },
                headers: { Authorization: `Bearer ${jwt}`}
            });
            var view = base64_decode_dataview(response.data.graph.data);
            // for(let i = 0; i < view.byteLength; i+=4) {
            //     const value = view.getFloat32(i, true);
            //     console.log(`Value: ${value}`);
            // }
            expect(response.status).to.equal(200);
            expect(view.byteLength).to.equal(5760);
        });
    });
});