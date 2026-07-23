# Fantasy Trytons League — Infrastructure, Architecture & Auth Flow

Extracted directly from `Fantasy-Trytons-Backend` and `Fantasy-Trytons-Frontend` source (commits `5ff7b4ae` / `6b519552`).

---

## 1. High-level shape

This is **two separate JEE WAR deployments** that talk to each other over HTTP — not a monolith.

| | Backend (`J151-FINAL-BE`) | Frontend (`J151-FINAL-FE`) |
|---|---|---|
| Role | Stateless REST API | Server-rendered web app (MVC) |
| Tech | JAX-RS (Jakarta EE 10 full profile), CDI, JDBC | Servlets + JSP, CDI, JAX-RS **Client** |
| State | None (pure JWT bearer auth) | Yes — `HttpSession` + CDI `@SessionScoped` |
| Persistence | Direct JDBC via DAO layer (no JPA/Hibernate) | None — talks to backend only |
| DB | MySQL 8, via Apache Commons DBCP2 pool | — |
| Packaging | `.war`, deployed to GlassFish | `.war`, deployed to GlassFish |

They are **not on the same server context** — the frontend calls the backend as an external HTTP client at `http://localhost:8080/J151-FINAL-BE/api` (configurable via `-Dapi.base.url`). This is a classic **BFF-less two-tier JEE split**: browser → frontend servlets (session-based) → backend REST API (token-based) → MySQL.

```
Browser (JSP/HTML)
   │  HTTP + JSESSIONID cookie
   ▼
Frontend WAR (Servlets, JSP, CDI @SessionScoped)
   │  HTTPS/HTTP + "Authorization: Bearer <JWT>"
   ▼
Backend WAR (JAX-RS Resources → Services → DAOs)
   │  JDBC (pooled connections)
   ▼
MySQL
```

---

## 2. Backend architecture (the REST API)

### 2.1 Layering
Standard 4-layer JEE stack, CDI-wired (`@Inject`), one interface + one `Impl` per layer:

```
Resource (JAX-RS @Path)  →  Service (interface + Impl, @ApplicationScoped)  →  DAO (interface + Impl)  →  BaseDAO → DBConnectionManager → MySQL
                    ↑                                     ↑
                 DTOs in/out                        Model/entity objects
```

- **Resources** (`resource/*Resource.java`) — JAX-RS endpoints under `@ApplicationPath("/api")` (set in `RestApplication`). 27 resource classes. They accept/return **DTOs only**, never domain models.
- **Services** (`service/*Service.java` + `*ServiceImpl.java`) — business logic, orchestrates DAOs, throws domain exceptions.
- **DAOs** (`dao/*DAO.java` + `*DAOImpl.java`) — raw JDBC (`PreparedStatement`/`ResultSet`), each extends `BaseDAO`.
- **`BaseDAO`** — the most-connected class in the whole graph (41+24 edges combined with `DataAccessException`). It's a two-line abstract class exposing `getConnection()` from `DBConnectionManager`.
- **`DBConnectionManager`** — a static `BasicDataSource` (Apache DBCP2) configured from env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_MIN_IDLE`, `DB_MAX_IDLE`, `DB_MAX_OPEN_PREPARED_STATEMENTS`). Classic connection-pool-per-JVM singleton — no JNDI datasource, no JPA.
- **DTOs** — request/response objects, `@Valid`-annotated with Bean Validation for input DTOs. There's a clean separation: `XRequestDTO` in, `XResponseDTO` out, never the JPA-style "same object both ways."
- **Config** — `DotEnvConfig` reads from (in priority order) JVM system property → OS env var → `.env` file (via `dotenv-java`). This is how secrets like `AUTH_TOKEN_SECRET` and DB credentials are supplied without hardcoding.

### 2.2 Cross-cutting concerns (JAX-RS providers)
Registered automatically via `@Provider` (classpath scanning — `RestApplication` itself is an **empty** `Application` subclass, so Jersey/RESTEasy auto-discovers everything):

- **`ApplicationExceptionMapper`** — catches the app's own `ApplicationException` hierarchy (`AuthenticationException`, `AuthorisationException`, `ValidationException`, `ResourceNotFoundException`, `ConflictException`, `DataAccessException`, etc.) and converts each to a JSON `ErrorResponseDTO` with the exception's own status code + error code.
- **`JAXExceptionMapper`** — catches Bean Validation `ConstraintViolationException` from `@Valid` DTOs and returns `400 BAD_REQUEST` with a comma-joined message list, error code `VALIDATION_ERROR`.
- **`AuthFilter`** — a `ContainerRequestFilter`, priority `Priorities.AUTHENTICATION`, bound to the `@Authenticated` annotation (see §3).
- **`RoleFilter`** — a `ContainerRequestFilter`, priority `Priorities.AUTHORIZATION`, bound to `@AdminOnly` (see §3).

No CORS filter exists in the backend — there's no `Access-Control-Allow-Origin` handling anywhere in the codebase. Since the frontend calls the backend server-side (JAX-RS `Client`, not a browser `fetch`), this isn't a same-origin problem for the current architecture, but it means the backend API cannot currently be called directly from browser JS on a different origin without adding one.

---

## 3. Authentication & Authorization flow (backend)

### 3.1 Token mechanics
- **Library:** `com.auth0:java-jwt`, algorithm **HMAC256**, signed with a shared secret (`AUTH_TOKEN_SECRET` env var).
- **Claims set:** `issuer` (`AUTH_TOKEN_ISSUER`), `subject` = the user's UUID (string), `issuedAt`, `expiresAt` = `issuedAt + AUTH_TOKEN_LIFETIME_MILLIS`.
- **No refresh-token / denylist storage.** `AuthTokenUtil.revokeToken()` exists as a stub with a comment stating server-side revocation isn't implemented — logout is **client-side only** (see §3.3). A token remains valid until it naturally expires, even after "logout."
- Token creation and validation both live in one static utility class, `AuthTokenUtil` (create / validate / refresh / revoke).

### 3.2 Login sequence
```
Client                     AuthResource            AuthServiceImpl              UserDAO / PasswordUtil
  │  POST /api/auth/login       │                        │                              │
  │  {identifier, password} ───►│                        │                              │
  │                              │── authenticate() ────►│                              │
  │                              │                        │── getUserByEmail() ────────►│
  │                              │                        │   (falls back to username)  │
  │                              │                        │── verifyPassword() (bcrypt) │
  │                              │                        │── updateLastLogin()  ──────►│
  │                              │                        │── AuthTokenUtil.createToken(userId)
  │                              │◄── LoginResponseDTO ───┤   (userId, username, email, role, token)
  │◄── 200 OK + JSON ────────────┤                        │                              │
