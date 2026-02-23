import { 
    MongoClient,
    ObjectId,
    Db
} from 'mongodb';
import axios, { 
    AxiosInstance 
} from 'axios';
import dotenv from 'dotenv';
import { generateMotionEntry } from './utils.js';
dotenv.config();

export let axiosInst: AxiosInstance;
export let dbClient: MongoClient;
export let db: Db;

const DB_TEST_URI    = process.env.DB_TEST_URI as string;
const DB_TEST_DNNAME = process.env.DB_TEST_DNNAME as string;

dbClient  = new MongoClient(DB_TEST_URI);
axiosInst = axios.create({
    baseURL: `http://${process.env.HTTP_TEST_HOST}:${process.env.HTTP_TEST_PORT}/`,
    timeout: 1000,
    headers: {'Content-Type': 'application/json'}
});


export const mochaHooks = {
    async beforeAll() {
        await dbClient.connect();
        db = await dbClient.db(DB_TEST_DNNAME);
        /// setup users
        await db.collection("users").deleteMany({});
        await db.collection("users").insertOne({
            _id: new ObjectId("000000000000000000000001"),
            email: "admin@admin.com",
            role: "ADMIN",
            gender: "male",
            first_name: "Admin",
            last_name: "Admin",
            date_of_birth: new Date("1990-01-01"),
            has_diagnosis: false,
            diagnosis: null,
            preferences: {},
            is_email_validated: true,
            auth: {
                pwd_digest: "$2a$12$VzmjPVoxT/wOWxk8jQIl8eG0hYVEfuhVYza7tRk/DFiEbQgwTNHD2", // Admin123$
                attempts: 0,
                last_attempt: new Date()
            },
        })

        /// setup motion entries
        await db.collection("motion_entries").deleteMany({});
        await db.collection("motion_entries").insertMany([
            generateMotionEntry(10, "000000000000000000000001", new Date("2024-01-01T00:00:00Z")),
            generateMotionEntry(10, "000000000000000000000001", new Date("2024-01-01T01:00:00Z")),
            generateMotionEntry(10, "000000000000000000000001", new Date("2024-01-01T02:00:00Z")),
            generateMotionEntry(10, "000000000000000000000001", new Date("2024-01-01T04:00:00Z")),
            generateMotionEntry(128, "000000000000000000000001", new Date("2024-01-01T08:00:00Z")),
        ]);

        /// setup devices
        await db.collection("devices").deleteMany({});
        await db.collection("devices").insertOne({
            _id: new ObjectId("000000000000000000000001"),
            user_id: new ObjectId("000000000000000000000001"),
            device_key: "device-test",
         });
    },
    async afterAll() {
        await dbClient.close();
    }
};