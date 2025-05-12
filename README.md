# E-scooter Booking System 🚲

## Overview
The **E-scooter Booking System** is a modern, full-stack web application for managing electric scooter rentals. It features a fully separated frontend and backend, providing a smooth experience for both users and administrators.

- **Frontend**: Built with ⚡️ Vue 3, Vite, and TypeScript for a fast, interactive UI.
- **Backend**: Powered by ☕ Spring Boot (Java 17) with RESTful APIs.
- **Database**: Uses 🐬 MySQL for reliable data storage.

---

## ✨ Main Features
- **User Portal**
  - 📝 Register & Login
  - 📅 Book e-scooters for flexible durations (1 hour, 4 hours, 1 day, 1 week)
  - 💳 Online payment (simulated)
  - 📋 Manage bookings: view, cancel, extend
  - 🛠️ Report scooter issues

- **Admin Dashboard**
  - 🔐 Admin login
  - 🏪 Manage stores & scooters
  - 👤 Manage users & bookings
  - 📊 Revenue statistics & reports
  - 🛠️ Handle fault reports

## 🏗️ Tech Stack
- **Frontend**: Vue 3, Vite, TypeScript, Pinia, Axios
- **Backend**: Spring Boot, Java 17, Maven, RESTful API
- **Database**: MySQL 8.x

## 🏛️ Architecture
- Frontend and backend are fully decoupled, communicating via RESTful APIs
- Multi-role support: user & admin
- Modular, maintainable codebase for easy extension

# E-scooter Booking System Setup Guide

This guide helps you set up and deploy the E-scooter Booking System, including database restoration, backend setup, and frontend setup.

---

## 📦 Requirements

- MySQL 8.x (or compatible version)
- Java 17
- Maven
- Node.js + npm

---

## You can just visit the website http://1.95.126.154/ (expired at 2025/6/12)

## 🏗️ 1. Restore the Database

Before importing the database, make sure the MySQL service is running. On Windows, you can start the service with:

```powershell
net start MySQL80
```

### 1️⃣ Create the Database

Run in MySQL CLI or a GUI tool (like MySQL Workbench):
```sql
CREATE DATABASE escooter_db;
```

---

### 2️⃣ Create and Run Import Script

Save the following script as **import_all.sh** in the folder containing your `.sql` files:

```bash
#!/bin/bash

DB_NAME="escooter_db"
DB_USER="root"
DB_PASS="your_password"

for file in e_scooter_db_*.sql; do
    echo "Importing $file..."
    mysql -u $DB_USER -p$DB_PASS $DB_NAME < "$file"
done

echo "All SQL files imported successfully!"
```

---

### 3️⃣ Run the Script

In terminal:
```bash
cd /path/to/your/sql/files
chmod +x import_all.sh
./import_all.sh
```

⚠️ **Note**:
- Replace `your_password` with your actual MySQL password.
- Ensure `mysql` CLI is in your system PATH.

---

## ⚙️ 2. Install Dependencies

### 1️⃣ Backend

```bash
cd backend
mvn clean install
```

---

### 2️⃣ Frontend

```bash
cd frontend
npm install
```

---

## 🚀 3. Start the Application

### 1️⃣ Start Backend

```bash
cd backend
mvn spring-boot:run
```

---

### 2️⃣ Start Frontend

```bash
cd frontend
npm run dev
```

---

## ✅ 4. Done!

Frontend will be running at:
```
http://localhost:5173/
```

Backend will be running at:
```
http://localhost:8080
```

You can now access and test the system 🎉!
