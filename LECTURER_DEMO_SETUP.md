# TryTons Lecturer Demo Build

## Purpose

This package stabilises the current TryTons backend and frontend for a progress demonstration. It does **not** claim that every final-project feature is complete.

The verified demonstration scope is:

- Backend deployment and database health check
- User registration
- User login and logout
- JWT creation and frontend session storage
- Players list, search, club filter, position filter and details
- Clubs list, search and details
- Positions API used by the player filters

Transfers, fixtures, leagues, leaderboards, history and processing screens remain visible as work in progress only where appropriate. Their navigation links have been disabled in the main demo navigation to prevent accidental failures during the presentation.

## Required software

- Java 17
- GlassFish 7.1.0
- MySQL 8
- Maven through NetBeans or a local Maven installation

## Database setup

1. Open the backend project.
2. Run `src/main/resources/db/schema.sql` against MySQL.
3. Run `src/main/resources/db/seed.sql` immediately afterwards.

The active `seed.sql` is now the small lecturer-demo seed and matches the current schema. The original larger, incomplete seed is preserved as `seed_full_wip.sql`.

### Demo accounts

| Role | Login | Password |
|---|---|---|
| Administrator | `admin@tritan.com` or `admin` | `Admin@12345` |
| Registered user | `john@test.com` or `john` | `John@12345` |

Registration through the frontend is also available.

## Backend configuration

The backend reads configuration in this order:

1. Java system property
2. Environment variable
3. The local `env` file

A safe template is available as `env.example`. The downloadable demo packages intentionally do not include the real `env` file, so copy your existing local `env` into the backend root or copy `env.example` to `env` and fill in your own values.

For GlassFish, the most reliable local option is to add this JVM option, replacing the path with your backend project directory:

```text
-DTRYTONS_ENV_DIR=C:/path/to/Backend Content
```

The directory must contain the file named `env`. You may alternatively configure each `DB_*` and `AUTH_TOKEN_*` value as a GlassFish JVM `-D` property.