```
- Login accepts **either email or username** as the identifier (`userDAO.getUserByEmail` first, `getUserByUsername` as fallback).
- Passwords are hashed with **bcrypt** (`jbcrypt`, via `PasswordUtil`) — never stored or compared in plaintext.
- Inactive accounts (`isActive = false`) are rejected with `AuthorisationException` even with correct credentials.
- The response DTO (`LoginResponseDTO`) carries the JWT back to the caller in a normal JSON field (`token`) — **not** a cookie. The client is fully responsible for storing and re-attaching it.

### 3.3 Authenticated request flow
Every endpoint annotated `@Authenticated` (a JAX-RS `@NameBinding` custom annotation) is intercepted by `AuthFilter` **before** the resource method runs (`Priorities.AUTHENTICATION`):

1. Reads the `Authorization` header, requires the literal `Bearer ` prefix — anything else → `AuthenticationException` (mapped to `401`).
2. Strips the prefix, rejects blank tokens.
3. `AuthTokenUtil.validateToken()` — verifies signature + issuer via the JWT library, extracts `subject` as a UUID, requires a non-null `expiresAt`. Any JWT verification failure (bad signature, expired, wrong issuer) → `AuthenticationException`.
4. Re-fetches the **live** user row from `UserDAO` by that UUID — so a deleted/deactivated user is rejected even with a still-valid, unexpired token (`isActive` check).
5. Builds an `AuthPrincipal` (userId, username, email, role) and stashes it on the JAX-RS `ContainerRequestContext` under key `"currentUser"` for the resource method to read via `@Context ContainerRequestContext`.

For endpoints additionally annotated `@AdminOnly`, `RoleFilter` runs next (`Priorities.AUTHORIZATION`), reads the same `"currentUser"` property, and requires `principal.getRole() == ADMINISTRATOR` (via `RoleUtil.isAdmin`) or throws `AuthorisationException` (`403`).

There is **no session state on the backend at all** — every single request is independently re-verified and re-hydrated from the DB. This is what makes it genuinely "stateless REST."

### 3.4 Logout
`AuthServiceImpl.logout()` literally just returns `"Logout acknowledged."` — there is no server-side token invalidation. The comment in the source is explicit: *"Authentication is stateless. The frontend removes its session and stored token."* This is a known, intentional design limitation (see §5 gaps).

### 3.5 Auth-related endpoints (`/api/auth`)
| Method | Path | Auth required | Purpose |
|---|---|---|---|
| POST | `/auth/register` | none | Create account (`RegisteredUserServices.registerUser`) |
| POST | `/auth/login` | none | Returns `LoginResponseDTO` incl. JWT |
| POST | `/auth/logout` | `@Authenticated` | No-op acknowledgement only |
| GET | `/auth/status` | `@Authenticated` | Re-validates the caller's principal, returns `AuthStatusResponseDTO` |

### 3.6 Coverage across the other 23 resources
Auditing every `@Path`-annotated resource class for `@Authenticated`/`@AdminOnly`:

- **8 resources require `@Authenticated`:** Auth, FantasyTeam, Fixture, Leaderboard, League, Notification, Transfer, UserHistory.
- **Only 1 resource (`FixtureResource`) uses `@AdminOnly`**, and only on its mutating endpoints (`POST`, `PUT .../status`) — its `GET` endpoints are merely `@Authenticated`.
- **18 resources have no auth annotation at all** — including data-mutating ones like `ClubResource` (`POST`/`PUT` club records), `MatchResultResource`, `PlayerResource`, `PositionResource`, `ScoringRuleResource`, `SimulationResource`, and `AdminUserResource` (currently an empty stub — no methods implemented yet, so this one is pending rather than a live gap).
- `ProtectedResource.java` is a two-line, unused scaffold class (injects an `HttpServlet` context but is never extended by anything) — dead code left over from an earlier auth approach.

---

## 4. Frontend architecture (the web app)

### 4.1 Layering
```
JSP pages  ◄── forwarded from ──  Servlet (@WebServlet)  ──►  *RestClient (typed per-resource client)  ──►  APIClient  ──►  Backend REST API
                                        │
                                  CDI @SessionScoped SessionAuthContext (holds JWT + user info for this HTTP session)
