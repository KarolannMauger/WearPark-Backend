import { expect } from "chai";
import { axiosInst } from "../setup.js";

describe("Authentication Route", function() {
    describe("POST /auth/login", function() {
        it("should authenticate user with valid credentials", async function() {
            const response = await axiosInst.post("auth/login", {
                email: "admin@admin.com",
                password: "Admin123$"
            })
    
            // Assert that the response is successful and contains a token
            expect(response.status).to.equal(200);
            expect(response.data).to.have.property("jwt");
        });
        it("should fail to authenticate user with invalid credentials", async function() {
            try {
                await axiosInst.post("auth/login", {
                    email: "admin@admin.com",
                    password: "NotAdmin123$"
                });
            } catch (error) {
                expect(error.response.status).to.equal(403);
                expect(error.response.data).to.have.property("code", "WRONG_PASSWORD");
            }
        });
        it("should fail to authenticate when no user exists", async function() {
            try {
                await axiosInst.post("auth/login", {
                    email: "nonexistent@user.com",
                    password: "SomePassword123$"
                });
            } catch (error) {
                expect(error.response.status).to.equal(404);
                expect(error.response.data).to.have.property("code", "RESOURCE_NOT_FOUND");
                expect(error.response.data).to.have.property("type", "user");
                expect(error.response.data).to.have.property("uri", "nonexistent@user.com");
            }
        });
    });
    describe("POST /auth/register", function() {
        it("should register a new user with valid data", async function() {
            const response = await axiosInst.post("auth/register", {
                email: "randomUser@email.com",
                password: "RandomUser123$"
            });
                
            // Assert that the response is successful and contains a token
            expect(response.status).to.equal(204);
        });
        it("should fail to register a user with an existing email", async function() {
            try {
                await axiosInst.post("auth/register", {
                    email: "admin@admin.com",
                    password: "RandomUser123$"
                });
            } catch (error) {
                expect(error.response.status).to.equal(409);
                expect(error.response.data).to.have.property("code", "EMAIL_CONFLICT");
            }
        });
    });
});