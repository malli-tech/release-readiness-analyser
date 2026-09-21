# Deployment Guide: MongoDB Atlas Configuration

This document provides instructions for setting up a production MongoDB Atlas cluster for the **AI Release Readiness Analyzer**.

## Step-by-Step Setup

1. **Create Atlas Cluster**:
   - Log into [MongoDB Atlas](https://www.mongodb.com/cloud/atlas).
   - Create a cluster (e.g. M0 Free Tier or M10+ Dedicated Cluster).

2. **Database User & Permissions**:
   - Navigate to **Security $\rightarrow$ Database Access**.
   - Create a database user (e.g., `aireadiness_user`).
   - Assign role: **Read and write to any database** (or target database `aireadiness`).

3. **Network Access**:
   - Navigate to **Security $\rightarrow$ Network Access**.
   - Add IP Access Entry: `0.0.0.0/0` (Allow Access from Anywhere) so Render backend instances can establish connection.

4. **Connection String**:
   - Navigate to **Database $\rightarrow$ Connect $\rightarrow$ Drivers (Java/Spring Boot)**.
   - Copy the SRV URI string format:
     `mongodb+srv://<username>:<password>@<cluster-name>.mongodb.net/?retryWrites=true&w=majority`

5. **Backend Configuration**:
   - Supply the connection string to the backend as environment variable `MONGODB_URI`.
   - Set `MONGODB_DATABASE` to `aireadiness`.

## Security Best Practices

- Do **NOT** commit MongoDB connection strings containing real username/password to Git.
- Regularly rotate database passwords in Atlas and update the environment variable in Render.