Required keys:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
DB_MIN_IDLE
DB_MAX_IDLE
DB_MAX_OPEN_PREPARED_STATEMENTS
AUTH_TOKEN_SECRET
AUTH_TOKEN_ISSUER
AUTH_TOKEN_LIFETIME_MILLIS
```

Keep the real `env` file private. It has been added to `.gitignore`.

## Build and deployment order

1. Stop GlassFish if stale deployments are locked.
2. Clean and build the backend project.
3. Deploy the backend WAR.
4. Start GlassFish and confirm the backend URLs below.
5. Clean and build the frontend project.
6. Deploy the frontend WAR.
7. Open the frontend root URL and log in.

The context roots are now fixed, so they do not depend on the Maven snapshot version.

### Backend URLs

```text
http://localhost:8080/J151-FINAL-BE/
http://localhost:8080/J151-FINAL-BE/api/test/database
http://localhost:8080/J151-FINAL-BE/api/player
http://localhost:8080/J151-FINAL-BE/api/club
http://localhost:8080/J151-FINAL-BE/api/position
```

### Frontend URL

```text
http://localhost:8080/J151-FINAL-FE/
```

The frontend default backend address is:

```text
http://localhost:8080/J151-FINAL-BE/api
```

It can be overridden with either:

```text
-Dapi.base.url=http://host:port/J151-FINAL-BE/api
```

or the environment variable:

```text
TRYTONS_API_BASE_URL=http://host:port/J151-FINAL-BE/api
```

## Recommended presentation flow

1. Open the backend landing page.
2. Open the database health endpoint and show the successful database response.
3. Open the frontend.
4. Log in using the registered-user demo account.
5. Show the players page.
6. Search by player name and filter by club or position.
7. Open a player details page.
8. Open the clubs page, search for a club and open its details page.
9. Log out.
10. Optionally register a new user, then log in with that new account.

## Main backend changes

### Compilation and contracts

- Corrected the database health DAO resource handling.
- Corrected leaderboard date/time mappings and persistence contracts.
- Added the missing fantasy-team owner lookup contract.
- Added the fixture DAO compatibility lookup required by services.
- Completed player-statistics DAO methods and Optional handling.
- Removed duplicate zero-field Lombok constructor generation.
- Corrected several resource path typos.
- Removed the duplicate application-exception mapper.
- Kept only one JAX-RS `@ApplicationPath` declaration.

### Authentication and CDI

- Login now generates and returns a JWT.
- Login and status responses now match the frontend contracts.
- Logout is protected and the frontend sends the stored Bearer token.
- Corrected constructor injection in `LeagueServiceImpl`.
- Removed invalid provider usage from `RoleUtil`.
- Made application-exception accessors explicit.
- Added configurable `env` filename/directory support.

### Schema and DAO alignment

- Club DAO now uses the current club columns.
- Player DAO no longer expects the removed player total-points column.
- Fantasy-team DAO now uses the current fantasy-team schema.
- Fantasy-points DAO now uses the current date/final/version columns.
- Player-statistics DAO and DTOs now use result, team and player IDs from the current schema.
- Match-result mapping now includes both team scores.
- The fixture status schema includes the processing state used by the Java enum.
- Added a schema-compatible minimal lecturer-demo seed.

## Main frontend changes

### Authentication and API integration

- Added a serializable session-scoped authentication context.
- Stores the JWT, user ID, username, email and role after login.
- Adds the Bearer token automatically to protected backend calls.
- Corrected PUT requests that previously sent POST.
- Added safe response closing and useful backend failure logging.
- Corrected login, registration and logout servlet routing.

### Demo pages

- Added a root redirect page.
- Added player details.
- Added club details.
- Completed player listing filters using the backend player, club and position APIs.
- Completed club listing search and details navigation.
- Added the missing position REST client.
- Marked unfinished modules as WIP instead of linking to unreliable pages.

### Deployment

- Added frontend CDI `beans.xml`.
- Added a Jakarta Servlet 6 web descriptor with session settings.
- Fixed both GlassFish context roots.
- Simplified the frontend Maven configuration and aligned it to Jakarta EE 10.
- Made the frontend backend URL configurable.

## Validation performed

The execution environment did not contain Maven, GlassFish or MySQL, so a true live deployment could not be performed here.

The following checks did pass:

- Backend cross-class source-contract compilation: **0 errors**
- Frontend cross-class source-contract compilation: **0 errors**
- Demo seed insert columns checked against the current schema: **passed**
- Literal servlet JSP dispatcher targets: **passed**
- JSP include targets: **passed**
- Active demo servlet mappings: **passed**
- Single backend `@ApplicationPath`: **passed**
- Backend/frontend context-root and default API URL alignment: **passed**

A final Maven `clean package` and GlassFish deployment must still be run on the development machine before the lecturer demonstration.

## Exact changed-file appendix

### Backend added

- `env.example`
- `src/main/resources/db/demo_seed.sql`
- `src/main/resources/db/seed_full_wip.sql`

### Backend removed

- `src/main/java/com/vzap/trytons/mapper/ApplicationExceptionMapper.java` — duplicate exception mapper

### Backend materially updated

- `.gitignore`
- `src/main/java/com/vzap/trytons/config/DotEnvConfig.java`
- `src/main/java/com/vzap/trytons/dao/ClubDAOImpl.java`
- `src/main/java/com/vzap/trytons/dao/DatabaseTestDAO.java`
- `src/main/java/com/vzap/trytons/dao/FantasyPointsDAOImpl.java`
- `src/main/java/com/vzap/trytons/dao/FantasyTeamDAO.java`
- `src/main/java/com/vzap/trytons/dao/FantasyTeamDAOImpl.java`
- `src/main/java/com/vzap/trytons/dao/FixtureDAO.java`
- `src/main/java/com/vzap/trytons/dao/LeaderboardDAOImpl.java`
- `src/main/java/com/vzap/trytons/dao/MatchResultDAOImpl.java`
- `src/main/java/com/vzap/trytons/dao/PlayerDAOImpl.java`
- `src/main/java/com/vzap/trytons/dao/PlayerStatisticsDAO.java`
- `src/main/java/com/vzap/trytons/dao/PlayerStatisticsDAOImpl.java`
- `src/main/java/com/vzap/trytons/dto/AdminUserSearchResponseDTO.java`
- `src/main/java/com/vzap/trytons/dto/AdminUserStatusResponseDTO.java`
- `src/main/java/com/vzap/trytons/dto/CompetitionProcessingSummaryDTO.java`
- `src/main/java/com/vzap/trytons/dto/FantasyPointCalculationResultDTO.java`
- `src/main/java/com/vzap/trytons/dto/PlayerPointsHistoryResponseDTO.java`
- `src/main/java/com/vzap/trytons/dto/PlayerStatisticsRequestDTO.java`
- `src/main/java/com/vzap/trytons/dto/PlayerStatisticsResponseDTO.java`
- `src/main/java/com/vzap/trytons/dto/RankMovementDTO.java`
- `src/main/java/com/vzap/trytons/dto/ScoringRuleRequestDTO.java`
- `src/main/java/com/vzap/trytons/dto/ScoringRuleResponseDTO.java`
- `src/main/java/com/vzap/trytons/dto/TeamScoreUpdateResultDTO.java`
- `src/main/java/com/vzap/trytons/exceptions/ApplicationException.java`
- `src/main/java/com/vzap/trytons/filter/AuthFilter.java`
- `src/main/java/com/vzap/trytons/model/Leaderboard.java`
- `src/main/java/com/vzap/trytons/model/Ranking.java`
- `src/main/java/com/vzap/trytons/resource/AdminUserResource.java`
- `src/main/java/com/vzap/trytons/resource/AuthResource.java`
- `src/main/java/com/vzap/trytons/resource/ClubResource.java`
- `src/main/java/com/vzap/trytons/resource/FantasyTeamResource.java`
- `src/main/java/com/vzap/trytons/resource/FixtureResource.java`
- `src/main/java/com/vzap/trytons/resource/LeaderboardResource.java`
- `src/main/java/com/vzap/trytons/resource/LeagueResource.java`
- `src/main/java/com/vzap/trytons/resource/LockStatusResource.java`
- `src/main/java/com/vzap/trytons/resource/PlayerResource.java`
- `src/main/java/com/vzap/trytons/resource/PositionResource.java`
- `src/main/java/com/vzap/trytons/service/AuthServiceImpl.java`
- `src/main/java/com/vzap/trytons/service/LeaderboardService.java`
- `src/main/java/com/vzap/trytons/service/LeaderboardServiceImpl.java`
- `src/main/java/com/vzap/trytons/service/LeagueServiceImpl.java`
- `src/main/java/com/vzap/trytons/service/MatchProcessingServiceImpl.java`
- `src/main/java/com/vzap/trytons/service/MatchResultServiceImpl.java`
- `src/main/java/com/vzap/trytons/service/PlayerStatisticsServiceImpl.java`
- `src/main/java/com/vzap/trytons/util/DBConnectionManager.java`
- `src/main/java/com/vzap/trytons/util/RoleUtil.java`
- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`
- `src/main/webapp/WEB-INF/glassfish-web.xml`
- `src/main/webapp/index.html`

