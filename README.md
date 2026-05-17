# WearPark Backend

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.2-6DB33F?style=flat&logo=springboot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-8.x-47A248?style=flat&logo=mongodb&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=flat)

> REST and WebSocket backend for real-time Parkinsonian tremor monitoring.
> Part of the **WearPark** research project.

---

## Overview

WearPark Backend handles authentication, data ingestion from wrist IMU sensors, tremor analysis, ML-based prediction orchestration, and PDF report generation. It serves both the mobile/web app and the embedded device firmware.

---

## System Architecture

```
ICM-20948 (100 Hz)
      │
      ▼
  Embedded (CircuitPython)
      │  TLS mutual auth (client certificate / CN = device key)
      ▼
  Netty TCP Server (port 9000)
      │  SSL handshake → CN extracted → device lookup → pipeline
      ▼
  WearPark Backend (Spring Boot)
      │
      ├─ Persist motion entries     → MongoDB
      ├─ POST /predict/binary       → WearPark ML (Hugging Face Space)
      ├─ WebSocket /ws/motion       → Mobile app (live stream)
      ├─ REST /motion/view          → Mobile app (daily / monthly)
      ├─ REST /report               → PDF generation (OpenHTMLtoPDF)
      └─ REST /admin                → Admin console (web)
```

---

## API Endpoints

### Auth — `/auth`
| Method | Path | Description |
|---|---|---|
| `POST` | `/auth/login` | Authenticate and receive JWT |
| `POST` | `/auth/register` | Create a new user account |
| `GET` | `/auth/validate-email/{jwt}` | Validated user email - not required yet |

### User — `/user`
| Method | Path | Description |
|---|---|---|
| `GET` | `/user` | Get current user profile |
| `POST` | `/user` | Update current user profile |

### Devices — `/devices`
| Method | Path | Description |
|---|---|---|
| `GET` | `/devices/me` | List devices for current user |
| `POST` | `/devices` | Register a new device |
| `PATCH` | `/devices/{id}` | Update device key |
| `PATCH` | `/devices/{id}/disable` | Disable a device |

### Motion — `/motion`
| Method | Path | Description |
|---|---|---|
| `GET` | `/motion/view/day` | Daily tremor analysis |
| `GET` | `/motion/view/month` | Monthly tremor summary |

### Prediction — `/prediction`
| Method | Path | Description |
|---|---|---|
| `GET` | `/prediction/latest` | Latest ML prediction result |
| `GET` | `/prediction/history` | Paginated prediction history |

### Report — `/report`
| Method | Path | Description |
|---|---|---|
| `POST` | `/report/generate` | Generate PDF report for a given month |
| `GET` | `/report/history` | List generated reports |
| `GET` | `/report/{id}/download` | Download a generated PDF |

### Admin — `/admin`
| Method | Path | Description |
|---|---|---|
| `GET` | `/admin/users` | Paginated user list |
| `GET` | `/admin/users/{id}` | User details |
| `PATCH` | `/admin/users/{id}/role` | Update user role |
| `DELETE` | `/admin/users/{id}` | Soft delete user |
| `POST` | `/admin/devices` | Add device to a user |
| `PATCH` | `/admin/devices/{id}` | Update device key |
| `PATCH` | `/admin/devices/{id}/disable` | Disable a device |

### WebSocket — `/ws`
| Path | Description |
|---|---|
| `/ws/motion?jwt=<token>` | Live IMU data stream to mobile app |

---

## Repository Structure

```
src/main/java/edu/wearpark/backend/
├── config/         # Spring Security, MongoDB auditing, CORS, WebSocket
├── controller/     # REST and WebSocket controllers
├── domain/         # MongoDB documents (User, Device, MotionEntry, Prediction, Report)
├── dto/            # Request and response records
├── exception/      # AppException, NotFoundException, ErrorCode enum
├── mapper/         # Domain → DTO mapping
├── netty/          # TLS TCP server for embedded device ingestion
│                   #   DeviceServer            — NIO bootstrap, starts on @PostConstruct
│                   #   DeviceServerHandler     — mTLS handshake, CN → device lookup, channel auth
│                   #   DeviceServerInitializer — pipeline: SSL → StringEncoder → ProtocolDecoder → BusinessHandler
│                   #   handler/                — BusinessHandler (motion data processing)
│                   #   protocol/               — ProtocolDecoder (binary frame parsing)
├── repository/     # Spring Data MongoDB repositories
├── security/       # JWT filter, auth provider, token utilities
├── service/        # Business logic
│   └── report/     # PDF generation (HTML templates, CSS, OpenHTMLtoPDF)
├── util/           # Shared utilities
└── ws/             # WebSocket handlers and session registry
```

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- MongoDB running locally or via Atlas

