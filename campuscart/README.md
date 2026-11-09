# CampusCart: Buy, Sell and Rent Marketplace for Students

A campus-only marketplace where students buy, sell and rent books, cycles, electronics and hostel essentials. Registration is limited to **@thapar.edu** emails, and a **hostel exchange** flag lets sellers mark items they can hand over quickly inside the hostel.

**Stack:** Java 17, Spring Boot 3 (JDBC via `JdbcTemplate`), PostgreSQL, JWT authentication, React 18 + Vite.

## Features

| Area | What it does |
|---|---|
| Accounts | Register and log in with a college email. Passwords are hashed with BCrypt. Sessions use JWT. |
| Listings | Post, edit and delete items as **sale** or **rent** (per day, week or month), with category, price, photo link and description. |
| Search | Keyword search, category, buy/rent, price range, hostel-exchange filter, sorting. |
| Hostel exchange | Sellers flag items for quick in-hostel handover. Buyers filter by it. |
| Bargaining | Buyers send an offer (or pay the asking price). Sellers accept, decline or **counter**, and the buyer can counter back. Whoever did not make the latest offer has the turn. Rules are enforced on the server. |
| Chat | Buyer and seller message each other about a listing. Messages refresh every few seconds. |
| Photos | Sellers upload a JPG, PNG or WebP (5 MB max) from their device. The file type is checked on the server. |

## Project structure

```
campuscart/
├── backend/            Spring Boot API
│   └── src/main/java/com/campuscart/
│       ├── config/       Security and CORS
│       ├── controller/   REST endpoints
│       ├── dto/          Request objects with validation
│       ├── exception/    Error handling (JSON errors)
│       ├── model/        Records returned by the API
│       ├── repository/   JDBC queries (JdbcTemplate)
│       ├── security/     JWT service and filter
│       └── service/      Business rules
├── frontend/           React app (Vite)
├── database/           schema.sql and data.sql (also loaded automatically by the backend)
├── docs/               API reference, ER diagram, report outline
└── docker-compose.yml  PostgreSQL in one command
```

## Run it

### 1. Database
Either use Docker:
```bash
docker compose up -d
```
or create the database yourself in pgAdmin or `psql`:
```sql
CREATE DATABASE campuscart;
```
Tables and categories are created automatically when the backend starts. You can also run `database/schema.sql` and `database/data.sql` by hand.

### 2. Backend
```bash
cd backend
mvn spring-boot:run
```
The API runs at http://localhost:8080. Defaults expect PostgreSQL user `postgres` with password `postgres`. Override with environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/campuscart` | Database URL |
| `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` | Database login |
| `JWT_SECRET` | development value | Token signing key (32+ characters). **Change for real use.** |
| `EMAIL_DOMAIN` | `thapar.edu` | Allowed email domain (subdomains such as `cs.thapar.edu` also work). `*` allows any while testing. |
| `UPLOAD_DIR` | `uploads` | Folder where uploaded photos are saved |
| `CORS_ORIGINS` | `http://localhost:5173` | Allowed frontend origins |

Only `@thapar.edu` emails can register. To test with another email, start the backend with `EMAIL_DOMAIN=*`.

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
Open http://localhost:5173. The dev server proxies `/api` to the backend.

### 4. Tests
```bash
cd backend
mvn test
```
Unit tests cover JWT handling, the thapar.edu rule, photo type detection and the bargaining rules.

## Try the full flow
1. Register two accounts (use two browsers or a private window for the second).
2. With account A, choose **Post an item**, tick *hostel exchange*.
3. With account B, find it on the home page, open it, and send an offer lower than the asking price. Or choose **Chat with seller**.
4. Back in A, open **Offers**, counter with a new price. In B, accept the counter. Then A marks it completed.
5. The item now shows as sold (or available again if it was a rental).

## Notes and next steps
- Photos are saved on the server's disk in the `uploads` folder. Cloud storage is the next step for deployment.
- Email verification currently checks the **domain**. Sending a one-time code by email is the next upgrade.
- Chat uses short polling. WebSocket push and unread counts are the next upgrade.
