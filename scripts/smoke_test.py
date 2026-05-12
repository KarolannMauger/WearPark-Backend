#!/usr/bin/env python3
"""
WearPark Pipeline Smoke Test
============================
Simulates the full flow:
  Login → Netty TLS (fake IMU data) → ML inference → MongoDB → GET /prediction/latest

Usage:
    python scripts/smoke_test.py

Environment variables (optional, defaults shown):
    BACKEND_URL   = http://localhost:8080
    NETTY_HOST    = localhost
    NETTY_PORT    = 9000
    TEST_EMAIL    = admin@admin.com
    TEST_PASSWORD = Admin123$
    CA_CERT       = e2e/certs/ca-test.crt
    DEVICE_CERT   = e2e/certs/device-test.crt
    DEVICE_KEY    = e2e/certs/device-test.key
    ML_URL        = https://karolannmauger-wearpark-ml.hf.space
    ML_TOKEN      = (your HF token)
    POLL_TIMEOUT  = 30

Requirements:
    pip install requests
"""

import os
import ssl
import socket
import struct
import random
import time
import sys

try:
    import requests
except ImportError:
    print("Missing dependency — run: pip install requests")
    sys.exit(1)

# ── Config ────────────────────────────────────────────────────────────────────

BACKEND_URL   = os.getenv("BACKEND_URL",   "http://localhost:8080")
NETTY_HOST    = os.getenv("NETTY_HOST",    "localhost")
NETTY_PORT    = int(os.getenv("NETTY_PORT", "9000"))
TEST_EMAIL    = os.getenv("TEST_EMAIL",    "admin@admin.com")
TEST_PASSWORD = os.getenv("TEST_PASSWORD", "Admin123$")
CA_CERT       = os.getenv("CA_CERT",       "e2e/certs/ca-test.crt")
DEVICE_CERT   = os.getenv("DEVICE_CERT",   "e2e/certs/device-test.crt")
DEVICE_KEY    = os.getenv("DEVICE_KEY",    "e2e/certs/device-test.key")
ML_URL        = os.getenv("ML_URL",        "https://karolannmauger-wearpark-ml.hf.space")
ML_TOKEN      = os.getenv("ML_TOKEN",      "")
POLL_TIMEOUT  = int(os.getenv("POLL_TIMEOUT", "30"))

# Protocol constants
MSG_TIMESTAMP   = 0x00
MSG_SINGLE_DATA = 0x01
N_SAMPLES       = 1001   # 1001 → triggers exactly 1 full batch (1000) + 1 leftover

# ── Helpers ───────────────────────────────────────────────────────────────────

GREEN  = "\033[92m"
RED    = "\033[91m"
YELLOW = "\033[93m"
RESET  = "\033[0m"
BOLD   = "\033[1m"

def header(n, title):
    print(f"\n{BOLD}{'─'*55}{RESET}")
    print(f"{BOLD}  Step {n}: {title}{RESET}")
    print(f"{BOLD}{'─'*55}{RESET}")

def ok(msg):   print(f"  {GREEN}✓{RESET}  {msg}")
def warn(msg): print(f"  {YELLOW}!{RESET}  {msg}")
def fail(msg): print(f"  {RED}✗{RESET}  {msg}")
def info(msg): print(f"     {msg}")

# ── Step 1: ML health check ───────────────────────────────────────────────────

def check_ml_health():
    header(1, "ML service health check")
    try:
        headers = {"Authorization": f"Bearer {ML_TOKEN}"} if ML_TOKEN else {}
        r = requests.get(f"{ML_URL}/health", headers=headers, timeout=10)
        r.raise_for_status()
        data = r.json()
        if data.get("model_ready"):
            ok(f"Model ready  —  threshold={data.get('threshold', '?'):.3f}")
        else:
            warn("Model not ready yet")
        return True
    except Exception as e:
        fail(f"ML unreachable: {e}")
        return False

# ── Step 2: Backend login ─────────────────────────────────────────────────────

def login():
    header(2, "Backend login")
    r = requests.post(
        f"{BACKEND_URL}/auth/login",
        json={"email": TEST_EMAIL, "password": TEST_PASSWORD},
        timeout=5
    )
    r.raise_for_status()
    jwt = r.json()["jwt"]
    ok(f"Authenticated as {TEST_EMAIL}")
    return jwt

# ── Step 3: Netty TLS — send fake IMU data ───────────────────────────────────

def _build_timestamp_msg():
    """1 byte type + 8 bytes int64 LE  →  9 bytes total"""
    ts_ms = int(time.time() * 1000)
    return struct.pack("<bq", MSG_TIMESTAMP, ts_ms)