### Clone and run

```bash
git clone https://github.com/your-org/WearPark-Backend.git
cd WearPark-Backend
./mvnw spring-boot:run
```

### Configuration

The application is configured via `src/main/resources/application.yaml`. All sensitive values are injected through environment variables with development defaults:

```yaml
db:
  uri:  ${MONGODB_CLIENT_URI:mongodb://localhost:27017/}
  name: ${MONGODB_DB_NAME:wearpark-dev}

auth:
  jwt:
    secret: ${JWT_SECRET:dev-secret-...}
    expiration: 1d
  providers:
    email-password:
      max-attempts: 5
      lockout-duration: 5m

netty:
  port: 9000
  server-cert-path: src/main/resources/server-test.crt
  server-key-path:  src/main/resources/server-test.key
  ca-cert-path:     src/main/resources/ca-test.crt
  ca-key-path:      src/main/resources/ca-test.key

ml:
  hf-space-url: ${ML_HF_SPACE_URL:https://karolannmauger-wearpark-ml.hf.space}
  hf-token:     ${ML_HF_TOKEN:}
```

### Environment variables (production)

| Variable | Description |
|---|---|
| `MONGODB_CLIENT_URI` | MongoDB connection string |
| `MONGODB_DB_NAME` | Database name |
| `JWT_SECRET` | JWT signing secret |
| `ML_HF_SPACE_URL` | WearPark ML Hugging Face Space URL |
| `ML_HF_TOKEN` | Hugging Face API token |

---

## Tech Stack

- **[Spring Boot](https://spring.io/projects/spring-boot)** `4.0.2` — application framework
- **[Spring Security](https://spring.io/projects/spring-security)** — authentication and role-based access control
- **[Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)** — data layer with auditing (`@CreatedDate`, `@LastModifiedDate`)
- **[Spring WebSocket](https://docs.spring.io/spring-framework/reference/web/websocket.html)** — live IMU data streaming to mobile app
- **[Netty](https://netty.io/)** `4.1.x` — TLS TCP server for embedded device ingestion; authenticates devices via mutual TLS (client certificate CN matched against registered device keys), then routes decoded IMU frames to the business pipeline
- **[jjwt](https://github.com/jwtk/jjwt)** `0.13.x` — JWT generation and validation
- **[OpenHTMLtoPDF](https://github.com/danfickle/openhtmltopdf)** `1.1.37` — HTML/CSS to PDF rendering for monthly reports
- **[Lombok](https://projectlombok.org/)** — boilerplate reduction
- **[JaCoCo](https://www.jacoco.org/)** — test coverage reporting (controllers and DTOs excluded)

---

## Tests

```bash
./mvnw test                  # Run all tests
./mvnw test jacoco:report    # Generate coverage report → target/site/jacoco/index.html
```

Controllers and DTOs are excluded from coverage measurement — business logic in `service/` and `security/` is the primary coverage target.

---

## Related Repositories

| Repository | Description |
|---|---|
| [WearPark-Embedded](https://github.com/KarolannMauger/WearPark-Embedded) | CircuitPython firmware — ICM-20948 data acquisition |
| [WearPark-ML](https://github.com/KarolannMauger/WearPark-ML) | Residual CNN — binary Parkinson tremor classification |
| [WearPark-App](https://github.com/KarolannMauger/WearPark-App) | Mobile app — user notifications and history |

---

## License

Copyright © 2026 WearPark. All rights reserved.
This project is released under the [MIT License](./LICENSE).

> **Medical disclaimer:** This software is a research prototype and is NOT a certified medical device. It must not be used as a substitute for professional medical diagnosis or treatment.
