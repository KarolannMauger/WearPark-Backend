import { expect } from "chai";
import { axiosInst } from "../setup.js";

describe("Prediction Route", function() {
    let jwt;
    this.beforeAll(async function() {
        const response = await axiosInst.post("auth/login", {
            email: "admin@admin.com",
            password: "Admin123$"
        });
        jwt = response.data.jwt;
    });

    describe("GET /prediction/latest", function() {
        it("should return the most recent prediction", async function() {
            const response = await axiosInst.get("prediction/latest", {
                headers: { Authorization: `Bearer ${jwt}` }
            });
            expect(response.status).to.equal(200);
            expect(response.data).to.have.property("state", "parkinson");
            expect(response.data).to.have.property("label", "Parkinson");
            expect(response.data).to.have.property("probability", 0.82);
            expect(response.data).to.have.property("confidence", "high");
            expect(response.data).to.have.property("prediction", 1);
            expect(response.data).to.have.property("motionEntryId");
            expect(response.data).to.have.property("createdAt");
        });
    });

    describe("GET /prediction/history", function() {
        it("should return the prediction history", async function() {
            const response = await axiosInst.get("prediction/history", {
                headers: { Authorization: `Bearer ${jwt}` }
            });
            expect(response.status).to.equal(200);
            expect(response.data).to.be.an("array");
            expect(response.data.length).to.be.at.least(2);
        });

        it("should return predictions sorted by most recent first", async function() {
            const response = await axiosInst.get("prediction/history", {
                headers: { Authorization: `Bearer ${jwt}` }
            });
            const dates = response.data.map((p) => new Date(p.createdAt).getTime());
            for (let i = 0; i < dates.length - 1; i++) {
                expect(dates[i]).to.be.at.least(dates[i + 1]);
            }
        });

        it("should support pagination", async function() {
            const response = await axiosInst.get("prediction/history", {
                params: { page: 0, size: 1 },
                headers: { Authorization: `Bearer ${jwt}` }
            });
            expect(response.status).to.equal(200);
            expect(response.data.length).to.equal(1);
        });
    });
});