```

- **Servlets** (`servlet/*Servlet.java`) — one per resource area (Auth, Club, Fixture, Leaderboard, League, Player, Transfer, UserHistory, plus `Admin*` variants). Each `doGet`/`doPost` reads request params, calls a REST client, sets request attributes, and forwards (`RequestDispatcher.forward`) to a JSP under `/pages/`.
- **`*RestClient` classes** (`client/*RestClient.java`) — thin, typed wrappers (`AuthRestClient`, `TransferRestClient`, `ClubRestClient`, `PlayerRestClient`, `LeaderboardRestClient`, `FixtureRestClient`, `AdminFixtureRestClient`, `AdminMatchResultRestClient`, `AdminUserRestClient`, `UserHistoryRestClient`, `LeagueRestClient`) — each just calls `APIClient.get/post/put/delete` with a fixed path and DTO classes, returning `Optional<T>`.
- **`APIClient`** — the single god-node of the frontend graph (12 edges). Wraps the **JAX-RS Client API** (`jakarta.ws.rs.client.Client`), builds a new `Client`/`WebTarget` per call, and — critically — **injects the `Authorization: Bearer <token>` header automatically** on every request if `SessionAuthContext.isAuthenticated()` is true. This is the single choke point where the JWT crosses from frontend session storage into outbound HTTP calls.
- **`APIConfig`** — resolves the backend base URL, defaulting to `http://localhost:8080/J151-FINAL-BE/api`, overridable via `-Dapi.base.url`.

### 4.2 Where the token actually lives on the frontend
- **`SessionAuthContext`** is a CDI `@SessionScoped` bean (backed by the underlying `HttpSession`) holding `userId`, `username`, `email`, `role`, and `token`. This is the **real** client-side session store for auth state — not raw `HttpSession` attributes.
- `isAuthenticated()` = `userId != null && token not blank`. `isAdmin()` additionally checks `role.equalsIgnoreCase("ADMINISTRATOR")`.
- The intended entry point to populate it is `SessionAuthContext.signIn(LoginResponse)`, called from `AbstractServlet.establishAuthenticatedSession()` — which also invalidates any pre-existing `HttpSession` and starts a fresh one (session-fixation mitigation) before signing in.

### 4.3 A real inconsistency worth flagging
`AbstractServlet` — the class that correctly wires session-fixation-safe login (`establishAuthenticatedSession`) and route guards (`requireAuthenticated`, `requireAdmin`) — is **never extended by any concrete servlet**. Every servlet, including `AuthServlet`, extends `HttpServlet` directly. Consequences observed in the code:

- `AuthServlet.doPost()`'s `"login"` branch manually does `request.getSession(true)` and sets raw attributes `userId`/`username`/`role` on the `HttpSession` — it **never calls `authContext.signIn(...)`** and never stores the token anywhere.
- Because `APIClient` reads the JWT from `SessionAuthContext` (not raw session attributes), and `SessionAuthContext.token` is never populated by the actual login servlet, any authenticated request made immediately after login would go out **without** an `Authorization` header until something else populates `SessionAuthContext`.
- No servlet calls `requireAuthenticated()`/`requireAdmin()` — e.g. `TransferServlet` (money/roster-affecting) has no route guard at all. There's a `// TODO [W4-FE-FIXES-01]` comment directly in `AdminMatchResultServlet.java` acknowledging an admin action currently runs with no `SessionAuthContext.isAuthenticated()`/role gate.
- The frontend's `filter/` package exists but contains **no filter classes** (just a `.gitkeep`) — so there's no `web.xml`/servlet-`Filter` enforcing auth centrally either. Page-level protection currently depends entirely on per-servlet code that, per above, isn't being invoked consistently.

This is the frontend's version of the backend's "18 resources without `@Authenticated`" gap (§3.6) — the guard mechanisms exist in the codebase but aren't wired everywhere yet.

### 4.4 Session lifetime
Both WARs set `<session-timeout>30</session-timeout>` (30 minutes) in `web.xml` — that governs the underlying `HttpSession`/CDI `@SessionScoped` lifetime on the frontend. It's independent of the JWT's own expiry (`AUTH_TOKEN_LIFETIME_MILLIS`, backend-side) — the two aren't currently kept in sync anywhere in the code, so a session could outlive its token (resulting in authenticated-looking pages that get `401`s from the API) or a token could outlive the session (irrelevant once the session that held it is gone, since nothing else stores it).

---

## 5. Full end-to-end request example (Transfer flow)

```
1. Browser: GET /transfers  (JSESSIONID cookie sent)
2. TransferServlet.doGet()
     → PlayerRestClient.getX(), TransferRestClient.getTransferHistory(teamId)
        → APIClient.get(path, Type.class)
           → builds new JAX-RS Client, WebTarget = APIConfig.getBaseUrl() + path
           → if SessionAuthContext.isAuthenticated(): adds header
                Authorization: Bearer <jwt>
           → GET http://localhost:8080/J151-FINAL-BE/api/transfers/history?...
3. Backend: AuthFilter intercepts (since TransferResource is @Authenticated)
     → validates JWT, reloads User from DB, builds AuthPrincipal
     → TransferResource method executes, calls TransferService → TransferDAO → JDBC → MySQL
     → returns TransferHistoryResponseDTO list as JSON
4. APIClient.handle(): 2xx → Optional.of(parsed DTO); non-2xx → logs + Optional.empty()
5. TransferServlet sets request attribute "history", forwards to /pages/transfer-history.jsp
6. JSP renders HTML back to the browser
```

Note step 4: **the frontend swallows all backend error detail.** `APIClient.handle()` only checks the numeric status code and, on any non-2xx response, logs a warning and returns `Optional.empty()` — the rich `ErrorResponseDTO` (message + error code) the backend went to the trouble of producing (§2.2) never reaches the JSP or the user; servlets just show a generic `"error"` attribute string they hardcode themselves.

---

## 6. Summary of architectural gaps (for audit purposes)

| # | Gap | Where | Impact |
|---|---|---|---|
| 1 | No server-side token revocation | `AuthTokenUtil.revokeToken()` | Logout doesn't actually invalidate a JWT; it stays valid until expiry |
| 2 | 18/27 backend resources have no `@Authenticated`/`@AdminOnly` | `resource/*.java` | Several data-mutating endpoints (Club, MatchResult, Player, Position, ScoringRule, Simulation…) are open to any caller |
| 3 | Only one resource enforces `@AdminOnly` | `FixtureResource` | Most admin-style operations aren't actually role-gated at the API layer |
| 4 | `AbstractServlet` (route guards + safe session establishment) is dead code | `servlet/AbstractServlet.java` | No frontend servlet enforces login/admin gating; `AuthServlet` doesn't even populate the token into `SessionAuthContext` |
| 5 | Empty `filter/` package on frontend | `frontend/filter/` | No centralized session/auth enforcement filter exists |
| 6 | No CORS handling on backend | backend-wide | Fine for current server-to-server calls; would block a future browser-direct SPA client |
| 7 | Backend session-timeout vs JWT lifetime aren't coordinated | `web.xml` vs `AUTH_TOKEN_LIFETIME_MILLIS` | Can produce a session that looks logged-in but gets 401s, or vice versa |
| 8 | Frontend discards backend error payloads | `APIClient.handle()` | Users/JSPs never see the specific validation/error messages the backend API returns |

---

*Generated from static analysis of the uploaded `Fantasy-Trytons-Backend.zip` and `Fantasy-Trytons-Frontend.zip` (includes pre-existing `graphify-out` dependency graphs at commits `5ff7b4ae` and `6b519552`).*
