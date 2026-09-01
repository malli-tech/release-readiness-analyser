# System Architecture

## Overview

The **AI Release Readiness Analyzer** foundation follows a 3-tier architecture:

```
+-------------------------------------------------------------+
|                     Next.js Frontend                        |
|        (React, TypeScript, Tailwind CSS - Port 3000)        |
+-------------------------------------------------------------+
                              |
                              | REST API (HTTP / JSON)
                              v
+-------------------------------------------------------------+
|                   Spring Boot Backend                       |
|           (Java 17, Spring Web - Port 8080)                 |
+-------------------------------------------------------------+
                              |
                              | Spring Data MongoDB
                              v
+-------------------------------------------------------------+
|                      MongoDB Atlas                          |
|             (Cloud Database Infrastructure)                 |
+-------------------------------------------------------------+
```

## Core Components (Foundation)

1. **Frontend (Member 1)**
   - **Framework**: Next.js (App Router), React 18, TypeScript, Tailwind CSS
   - **Role**: User interface, dashboard client, and API consumer communicating over REST.

2. **Backend (Member 2)**
   - **Framework**: Java 17+, Spring Boot 3.x, Spring Web, Spring Data MongoDB, Maven
   - **Role**: Central orchestrator and REST API provider.
   - **Endpoints Implemented**: `GET /api/health`

3. **Database Layer**
   - **Technology**: MongoDB Atlas
   - **Configuration**: Managed via `MONGODB_URI` and `MONGODB_DATABASE` environment variables.

---
*Note: Additional services (Analyzer, AI Service, Risk Engine) will be integrated in subsequent parts.*
