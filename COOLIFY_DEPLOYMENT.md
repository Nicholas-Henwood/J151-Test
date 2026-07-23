# Deploying the TryTons Backend to Coolify

This backend is a **Jakarta EE 10 WAR** that runs on **Payara Micro 6** (bundled by the
`Dockerfile`) and needs a **MySQL 8** database. Coolify builds the image from the
Dockerfile, injects config as environment variables, and gives you a public HTTPS URL.

Public API base once deployed:

```
https://<your-domain>/J151-FINAL-BE/api
```

Health check to confirm it's live:

```
https://<your-domain>/J151-FINAL-BE/api/test/database
```

---

## 1. Create the MySQL database in Coolify

1. In your project, **+ New Resource → Database → MySQL** (version 8).
2. Note the credentials Coolify generates: database name, username, password.
3. The **internal hostname** is the database's service name (e.g. `mysql`). The app
   connects to it over Coolify's internal network on port `3306`.

## 2. Create the application service

1. **+ New Resource → Application → Public Repository** (or connect GitHub) and point it at:
   `https://github.com/Nicholas-Henwood/J151-Test`
2. **Build Pack: Dockerfile** (Coolify auto-detects the `Dockerfile` at the repo root).
3. Set the branch to `main`.
4. **Port: `8080`** (Payara Micro's HTTP port — set this as the exposed/ports value).

## 3. Set environment variables

On the application's **Environment Variables** tab, add every key from
[`env.example`](env.example). Point the DB values at the MySQL resource from step 1:

| Key | Value |
|---|---|
| `DB_URL` | `jdbc:mysql://<mysql-host>:3306/<db>?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | from the MySQL resource |
| `DB_PASSWORD` | from the MySQL resource |
| `DB_MIN_IDLE` | `2` |
| `DB_MAX_IDLE` | `10` |
| `DB_MAX_TOTAL` | `20` |
| `DB_MAX_WAIT_MILLIS` | `10000` |
| `DB_MAX_OPEN_PREPARED_STATEMENTS` | `100` |
| `AUTH_TOKEN_SECRET` | a long random string — generate with `openssl rand -base64 48` |
| `AUTH_TOKEN_ISSUER` | `trytons` |
| `AUTH_TOKEN_LIFETIME_MILLIS` | `86400000` (24h) |

> Replace `<mysql-host>` with the MySQL service's internal hostname and `<db>` with its
> database name. Keep `AUTH_TOKEN_SECRET` private.

## 4. Load the database schema and seed data

The app does **not** create tables on startup — you load the SQL once, up front. Two ways:

**A. From your machine** (if the MySQL port is exposed publicly by Coolify):

```bash
mysql -h <public-host> -P <port> -u <user> -p <db> < src/main/resources/db/schema.sql
mysql -h <public-host> -P <port> -u <user> -p <db> < src/main/resources/db/seed.sql
```

**B. From Coolify's terminal** into the MySQL container: open the database resource's
**Terminal**, then paste the contents of `schema.sql` first, then `seed.sql`.

Run `schema.sql` **before** `seed.sql`.

## 5. Deploy

Click **Deploy**. Coolify runs the multi-stage build (Maven compiles the WAR, then it's
copied onto Payara Micro). First build is slow (Maven downloads dependencies); later
builds are cached.

When it's up, open the health endpoint from the top of this doc — a successful database
response means backend + DB are wired correctly.

---

## Demo accounts (from `seed.sql`)

| Role | Login | Password |
|---|---|---|
| Administrator | `admin@tritan.com` / `admin` | `Admin@12345` |
| Registered user | `john@test.com` / `john` | `John@12345` |

## Troubleshooting

- **App starts then crashes / `Missing required configuration value`** — an env var is
  unset. The app fails fast on any missing key in the table above. Check the logs for the
  exact key name.
- **`Communications link failure` / cannot connect to DB** — `DB_URL` host is wrong. Use
  the MySQL resource's **internal** service name, not `localhost`.
- **404 at the domain root** — expected. The app lives under `/J151-FINAL-BE`, not `/`.
- **Image fails to pull `payara/micro:6.2024.10-jdk17`** — pick a current 6.x-jdk17 tag
  from https://hub.docker.com/r/payara/micro/tags and update the `Dockerfile`. Stay on
  the **6.x** line (Jakarta EE 10); do not use `7.x`/`latest` (Jakarta EE 11 / JDK 25).
```