### Frontend added

- `src/main/java/za/ac/vzap/trytons/frontend/client/PositionRestClient.java`
- `src/main/java/za/ac/vzap/trytons/frontend/session/SessionAuthContext.java`
- `src/main/webapp/WEB-INF/beans.xml`
- `src/main/webapp/WEB-INF/web.xml`
- `src/main/webapp/index.jsp`
- `src/main/webapp/pages/club.jsp`
- `src/main/webapp/pages/player.jsp`

### Frontend updated

- `pom.xml`
- `src/main/java/za/ac/vzap/trytons/frontend/client/APIClient.java`
- `src/main/java/za/ac/vzap/trytons/frontend/client/AuthRestClient.java`
- `src/main/java/za/ac/vzap/trytons/frontend/client/ClubRestClient.java`
- `src/main/java/za/ac/vzap/trytons/frontend/client/LoginResponse.java`
- `src/main/java/za/ac/vzap/trytons/frontend/client/PlayerResponse.java`
- `src/main/java/za/ac/vzap/trytons/frontend/client/PlayerRestClient.java`
- `src/main/java/za/ac/vzap/trytons/frontend/client/RegisteredUserResponse.java`
- `src/main/java/za/ac/vzap/trytons/frontend/client/TransferRecommendationResponse.java`
- `src/main/java/za/ac/vzap/trytons/frontend/servlet/AdminFixtureServlet.java`
- `src/main/java/za/ac/vzap/trytons/frontend/servlet/AuthServlet.java`
- `src/main/java/za/ac/vzap/trytons/frontend/servlet/ClubServlet.java`
- `src/main/java/za/ac/vzap/trytons/frontend/servlet/PlayerServlet.java`
- `src/main/java/za/ac/vzap/trytons/frontend/util/APIConfig.java`
- `src/main/webapp/WEB-INF/glassfish-web.xml`
- `src/main/webapp/WEB-INF/jspf/navigation.jspf`
- `src/main/webapp/pages/clubs.jsp`
- `src/main/webapp/pages/login.jsp`
- `src/main/webapp/pages/players.jsp`
- `src/main/webapp/pages/register.jsp`
