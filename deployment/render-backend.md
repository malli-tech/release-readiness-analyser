# Render Deployment Guide: Backend (Spring Boot)

This document provides instructions for deploying the Spring Boot backend (`backend/`) as a Web Service on Render.

## Overview

- **Service Type**: Render Web Service
- **Runtime**: Java 17+ or Docker
- **Root Directory**: `backend`
- **Build Command**: `./mvnw clean package -DskipTests`
- **Start Command**: `java -jar target/ai-release-readiness-analyzer-1.0.0.jar`

## Environment Variables Required

In your Render Web Service dashboard, configure the following Environment Variables:

| Variable Name | Description | Example / Note |
| :--- | :--- | :--- |
| `SERVER_PORT` | Port exposed by Spring Boot | `8080` (or leave empty if Render sets `PORT`) |
| `MONGODB_URI` | Production MongoDB Atlas connection string | `mongodb+srv://<user>:<password>@cluster.mongodb.net/?retryWrites=true&w=majority` |
| `MONGODB_DATABASE` | Database name | `aireadiness` |
| `JWT_SECRET` | Secret key for JWT signing | 256-bit random string (e.g. `404E6352...`) |
| `JWT_EXPIRATION` | JWT token expiration in ms | `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins (comma-separated) | `https://your-frontend.onrender.com` |
| `OPENAI_API_KEY` | OpenAI API Secret Key | `sk-proj-...` |
| `OPENAI_LLM_MODEL` | OpenAI Chat model | `gpt-4o-mini` |
| `OPENAI_EMBEDDING_MODEL` | OpenAI Embedding model | `text-embedding-3-small` |

> [!IMPORTANT]
> Never hardcode `OPENAI_API_KEY`, `MONGODB_URI`, or `JWT_SECRET` in source code or `pom.xml`. Always inject them via environment variables in the Render Dashboard.

## Filesystem & Storage Considerations

- Render Web Services use **ephemeral filesystems**. Any uploaded ZIP files or extracted workspace files stored in `target/workspaces/` will be reset on instance restarts or redeployments.
- Static analysis results, readiness scores, recommendations, risk summaries, AI reviews, and reports are persisted authoritatively in **MongoDB Atlas** and will remain intact across restarts.
