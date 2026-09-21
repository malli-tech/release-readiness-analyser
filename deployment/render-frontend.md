# Render Deployment Guide: Frontend (Next.js)

This document provides instructions for deploying the Next.js frontend (`frontend/`) to Render.

## Overview

- **Service Type**: Render Web Service (Node)
- **Root Directory**: `frontend`
- **Node Version**: 18.x or 20.x
- **Build Command**: `npm run build`
- **Start Command**: `npm run start`

## Environment Variables Required

In your Render Web Service dashboard, configure the following Environment Variable:

| Variable Name | Description | Example |
| :--- | :--- | :--- |
| `NEXT_PUBLIC_API_URL` | Public API URL of your deployed Render backend | `https://your-backend-api.onrender.com` |

> [!IMPORTANT]
> `NEXT_PUBLIC_API_URL` is baked into the client bundle at build time by Next.js. Ensure `NEXT_PUBLIC_API_URL` is set **before** initiating the Render build step.

## Frontend to Backend Communication

1. Client browsers communicate with the deployed backend via CORS-enabled HTTPS requests using `NEXT_PUBLIC_API_URL`.
2. Ensure your backend `CORS_ALLOWED_ORIGINS` environment variable includes your frontend Render URL (`https://your-frontend.onrender.com`).
