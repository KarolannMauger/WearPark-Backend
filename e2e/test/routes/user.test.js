import { expect } from "chai";
import { axiosInst } from "../setup.js";

describe("User Route", function() {
    let jwt;
    this.beforeAll(async function() {
        const response = await axiosInst.post("auth/login", {
            email: "admin@admin.com",
            password: "Admin123$"
        });
        jwt = response.data.jwt;
    });
    describe("GET /user", function() {
        it("should return user information for authenticated user", async function() {
            const response = await axiosInst.get("user", {
                headers: { Authorization: `Bearer ${jwt}`}
            });

            expect(response.status).to.equal(200);
            expect(response.data).to.have.property("email", "admin@admin.com");
            expect(response.data).to.have.property("gender", "male");
            expect(response.data).to.have.property("firstName", "Admin");
            expect(response.data).to.have.property("lastName", "Admin");
            expect(response.data).to.have.property("dateOfBirth");
        });
    });
    describe("POST /user", function() {
        it("should patch user information for authenticated user", async function() {
            let response = await axiosInst.post("user", 
                {
                    gender: "female",
                    firstName: "Admina"
                },
                {
                    headers: { Authorization: `Bearer ${jwt}`}
                }
            );

            expect(response.status).to.equal(204);

            response = await axiosInst.get("user", {
                headers: { Authorization: `Bearer ${jwt}`}
            });
            expect(response.status).to.equal(200);
            expect(response.data).to.have.property("firstName", "Admina");
        });
    });
            
});