# Deployment Architecture & Setup Guide

This directory contains deployment guides and instructions for deploying the **AI Release Readiness Analyzer** to cloud infrastructure.

## Overall Architecture Flow

```
+------------------------+           +------------------------+
|    GitHub Repository   |           |    Render Deployment   |
|  (Source Control Root) |           |   (Web Services / UI)   |
+-----------+------------+           +-----------+------------+
            |                                    |
            v                                    v
+------------------------+           +------------------------+
| Render Frontend        | --------> | Render Backend         |
| (Next.js 15 / React)   |  HTTPS    | (Spring Boot 3.4 Java) |
+------------------------+           +-----------+------------+
                                                 |
                       +-------------------------+-------------------------+
                       |                                                   |
                       v                                                   v
          +------------------------+                          +------------------------+
          | MongoDB Atlas          |                          | OpenAI API             |
          | (Managed Document DB)  |                          | (LLM & Embeddings)     |
          +------------------------+                          +------------------------+
```

## Production Components

| Component | Target Platform | Type | Key Configuration |
| :--- | :--- | :--- | :--- |
| **Frontend** | Render | Web Service / Static | `NEXT_PUBLIC_API_URL` pointing to backend Render URL |
| **Backend** | Render | Web Service (Docker / Java) | Spring Boot Maven build, environment variables for MongoDB, JWT, OpenAI |
| **Database** | MongoDB Atlas | Managed Database | Connection string `MONGODB_URI`, Network IP access (`0.0.0.0/0` for Render) |
| **AI / LLM** | OpenAI API | SaaS API | `OPENAI_API_KEY` injected into Render Backend environment |

## Deployment Documentation Files

- [Render Backend Guide](render-backend.md): Spring Boot Web Service build, run commands, and environment variables on Render.
- [Render Frontend Guide](render-frontend.md): Next.js Web Service build, run commands, and environment variables on Render.
- [MongoDB Atlas Guide](mongodb-atlas.md): Database cluster setup, user authentication, database name (`aireadiness`), and security.