def _build_single_data_msg(offset_ms):
    """1 byte type + 4 bytes int32 LE + 6 × float32 LE  →  29 bytes total"""
    ax = random.uniform(-0.5,  0.5)
    ay = random.uniform(-0.5,  0.5)
    az = random.uniform( 9.5, 10.0)   # resting wrist ≈ 1g downward
    gx = random.uniform(-0.1,  0.1)
    gy = random.uniform(-0.1,  0.1)
    gz = random.uniform(-0.1,  0.1)
    return struct.pack("<biffffff", MSG_SINGLE_DATA, offset_ms, ax, ay, az, gx, gy, gz)

def send_netty_data():
    header(3, f"Netty TLS  —  sending {N_SAMPLES} fake IMU samples")

    ctx = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
    ctx.load_verify_locations(CA_CERT)
    ctx.load_cert_chain(DEVICE_CERT, DEVICE_KEY)
    ctx.check_hostname = False
    ctx.verify_mode    = ssl.CERT_REQUIRED

    with socket.create_connection((NETTY_HOST, NETTY_PORT), timeout=10) as raw:
        with ctx.wrap_socket(raw) as tls:
            # Initial OK from server
            greeting = tls.recv(64)
            ok(f"TLS connected  —  server: {greeting.decode().strip()!r}")

            # Send TIMESTAMP
            tls.sendall(_build_timestamp_msg())
            ack = tls.recv(64)
            ok(f"TIMESTAMP sent  —  server: {ack.decode().strip()!r}")

            # Build all SINGLE_DATA messages
            payload = bytearray()
            for i in range(N_SAMPLES):
                payload += _build_single_data_msg(i * 10)   # 10 ms per sample = 100 Hz

            # Send in chunks of 50 messages to avoid TCP fragmentation
            chunk = 29 * 50
            sent  = 0
            for i in range(0, len(payload), chunk):
                tls.sendall(bytes(payload[i:i + chunk]))
                sent += min(chunk, len(payload) - i)
                print(f"  → {sent // 29}/{N_SAMPLES} samples sent", end="\r")
                time.sleep(0.005)

            print()
            ok(f"{N_SAMPLES} samples sent  —  1 full batch (1000) queued for ML")

# ── Step 4: Poll for prediction ───────────────────────────────────────────────

def poll_prediction(jwt):
    header(4, f"Polling backend for prediction  (timeout={POLL_TIMEOUT}s)")
    hdrs    = {"Authorization": f"Bearer {jwt}"}
    sent_at = time.time()

    while time.time() - sent_at < POLL_TIMEOUT:
        try:
            r = requests.get(f"{BACKEND_URL}/prediction/latest", headers=hdrs, timeout=5)
            if r.status_code == 200:
                d = r.json()
                ok("Prediction received!")
                print()
                print(f"  {'state':<12}  {d['state']}")
                print(f"  {'label':<12}  {d['label']}")
                print(f"  {'probability':<12}  {d['probability']:.4f}")
                print(f"  {'confidence':<12}  {d['confidence']}")
                print(f"  {'prediction':<12}  {d['prediction']}")
                print(f"  {'createdAt':<12}  {d['createdAt']}")
                return d
        except Exception:
            pass
        remaining = int(POLL_TIMEOUT - (time.time() - sent_at))
        print(f"  waiting for ML to respond... ({remaining}s left)", end="\r")
        time.sleep(2)

    print()
    fail(f"No prediction received after {POLL_TIMEOUT}s")
    return None

# ── Main ──────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    print(f"\n{BOLD}WearPark Pipeline Smoke Test{RESET}")
    print(f"  Backend  : {BACKEND_URL}")
    print(f"  Netty    : {NETTY_HOST}:{NETTY_PORT}")
    print(f"  ML       : {ML_URL}")

    try:
        check_ml_health()
        jwt = login()
        send_netty_data()
        result = poll_prediction(jwt)

        print(f"\n{'─'*55}")
        if result:
            print(f"{GREEN}{BOLD}  ✓  PIPELINE OK — full flow working!{RESET}")
        else:
            print(f"{RED}{BOLD}  ✗  PIPELINE FAILED — check backend logs{RESET}")
        print(f"{'─'*55}\n")
        sys.exit(0 if result else 1)

    except KeyboardInterrupt:
        print("\n\nInterrupted.")
        sys.exit(1)
    except Exception as e:
        print(f"\n{RED}ERROR: {e}{RESET}")
        sys.exit(1)
