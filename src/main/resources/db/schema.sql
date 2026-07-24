/*
    Fantasy TryTons League schema
    Target: MySQL 8.0.16 or newer (CHECK constraints are part of integrity enforcement).
*/

CREATE
DATABASE IF NOT EXISTS `tryton_fantasy_rugby`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE
`tryton_fantasy_rugby`;

/*
    CORE DOMAIN FLOW

    fantasyTeam
        -> team_player_selection (current editable squad)
        -> fantasy_team_round_selection (locked squad snapshot)

    league + fantasyRound
        -> fixture (purely simulated fantasy team versus fantasy team)
            -> simulationSettings (version used for the simulation run)
            -> matchResult (one record per simulation run)
                -> playerStatistics (simulated statistics for each participating team/player)
                    -> fantasyPoints
                        -> fantasy_point_breakdown
                -> match_team_score (auditable score per participating team)
        -> leaderboard + ranking

    Clubs remain player metadata only. They do not participate in fixtures.
*/

SET
FOREIGN_KEY_CHECKS = 0;

DROP VIEW IF EXISTS `player_round_performance`;
DROP TABLE IF EXISTS `systemReport`;
DROP TABLE IF EXISTS `simulationSettings`;
DROP TABLE IF EXISTS `roundLock`;
DROP TABLE IF EXISTS `log`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `fantasy_team_round_selection`;
DROP TABLE IF EXISTS `player_statistics_correction`;
DROP TABLE IF EXISTS `fantasy_point_breakdown`;
DROP TABLE IF EXISTS `fantasyPoints`;
DROP TABLE IF EXISTS `scoringRule`;
DROP TABLE IF EXISTS `playerStatistics`;
DROP TABLE IF EXISTS `match_team_score`;
DROP TABLE IF EXISTS `matchResult`;
DROP TABLE IF EXISTS `fixture`;
DROP TABLE IF EXISTS `ranking`;
DROP TABLE IF EXISTS `leaderboard`;
DROP TABLE IF EXISTS `transfer`;
DROP TABLE IF EXISTS `fantasyRound`;
DROP TABLE IF EXISTS `leagueMembership`;
DROP TABLE IF EXISTS `league`;
DROP TABLE IF EXISTS `playerRecommendation`;
DROP TABLE IF EXISTS `team_player_selection`;
DROP TABLE IF EXISTS `fantasyTeam`;
DROP TABLE IF EXISTS `playerAvailability`;
DROP TABLE IF EXISTS `player`;
DROP TABLE IF EXISTS `position`;
DROP TABLE IF EXISTS `club`;
DROP TABLE IF EXISTS `administrator`;
DROP TABLE IF EXISTS `registeredUser`;
DROP TABLE IF EXISTS `user`;

SET
FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `user`
(
    `userId`           VARCHAR(36)  NOT NULL,
    `email`            VARCHAR(255) NOT NULL,
    `passwordHash`     VARCHAR(255) NOT NULL,
    `username`         VARCHAR(100) NOT NULL,
    `role`             ENUM('REGISTERED_USER', 'ADMINISTRATOR') NOT NULL DEFAULT 'REGISTERED_USER',
    `isActive`         BOOLEAN      NOT NULL DEFAULT TRUE,
    `profilePic`       VARCHAR(255)          DEFAULT NULL,
    `registrationDate` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_login_at`    DATETIME              DEFAULT NULL,

    PRIMARY KEY (`userId`),
    UNIQUE KEY `uk_user_email` (`email`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `registeredUser`
(
    `userId`             VARCHAR(36) NOT NULL,
    `registrationStatus` ENUM('PENDING', 'ACTIVE', 'SUSPENDED', 'DEACTIVATED') NOT NULL DEFAULT 'ACTIVE',

    PRIMARY KEY (`userId`),

    CONSTRAINT `fk_registeredUser_user`
        FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `administrator`
(
    `userId`     VARCHAR(36) NOT NULL,
    `adminLevel` INT         NOT NULL DEFAULT 1,

    PRIMARY KEY (`userId`),

    CONSTRAINT `fk_administrator_user`
        FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `chk_administrator_adminLevel`
        CHECK (`adminLevel` >= 1)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `club`
(
    `clubId`    VARCHAR(36)  NOT NULL,
    `clubName`  VARCHAR(100) NOT NULL,
    `location`  VARCHAR(100)          DEFAULT NULL,
    `homeVenue` VARCHAR(100)          DEFAULT NULL,
    `isActive`  BOOLEAN      NOT NULL DEFAULT TRUE,

    PRIMARY KEY (`clubId`),
    UNIQUE KEY `uk_club_clubName` (`clubName`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `position`
(
    `positionId`       VARCHAR(36) NOT NULL,
    `positionName`     VARCHAR(50) NOT NULL,
    `positionCategory` VARCHAR(50) NOT NULL,
    `minRequired`      INT         NOT NULL DEFAULT 0,
    `maxAllowed`       INT         NOT NULL DEFAULT 8,

    PRIMARY KEY (`positionId`),
    UNIQUE KEY `uk_position_positionName` (`positionName`),

    CONSTRAINT `chk_position_required_range`
        CHECK (`minRequired` >= 0 AND `maxAllowed` >= `minRequired`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `player`
(
    `playerId`         VARCHAR(36)    NOT NULL,
    `clubId`           VARCHAR(36)    NOT NULL,
    `positionId`       VARCHAR(36)    NOT NULL,
    `playerName`       VARCHAR(100)   NOT NULL,
    `value`            DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `attackingAbility` INT            NOT NULL DEFAULT 50,
    `defensiveAbility` INT            NOT NULL DEFAULT 50,
    `kickingAbility`   INT            NOT NULL DEFAULT 50,
    `discipline`       INT            NOT NULL DEFAULT 50,
    `consistency`      INT            NOT NULL DEFAULT 50,
    `fitness`          INT            NOT NULL DEFAULT 50,
    `currentForm`      INT            NOT NULL DEFAULT 50,
    `isActive`         BOOLEAN        NOT NULL DEFAULT TRUE,

    PRIMARY KEY (`playerId`),
    KEY                `idx_player_club` (`clubId`),
    KEY                `idx_player_position` (`positionId`),
    KEY                `idx_player_search` (`playerName`, `value`),

    CONSTRAINT `fk_player_club`
        FOREIGN KEY (`clubId`) REFERENCES `club` (`clubId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `fk_player_position`
        FOREIGN KEY (`positionId`) REFERENCES `position` (`positionId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `chk_player_value`
        CHECK (`value` >= 0),

    CONSTRAINT `chk_player_ratings`
        CHECK (
            `attackingAbility` BETWEEN 0 AND 100
                AND `defensiveAbility` BETWEEN 0 AND 100
                AND `kickingAbility` BETWEEN 0 AND 100
                AND `discipline` BETWEEN 0 AND 100
                AND `consistency` BETWEEN 0 AND 100
                AND `fitness` BETWEEN 0 AND 100
                AND `currentForm` BETWEEN 0 AND 100
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `playerAvailability`
(
    `availabilityId` VARCHAR(36) NOT NULL,
    `playerId`       VARCHAR(36) NOT NULL,
    `status`         ENUM('ACTIVE', 'INJURED', 'SUSPENDED', 'UNAVAILABLE') NOT NULL DEFAULT 'ACTIVE',
    `effectiveDate`  DATE        NOT NULL DEFAULT (CURRENT_DATE),
    `endDate`        DATE                 DEFAULT NULL,
    `notes`          TEXT                 DEFAULT NULL,

    PRIMARY KEY (`availabilityId`),
    KEY              `idx_playerAvailability_player` (`playerId`),
    KEY              `idx_playerAvailability_status` (`status`),

    CONSTRAINT `fk_playerAvailability_player`
        FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `chk_playerAvailability_dates`
        CHECK (`endDate` IS NULL OR `endDate` >= `effectiveDate`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `fantasyTeam`
(
    `teamId`          VARCHAR(36)    NOT NULL,
    `owner_user_id`   VARCHAR(36)    NOT NULL,
    `teamName`        VARCHAR(100)   NOT NULL,
    -- Millions of rands, matching player.value. Keep in step with
    -- FantasyTeamServiceImpl.INITIAL_BUDGET.
    `remainingBudget` DECIMAL(10, 2) NOT NULL DEFAULT 250.00,
    `creationDate`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isValid`         BOOLEAN        NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`teamId`),
    UNIQUE KEY `uk_fantasyTeam_owner` (`owner_user_id`),
    UNIQUE KEY `uk_fantasyTeam_teamName` (`teamName`),
    UNIQUE KEY `uk_fantasyTeam_team_owner` (`teamId`, `owner_user_id`),

    CONSTRAINT `fk_fantasyTeam_owner`
        FOREIGN KEY (`owner_user_id`) REFERENCES `registeredUser` (`userId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `chk_fantasyTeam_budget`
        CHECK (`remainingBudget` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Current editable squad. Historical round squads are stored separately. */
CREATE TABLE `team_player_selection`
(
    `selectionId`          VARCHAR(36) NOT NULL,
    `teamId`               VARCHAR(36) NOT NULL,
    `playerId`             VARCHAR(36) NOT NULL,
    `selectedDate`         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `squadRole`            ENUM('STARTING', 'BENCH') NOT NULL DEFAULT 'STARTING',
    `isCaptain`            BOOLEAN     NOT NULL DEFAULT FALSE,
    `is_vice_captain`      BOOLEAN     NOT NULL DEFAULT FALSE,
    `captain_team_id`      VARCHAR(36) GENERATED ALWAYS AS (
        CASE WHEN `isCaptain` = TRUE THEN `teamId` ELSE NULL END
        ) STORED,
    `vice_captain_team_id` VARCHAR(36) GENERATED ALWAYS AS (
        CASE WHEN `is_vice_captain` = TRUE THEN `teamId` ELSE NULL END
        ) STORED,

    PRIMARY KEY (`selectionId`),
    UNIQUE KEY `uk_team_player_selection_team_player` (`teamId`, `playerId`),
    UNIQUE KEY `uk_team_player_selection_captain` (`captain_team_id`),
    UNIQUE KEY `uk_team_player_selection_vice_captain` (`vice_captain_team_id`),
    KEY                    `idx_team_player_selection_player` (`playerId`),

    CONSTRAINT `fk_team_player_selection_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `fk_team_player_selection_player`
        FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `chk_team_player_selection_captains`
        CHECK (NOT (`isCaptain` = TRUE AND `is_vice_captain` = TRUE))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `playerRecommendation`
(
    `recommendationId`      VARCHAR(36)   NOT NULL,
    `teamId`                VARCHAR(36)   NOT NULL,
    `current_player_id`     VARCHAR(36)            DEFAULT NULL,
    `recommended_player_id` VARCHAR(36)   NOT NULL,
    `reason`                TEXT                   DEFAULT NULL,
    `score`                 DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    `createdAt`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isDismissed`           BOOLEAN       NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`recommendationId`),
    KEY                     `idx_playerRecommendation_team` (`teamId`),
    KEY                     `idx_playerRecommendation_current_player` (`current_player_id`),
    KEY                     `idx_playerRecommendation_recommended_player` (`recommended_player_id`),

    CONSTRAINT `fk_playerRecommendation_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `fk_playerRecommendation_current_player`
        FOREIGN KEY (`current_player_id`) REFERENCES `player` (`playerId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `fk_playerRecommendation_recommended_player`
        FOREIGN KEY (`recommended_player_id`) REFERENCES `player` (`playerId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `chk_playerRecommendation_players_different`
        CHECK (`current_player_id` IS NULL OR `current_player_id` <> `recommended_player_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `league`
(
    `leagueId`        VARCHAR(36)  NOT NULL,
    `manager_user_id` VARCHAR(36)           DEFAULT NULL,
    `leagueName`      VARCHAR(100) NOT NULL,
    `description`     TEXT                  DEFAULT NULL,
    `leagueType`      ENUM('PUBLIC', 'PRIVATE') NOT NULL DEFAULT 'PUBLIC',
    `leagueCode`      VARCHAR(6)            DEFAULT NULL,
    `creationDate`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isActive`        BOOLEAN      NOT NULL DEFAULT TRUE,
    `maxMembers`      INT          NOT NULL DEFAULT 100,

    PRIMARY KEY (`leagueId`),
    UNIQUE KEY `uk_league_code` (`leagueCode`),
    KEY               `idx_league_manager` (`manager_user_id`),
    KEY               `idx_league_type_active` (`leagueType`, `isActive`),

    CONSTRAINT `fk_league_manager`
        FOREIGN KEY (`manager_user_id`) REFERENCES `registeredUser` (`userId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `chk_league_maxMembers`
        CHECK (`maxMembers` > 1),

    CONSTRAINT `chk_league_code_type`
        CHECK (
            (`leagueType` = 'PRIVATE' AND `leagueCode` IS NOT NULL)
                OR (`leagueType` = 'PUBLIC' AND `leagueCode` IS NULL)
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `leagueMembership`
(
    `membershipId`       VARCHAR(36) NOT NULL,
    `leagueId`           VARCHAR(36) NOT NULL,
    `registered_user_id` VARCHAR(36) NOT NULL,
    `teamId`             VARCHAR(36) NOT NULL,
    `isActive`           BOOLEAN     NOT NULL DEFAULT TRUE,
    `joinDate`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`membershipId`),
    UNIQUE KEY `uk_leagueMembership_user` (`leagueId`, `registered_user_id`),
    UNIQUE KEY `uk_leagueMembership_team` (`leagueId`, `teamId`),
    KEY                  `idx_leagueMembership_user` (`registered_user_id`),
    KEY                  `idx_leagueMembership_team` (`teamId`),

    CONSTRAINT `fk_leagueMembership_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `fk_leagueMembership_registeredUser`
        FOREIGN KEY (`registered_user_id`) REFERENCES `registeredUser` (`userId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `fk_leagueMembership_team_owner`
        FOREIGN KEY (`teamId`, `registered_user_id`)
            REFERENCES `fantasyTeam` (`teamId`, `owner_user_id`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/*
    Global fantasy gameweek. Transfers, locked squads, simulated fantasy
    fixtures, and league standings all reference the same round record.
*/
CREATE TABLE `fantasyRound`
(
    `roundId`      VARCHAR(36) NOT NULL,
    `season`       VARCHAR(20) NOT NULL,
    `roundNumber`  INT         NOT NULL,
    `openDate`     DATETIME    NOT NULL,
    `lockDeadline` DATETIME    NOT NULL,
    `endDate`      DATETIME DEFAULT NULL,
    `status`       ENUM('UPCOMING', 'OPEN', 'LOCKED', 'IN_PROGRESS', 'COMPLETED', 'PROCESSED', 'CANCELLED') NOT NULL DEFAULT 'UPCOMING',

    PRIMARY KEY (`roundId`),
    UNIQUE KEY `uk_fantasyRound_season_round` (`season`, `roundNumber`),
    KEY            `idx_fantasyRound_status` (`status`),

    CONSTRAINT `chk_fantasyRound_number`
        CHECK (`roundNumber` > 0),

    CONSTRAINT `chk_fantasyRound_dates`
        CHECK (
            `lockDeadline` >= `openDate`
                AND (`endDate` IS NULL OR `endDate` >= `lockDeadline`)
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Each transfer is already a historical record; a duplicate transferHistory table is unnecessary. */
CREATE TABLE `transfer`
(
    `transferId`           VARCHAR(36)    NOT NULL,
    `teamId`               VARCHAR(36)    NOT NULL,
    `roundId`              VARCHAR(36)    NOT NULL,
    `removed_player_id`    VARCHAR(36)    NOT NULL,
    `added_player_id`      VARCHAR(36)    NOT NULL,
    `transferDate`         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `removed_player_value` DECIMAL(10, 2) NOT NULL,
    `added_player_value`   DECIMAL(10, 2) NOT NULL,
    `valueDifference`      DECIMAL(10, 2) GENERATED ALWAYS AS (
        `added_player_value` - `removed_player_value`
        ) STORED,
    `penaltyPoints`        INT            NOT NULL DEFAULT 0,
    `status`               ENUM('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    `confirmedAt`          DATETIME                DEFAULT NULL,
    `created_by_user_id`   VARCHAR(36)    NOT NULL,

    PRIMARY KEY (`transferId`),
    KEY                    `idx_transfer_team_round` (`teamId`, `roundId`),
    KEY                    `idx_transfer_removed_player` (`removed_player_id`),
    KEY                    `idx_transfer_added_player` (`added_player_id`),
    KEY                    `idx_transfer_created_by` (`created_by_user_id`),

    CONSTRAINT `fk_transfer_team_owner`
        FOREIGN KEY (`teamId`, `created_by_user_id`)
            REFERENCES `fantasyTeam` (`teamId`, `owner_user_id`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `fk_transfer_round`
        FOREIGN KEY (`roundId`) REFERENCES `fantasyRound` (`roundId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `fk_transfer_removed_player`
        FOREIGN KEY (`removed_player_id`) REFERENCES `player` (`playerId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `fk_transfer_added_player`
        FOREIGN KEY (`added_player_id`) REFERENCES `player` (`playerId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `chk_transfer_players_different`
        CHECK (`removed_player_id` <> `added_player_id`),

    CONSTRAINT `chk_transfer_values`
        CHECK (
            `removed_player_value` >= 0
                AND `added_player_value` >= 0
                AND `penaltyPoints` >= 0
            ),

    CONSTRAINT `chk_transfer_confirmation`
        CHECK (
            (`status` = 'CONFIRMED' AND `confirmedAt` IS NOT NULL)
                OR (`status` <> 'CONFIRMED' AND `confirmedAt` IS NULL)
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `leaderboard`
(
    `leaderboardId` VARCHAR(36) NOT NULL,
    `leagueId`      VARCHAR(36)          DEFAULT NULL,
    `season`        VARCHAR(20) NOT NULL,
    `scope`         ENUM('MASTER', 'LEAGUE') NOT NULL,
    `scopeKey`      VARCHAR(36) GENERATED ALWAYS AS (COALESCE(`leagueId`, 'MASTER')) STORED,
    `lastUpdated`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`leaderboardId`),
    UNIQUE KEY `uk_leaderboard_scope_season` (`scopeKey`, `season`),
    KEY             `idx_leaderboard_league` (`leagueId`),

    CONSTRAINT `fk_leaderboard_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `chk_leaderboard_scope`
        CHECK (
            (`scope` = 'MASTER' AND `leagueId` IS NULL)
                OR (`scope` = 'LEAGUE' AND `leagueId` IS NOT NULL)
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Head-to-head standings rather than only cumulative fantasy-point rankings. */
CREATE TABLE `ranking`
(
    `rankingId`            VARCHAR(36) NOT NULL,
    `leaderboardId`        VARCHAR(36) NOT NULL,
    `teamId`               VARCHAR(36) NOT NULL,
    `currentRanking`       INT         NOT NULL,
    `previousRanking`      INT                  DEFAULT NULL,
    `matchesPlayed`        INT         NOT NULL DEFAULT 0,
    `matchesWon`           INT         NOT NULL DEFAULT 0,
    `matchesDrawn`         INT         NOT NULL DEFAULT 0,
    `matchesLost`          INT         NOT NULL DEFAULT 0,
    `pointsFor`            INT         NOT NULL DEFAULT 0,
    `pointsAgainst`        INT         NOT NULL DEFAULT 0,
    `scoreDifference`      INT GENERATED ALWAYS AS (`pointsFor` - `pointsAgainst`) STORED,
    `leaguePoints`         INT         NOT NULL DEFAULT 0,
    `total_fantasy_points` INT         NOT NULL DEFAULT 0,
    `updatedAt`            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`rankingId`),
    UNIQUE KEY `uk_ranking_leaderboard_team` (`leaderboardId`, `teamId`),
    UNIQUE KEY `uk_ranking_position` (`leaderboardId`, `currentRanking`),
    KEY                    `idx_ranking_team` (`teamId`),

    CONSTRAINT `fk_ranking_leaderboard`
        FOREIGN KEY (`leaderboardId`) REFERENCES `leaderboard` (`leaderboardId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `fk_ranking_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `chk_ranking_position`
        CHECK (`currentRanking` > 0 AND (`previousRanking` IS NULL OR `previousRanking` > 0)),

    CONSTRAINT `chk_ranking_counts`
        CHECK (
            `matchesPlayed` >= 0
                AND `matchesWon` >= 0
                AND `matchesDrawn` >= 0
                AND `matchesLost` >= 0
                AND `matchesPlayed` = `matchesWon` + `matchesDrawn` + `matchesLost`
            ),

    CONSTRAINT `chk_ranking_points`
        CHECK (
            `leaguePoints` >= 0
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Purely simulated head-to-head fixture between two fantasy teams in one league. */
CREATE TABLE `fixture`
(
    `fixtureId`      VARCHAR(36) NOT NULL,
    `leagueId`       VARCHAR(36) NOT NULL,
    `roundId`        VARCHAR(36) NOT NULL,
    `team_a_id`      VARCHAR(36) NOT NULL,
    `team_b_id`      VARCHAR(36) NOT NULL,
    `fixtureDate`    DATE        NOT NULL,
    `fixtureTime`    TIME        NOT NULL,
    `status`         ENUM('UPCOMING', 'LOCKED', 'SIMULATING', 'COMPLETED', 'PROCESSED', 'CANCELLED') NOT NULL DEFAULT 'UPCOMING',
    `simulationDate` DATETIME             DEFAULT NULL,
    `first_team_id`  VARCHAR(36) GENERATED ALWAYS AS (LEAST(`team_a_id`, `team_b_id`)) STORED,
    `second_team_id` VARCHAR(36) GENERATED ALWAYS AS (GREATEST(`team_a_id`, `team_b_id`)) STORED,
    `createdAt`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`fixtureId`),
    UNIQUE KEY `uk_fixture_round_teams` (`leagueId`, `roundId`, `first_team_id`, `second_team_id`),
    KEY              `idx_fixture_round` (`roundId`),
    KEY              `idx_fixture_team_a` (`team_a_id`),
    KEY              `idx_fixture_team_b` (`team_b_id`),
    KEY              `idx_fixture_status_date` (`status`, `fixtureDate`, `fixtureTime`),

    CONSTRAINT `fk_fixture_league`
        FOREIGN KEY (`leagueId`) REFERENCES `league` (`leagueId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `fk_fixture_round`
        FOREIGN KEY (`roundId`) REFERENCES `fantasyRound` (`roundId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `fk_fixture_team_a_membership`
        FOREIGN KEY (`leagueId`, `team_a_id`)
            REFERENCES `leagueMembership` (`leagueId`, `teamId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `fk_fixture_team_b_membership`
        FOREIGN KEY (`leagueId`, `team_b_id`)
            REFERENCES `leagueMembership` (`leagueId`, `teamId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `chk_fixture_teams_different`
        CHECK (`team_a_id` <> `team_b_id`),

    CONSTRAINT `chk_fixture_simulation_date`
        CHECK (
            (`status` IN ('COMPLETED', 'PROCESSED') AND `simulationDate` IS NOT NULL)
                OR (`status` NOT IN ('COMPLETED', 'PROCESSED'))
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `matchResult`
(
    `resultId`                  VARCHAR(36) NOT NULL,
    `fixtureId`                 VARCHAR(36) NOT NULL,
    `settingsId`                VARCHAR(36) NOT NULL,
    `team_a_score`              INT         NOT NULL,
    `team_b_score`              INT         NOT NULL,
    `winnerSide`                ENUM('TEAM_A', 'TEAM_B') DEFAULT NULL,
    `isDraw`                    BOOLEAN     NOT NULL DEFAULT FALSE,
    `resultDate`                DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `approved`                  BOOLEAN     NOT NULL DEFAULT FALSE,
    `approved_by_admin_user_id` VARCHAR(36)          DEFAULT NULL,
    `simulation_run_number`     INT         NOT NULL,
    `isCurrent`                 BOOLEAN     NOT NULL DEFAULT TRUE,
    `current_fixture_id`        VARCHAR(36) GENERATED ALWAYS AS (
        CASE WHEN `isCurrent` = TRUE THEN `fixtureId` ELSE NULL END
        ) STORED,

    PRIMARY KEY (`resultId`),
    UNIQUE KEY `uk_matchResult_fixture_run` (`fixtureId`, `simulation_run_number`),
    UNIQUE KEY `uk_matchResult_current_fixture` (`current_fixture_id`),
    KEY                         `idx_matchResult_fixture_current` (`fixtureId`, `isCurrent`),
    KEY                         `idx_matchResult_settings` (`settingsId`),
    KEY                         `idx_matchResult_approved_by` (`approved_by_admin_user_id`),

    CONSTRAINT `fk_matchResult_fixture`
        FOREIGN KEY (`fixtureId`) REFERENCES `fixture` (`fixtureId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `fk_matchResult_approved_by_admin`
        FOREIGN KEY (`approved_by_admin_user_id`) REFERENCES `administrator` (`userId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `chk_matchResult_simulation_run`
        CHECK (`simulation_run_number` > 0),

    CONSTRAINT `chk_matchResult_draw`
        CHECK (
            (`isDraw` = TRUE
                AND `team_a_score` = `team_b_score`
                AND `winnerSide` IS NULL)
                OR
            (`isDraw` = FALSE
                AND `team_a_score` > `team_b_score`
                AND `winnerSide` = 'TEAM_A')
                OR
            (`isDraw` = FALSE
                AND `team_b_score` > `team_a_score`
                AND `winnerSide` = 'TEAM_B')
            ),

    CONSTRAINT `chk_matchResult_approval`
        CHECK (
            (`approved` = TRUE AND `approved_by_admin_user_id` IS NOT NULL)
                OR (`approved` = FALSE AND `approved_by_admin_user_id` IS NULL)
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Auditable construction of each fantasy team's score for one simulation run. */
CREATE TABLE `match_team_score`
(
    `scoreId`         VARCHAR(36) NOT NULL,
    `resultId`        VARCHAR(36) NOT NULL,
    `teamId`          VARCHAR(36) NOT NULL,
    `teamSide`        ENUM('TEAM_A', 'TEAM_B') NOT NULL,
    `playerPoints`    INT         NOT NULL DEFAULT 0,
    `captainBonus`    INT         NOT NULL DEFAULT 0,
    `transferPenalty` INT         NOT NULL DEFAULT 0,
    `totalScore`      INT GENERATED ALWAYS AS (
        `playerPoints` + `captainBonus` - `transferPenalty`
        ) STORED,
    `calculatedAt`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`scoreId`),
    UNIQUE KEY `uk_match_team_score_result_team` (`resultId`, `teamId`),
    UNIQUE KEY `uk_match_team_score_result_side` (`resultId`, `teamSide`),
    KEY               `idx_match_team_score_team` (`teamId`),

    CONSTRAINT `fk_match_team_score_result`
        FOREIGN KEY (`resultId`) REFERENCES `matchResult` (`resultId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `fk_match_team_score_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `chk_match_team_score_penalty`
        CHECK (`transferPenalty` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE `playerStatistics`
(
    `statId`                     VARCHAR(36) NOT NULL,
    `resultId`                   VARCHAR(36) NOT NULL,
    `teamId`                     VARCHAR(36) NOT NULL,
    `playerId`                   VARCHAR(36) NOT NULL,
    `tries`                      INT         NOT NULL DEFAULT 0,
    `assists`                    INT         NOT NULL DEFAULT 0,
    `tackles`                    INT         NOT NULL DEFAULT 0,
    `missedTackles`              INT         NOT NULL DEFAULT 0,
    `conversions`                INT         NOT NULL DEFAULT 0,
    `penalties`                  INT         NOT NULL DEFAULT 0,
    `metersGained`               INT         NOT NULL DEFAULT 0,
    `yellowCards`                INT         NOT NULL DEFAULT 0,
    `redCards`                   INT         NOT NULL DEFAULT 0,
    `statisticDate`              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `corrected_by_admin_user_id` VARCHAR(36)          DEFAULT NULL,
    `correctionReason`           TEXT                 DEFAULT NULL,
    `correctedAt`                DATETIME             DEFAULT NULL,

    PRIMARY KEY (`statId`),
    UNIQUE KEY `uk_playerStatistics_result_team_player` (`resultId`, `teamId`, `playerId`),
    KEY                          `idx_playerStatistics_team` (`teamId`),
    KEY                          `idx_playerStatistics_player` (`playerId`),
    KEY                          `idx_playerStatistics_corrected_by` (`corrected_by_admin_user_id`),

    CONSTRAINT `fk_playerStatistics_result`
        FOREIGN KEY (`resultId`) REFERENCES `matchResult` (`resultId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `fk_playerStatistics_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `fk_playerStatistics_player`
        FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `fk_playerStatistics_corrected_by_admin`
        FOREIGN KEY (`corrected_by_admin_user_id`) REFERENCES `administrator` (`userId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `chk_playerStatistics_non_negative`
        CHECK (
            `tries` >= 0
                AND `assists` >= 0
                AND `tackles` >= 0
                AND `missedTackles` >= 0
                AND `conversions` >= 0
                AND `penalties` >= 0
                AND `metersGained` >= 0
                AND `yellowCards` >= 0
                AND `redCards` >= 0
            ),

    CONSTRAINT `chk_playerStatistics_correction`
        CHECK (
            (`corrected_by_admin_user_id` IS NULL
                AND `correctionReason` IS NULL
                AND `correctedAt` IS NULL)
                OR
            (`corrected_by_admin_user_id` IS NOT NULL
                AND `correctionReason` IS NOT NULL
                AND CHAR_LENGTH(TRIM(`correctionReason`)) > 0
                AND `correctedAt` IS NOT NULL)
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Immutable audit history for administrator corrections to simulated statistics. */
CREATE TABLE `player_statistics_correction`
(
    `correctionId`               VARCHAR(36) NOT NULL,
    `statId`                     VARCHAR(36) NOT NULL,
    `corrected_by_admin_user_id` VARCHAR(36)          DEFAULT NULL,
    `reason`                     TEXT        NOT NULL,
    `old_values_json`            JSON        NOT NULL,
    `new_values_json`            JSON        NOT NULL,
    `correctedAt`                DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`correctionId`),
    KEY                          `idx_player_statistics_correction_stat` (`statId`),
    KEY                          `idx_player_statistics_correction_admin` (`corrected_by_admin_user_id`),

    CONSTRAINT `fk_player_statistics_correction_stat`
        FOREIGN KEY (`statId`) REFERENCES `playerStatistics` (`statId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `fk_player_statistics_correction_admin`
        FOREIGN KEY (`corrected_by_admin_user_id`) REFERENCES `administrator` (`userId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


/* Global scoring configuration controlled by administrators. */
CREATE TABLE `scoringRule`
(
    `ruleId`        VARCHAR(36) NOT NULL,
    `season`        VARCHAR(20) NOT NULL,
    `eventType`     VARCHAR(50) NOT NULL,
    `pointsAwarded` INT         NOT NULL,
    `isDeduction`   BOOLEAN     NOT NULL DEFAULT FALSE,
    `description`   TEXT                 DEFAULT NULL,
    `isActive`      BOOLEAN     NOT NULL DEFAULT TRUE,

    PRIMARY KEY (`ruleId`),
    UNIQUE KEY `uk_scoringRule_season_event` (`season`, `eventType`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Final fantasy points calculated from one simulated player-statistics record. */
CREATE TABLE `fantasyPoints`
(
    `pointsId`           VARCHAR(36) NOT NULL,
    `statId`             VARCHAR(36) NOT NULL,
    `totalPoints`        INT         NOT NULL DEFAULT 0,
    `calculationVersion` INT         NOT NULL DEFAULT 1,
    `calculatedAt`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isFinal`            BOOLEAN     NOT NULL DEFAULT FALSE,
    `final_stat_id`      VARCHAR(36) GENERATED ALWAYS AS (
        CASE WHEN `isFinal` = TRUE THEN `statId` ELSE NULL END
        ) STORED,

    PRIMARY KEY (`pointsId`),
    UNIQUE KEY `uk_fantasyPoints_stat_version` (`statId`, `calculationVersion`),
    UNIQUE KEY `uk_fantasyPoints_final_stat` (`final_stat_id`),
    KEY                  `idx_fantasyPoints_final` (`isFinal`),

    CONSTRAINT `fk_fantasyPoints_statistics`
        FOREIGN KEY (`statId`) REFERENCES `playerStatistics` (`statId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `chk_fantasyPoints_version`
        CHECK (`calculationVersion` > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Audit breakdown showing how the player's final fixture points were calculated. */
CREATE TABLE `fantasy_point_breakdown`
(
    `breakdownId`  VARCHAR(36) NOT NULL,
    `pointsId`     VARCHAR(36) NOT NULL,
    `ruleId`       VARCHAR(36) NOT NULL,
    `eventCount`   INT         NOT NULL DEFAULT 0,
    `pointsEarned` INT         NOT NULL DEFAULT 0,

    PRIMARY KEY (`breakdownId`),
    UNIQUE KEY `uk_fantasy_point_breakdown_rule` (`pointsId`, `ruleId`),
    KEY            `idx_fantasy_point_breakdown_rule` (`ruleId`),

    CONSTRAINT `fk_fantasy_point_breakdown_points`
        FOREIGN KEY (`pointsId`) REFERENCES `fantasyPoints` (`pointsId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `fk_fantasy_point_breakdown_rule`
        FOREIGN KEY (`ruleId`) REFERENCES `scoringRule` (`ruleId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `chk_fantasy_point_breakdown_count`
        CHECK (`eventCount` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Immutable snapshot of the fantasy team's squad when a round locks. */
CREATE TABLE `fantasy_team_round_selection`
(
    `selectionId`          VARCHAR(36) NOT NULL,
    `roundId`              VARCHAR(36) NOT NULL,
    `teamId`               VARCHAR(36) NOT NULL,
    `playerId`             VARCHAR(36) NOT NULL,
    `selectedDate`         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `squadRole`            ENUM('STARTING', 'BENCH') NOT NULL,
    `isCaptain`            BOOLEAN     NOT NULL DEFAULT FALSE,
    `is_vice_captain`      BOOLEAN     NOT NULL DEFAULT FALSE,
    `lockedAt`             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `captain_team_id`      VARCHAR(36) GENERATED ALWAYS AS (
        CASE WHEN `isCaptain` = TRUE THEN `teamId` ELSE NULL END
        ) STORED,
    `vice_captain_team_id` VARCHAR(36) GENERATED ALWAYS AS (
        CASE WHEN `is_vice_captain` = TRUE THEN `teamId` ELSE NULL END
        ) STORED,

    PRIMARY KEY (`selectionId`),
    UNIQUE KEY `uk_fantasy_team_round_selection_player` (`roundId`, `teamId`, `playerId`),
    UNIQUE KEY `uk_fantasy_team_round_selection_captain` (`roundId`, `captain_team_id`),
    UNIQUE KEY `uk_fantasy_team_round_selection_vice_captain` (`roundId`, `vice_captain_team_id`),
    KEY                    `idx_fantasy_team_round_selection_team` (`teamId`),
    KEY                    `idx_fantasy_team_round_selection_player` (`playerId`),

    CONSTRAINT `fk_fantasy_team_round_selection_round`
        FOREIGN KEY (`roundId`) REFERENCES `fantasyRound` (`roundId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `fk_fantasy_team_round_selection_team`
        FOREIGN KEY (`teamId`) REFERENCES `fantasyTeam` (`teamId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT `fk_fantasy_team_round_selection_player`
        FOREIGN KEY (`playerId`) REFERENCES `player` (`playerId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,

    CONSTRAINT `chk_fantasy_team_round_selection_captains`
        CHECK (NOT (`isCaptain` = TRUE AND `is_vice_captain` = TRUE))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `notification`
(
    `notificationId`      VARCHAR(36) NOT NULL,
    `userId`              VARCHAR(36) NOT NULL,
    `type`                ENUM(
                                'LEADERBOARD_CHANGE',
                                'POINTS_UPDATE',
                                'MATCHUP_RESULT',
                                'SIMULATED_RESULT',
                                'PLAYER_AVAILABILITY',
                                'TRANSFER_DEADLINE',
                                'ROUND_LOCK',
                                'SYSTEM'
                            ) NOT NULL,
    `body`                TEXT        NOT NULL,
    `createdAt`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isRead`              BOOLEAN     NOT NULL DEFAULT FALSE,
    `related_entity_type` VARCHAR(50)          DEFAULT NULL,
    `related_entity_id`   VARCHAR(36)          DEFAULT NULL,

    PRIMARY KEY (`notificationId`),
    KEY                   `idx_notification_user_read` (`userId`, `isRead`, `createdAt`),

    CONSTRAINT `fk_notification_user`
        FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `chk_notification_related_entity`
        CHECK (
            (`related_entity_type` IS NULL AND `related_entity_id` IS NULL)
                OR (`related_entity_type` IS NOT NULL AND `related_entity_id` IS NOT NULL)
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `log`
(
    `logId`          VARCHAR(36) NOT NULL,
    `userId`         VARCHAR(36)          DEFAULT NULL,
    `transferId`     VARCHAR(36)          DEFAULT NULL,
    `notificationId` VARCHAR(36)          DEFAULT NULL,
    `entityType`     VARCHAR(50) NOT NULL,
    `entityId`       VARCHAR(36)          DEFAULT NULL,
    `actionType`     VARCHAR(50) NOT NULL,
    `description`    TEXT                 DEFAULT NULL,
    `createdAt`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ipAddress`      VARCHAR(45)          DEFAULT NULL,

    PRIMARY KEY (`logId`),
    KEY              `idx_log_user` (`userId`),
    KEY              `idx_log_transfer` (`transferId`),
    KEY              `idx_log_notification` (`notificationId`),
    KEY              `idx_log_entity` (`entityType`, `entityId`),
    KEY              `idx_log_createdAt` (`createdAt`),

    CONSTRAINT `fk_log_user`
        FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
            ON DELETE SET NULL
            ON UPDATE CASCADE,

    CONSTRAINT `fk_log_transfer`
        FOREIGN KEY (`transferId`) REFERENCES `transfer` (`transferId`)
            ON DELETE SET NULL
            ON UPDATE CASCADE,

    CONSTRAINT `fk_log_notification`
        FOREIGN KEY (`notificationId`) REFERENCES `notification` (`notificationId`)
            ON DELETE SET NULL
            ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Event history only; fantasyRound.status remains the authoritative current state. */
CREATE TABLE `roundLock`
(
    `lockId`                  VARCHAR(36) NOT NULL,
    `roundId`                 VARCHAR(36) NOT NULL,
    `lockAction`              ENUM('LOCKED', 'UNLOCKED') NOT NULL,
    `action_by_admin_user_id` VARCHAR(36)          DEFAULT NULL,
    `actionAt`                DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `reason`                  TEXT                 DEFAULT NULL,

    PRIMARY KEY (`lockId`),
    KEY                       `idx_roundLock_round_action` (`roundId`, `actionAt`),
    KEY                       `idx_roundLock_admin` (`action_by_admin_user_id`),

    CONSTRAINT `fk_roundLock_round`
        FOREIGN KEY (`roundId`) REFERENCES `fantasyRound` (`roundId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    CONSTRAINT `fk_roundLock_admin`
        FOREIGN KEY (`action_by_admin_user_id`) REFERENCES `administrator` (`userId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Global settings controlling purely simulated fantasy-team fixtures. */
CREATE TABLE `simulationSettings`
(
    `settingsId`              VARCHAR(36)   NOT NULL,
    `season`                  VARCHAR(20)   NOT NULL,
    `settingsVersion`         INT           NOT NULL DEFAULT 1,
    `player_ability_weight`   DECIMAL(5, 2) NOT NULL DEFAULT 35.00,
    `player_form_weight`      DECIMAL(5, 2) NOT NULL DEFAULT 25.00,
    `team_balance_weight`     DECIMAL(5, 2) NOT NULL DEFAULT 20.00,
    `random_variation_weight` DECIMAL(5, 2) NOT NULL DEFAULT 20.00,
    `require_admin_approval`  BOOLEAN       NOT NULL DEFAULT TRUE,
    `allowResimulation`       BOOLEAN       NOT NULL DEFAULT FALSE,
    `maxResimulations`        INT           NOT NULL DEFAULT 0,
    `isActive`                BOOLEAN       NOT NULL DEFAULT TRUE,
    `createdAt`               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt`               DATETIME               DEFAULT NULL,
    `activeSeason`            VARCHAR(20) GENERATED ALWAYS AS (
        CASE WHEN `isActive` = TRUE THEN `season` ELSE NULL END
        ) STORED,

    PRIMARY KEY (`settingsId`),
    UNIQUE KEY `uk_simulationSettings_season_version` (`season`, `settingsVersion`),
    UNIQUE KEY `uk_simulationSettings_active_season` (`activeSeason`),

    CONSTRAINT `chk_simulationSettings_version`
        CHECK (`settingsVersion` > 0),

    CONSTRAINT `chk_simulationSettings_weights`
        CHECK (
            `player_ability_weight` >= 0
                AND `player_form_weight` >= 0
                AND `team_balance_weight` >= 0
                AND `random_variation_weight` >= 0
                AND `player_ability_weight`
                        + `player_form_weight`
                        + `team_balance_weight`
                        + `random_variation_weight` = 100.00
            ),

    CONSTRAINT `chk_simulationSettings_resimulations`
        CHECK (`maxResimulations` >= 0),

    CONSTRAINT `chk_simulationSettings_resimulation_enabled`
        CHECK (
            (`allowResimulation` = TRUE AND `maxResimulations` > 0)
                OR (`allowResimulation` = FALSE AND `maxResimulations` = 0)
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

/* Every result records the exact settings version used for that simulation run. */
ALTER TABLE `matchResult`
    ADD CONSTRAINT `fk_matchResult_settings`
        FOREIGN KEY (`settingsId`) REFERENCES `simulationSettings` (`settingsId`)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT;

CREATE TABLE `systemReport`
(
    `reportId`                   VARCHAR(36)  NOT NULL,
    `generated_by_admin_user_id` VARCHAR(36)           DEFAULT NULL,
    `reportType`                 ENUM(
                                     'ACTIVE_USERS',
                                     'ACTIVE_LEAGUES',
                                     'TOP_FANTASY_TEAMS',
                                     'TOP_RUGBY_PLAYERS',
                                     'MOST_SELECTED_PLAYERS',
                                     'UNAVAILABLE_PLAYERS',
                                     'COMPLETED_FIXTURES',
                                     'FIXTURE_RESULTS',
                                     'TRANSFER_ACTIVITY',
                                     'SYSTEM_ACTIVITY'
                                 ) NOT NULL,
    `reportTitle`                VARCHAR(150) NOT NULL,
    `parametersJson`             JSON                  DEFAULT NULL,
    `resultJson`                 JSON                  DEFAULT NULL,
    `generatedAt`                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`reportId`),
    KEY                          `idx_systemReport_generated_by` (`generated_by_admin_user_id`),
    KEY                          `idx_systemReport_type_date` (`reportType`, `generatedAt`),

    CONSTRAINT `fk_systemReport_generated_by_admin`
        FOREIGN KEY (`generated_by_admin_user_id`) REFERENCES `administrator` (`userId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


/*
    DATABASE-LEVEL INTEGRITY GUARDS

    The service layer remains responsible for transactions and business
    orchestration. These triggers prevent records that would contradict the
    confirmed fantasy-team-versus-fantasy-team model.
*/

DELIMITER $$

CREATE TRIGGER `trg_registeredUser_role_insert`
    BEFORE INSERT
    ON `registeredUser`
    FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM `user`
        WHERE `userId` = NEW.`userId`
          AND `role` = 'REGISTERED_USER'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'registeredUser requires a REGISTERED_USER base user';
END IF;

IF
EXISTS (
        SELECT 1 FROM `administrator`
        WHERE `userId` = NEW.`userId`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A user cannot be both registeredUser and administrator';
END IF;
END$$

CREATE TRIGGER `trg_administrator_role_insert`
    BEFORE INSERT
    ON `administrator`
    FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM `user`
        WHERE `userId` = NEW.`userId`
          AND `role` = 'ADMINISTRATOR'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'administrator requires an ADMINISTRATOR base user';
END IF;

IF
EXISTS (
        SELECT 1 FROM `registeredUser`
        WHERE `userId` = NEW.`userId`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A user cannot be both administrator and registeredUser';
END IF;
END$$

CREATE TRIGGER `trg_user_role_update`
    BEFORE UPDATE
    ON `user`
    FOR EACH ROW
BEGIN
    IF NEW.`role` <> OLD.`role` THEN
        IF EXISTS (
            SELECT 1 FROM `registeredUser`
            WHERE `userId` = OLD.`userId`
        ) AND NEW.`role` <> 'REGISTERED_USER' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Remove the registeredUser subtype before changing the base role';
END IF;

IF
EXISTS (
            SELECT 1 FROM `administrator`
            WHERE `userId` = OLD.`userId`
        ) AND NEW.`role` <> 'ADMINISTRATOR' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Remove the administrator subtype before changing the base role';
END IF;
END IF;
END$$

CREATE TRIGGER `trg_league_manager_insert`
    BEFORE INSERT
    ON `league`
    FOR EACH ROW
BEGIN
    IF NEW.`manager_user_id` IS NOT NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Create the league, add its manager membership, then assign manager_user_id';
END IF;
END$$

CREATE TRIGGER `trg_league_manager_update`
    BEFORE UPDATE
    ON `league`
    FOR EACH ROW
BEGIN
    IF NEW.`manager_user_id` IS NOT NULL
       AND NOT (NEW.`manager_user_id` <=> OLD.`manager_user_id`)
       AND NOT EXISTS (
            SELECT 1
            FROM `leagueMembership`
            WHERE `leagueId` = NEW.`leagueId`
              AND `registered_user_id` = NEW.`manager_user_id`
              AND `isActive` = TRUE
       ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'League manager must be an active member of the league';
END IF;
END$$

CREATE TRIGGER `trg_leagueMembership_manager_update`
    BEFORE UPDATE
    ON `leagueMembership`
    FOR EACH ROW
BEGIN
    IF NEW.`leagueId` <> OLD.`leagueId`
       OR NEW.`registered_user_id` <> OLD.`registered_user_id`
       OR NEW.`teamId` <> OLD.`teamId` THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'League membership identity fields are immutable';
END IF;

IF
OLD.`isActive` = TRUE
       AND NEW.`isActive` = FALSE
       AND EXISTS (
            SELECT 1 FROM `league`
            WHERE `leagueId` = OLD.`leagueId`
              AND `manager_user_id` = OLD.`registered_user_id`
       ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Assign a new league manager before deactivating the current manager';
END IF;
END$$

CREATE TRIGGER `trg_leagueMembership_manager_delete`
    BEFORE DELETE
    ON `leagueMembership`
    FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM `league`
        WHERE `leagueId` = OLD.`leagueId`
          AND `manager_user_id` = OLD.`registered_user_id`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Assign a new league manager before deleting the current manager membership';
END IF;
END$$

CREATE TRIGGER `trg_fixture_integrity_insert`
    BEFORE INSERT
    ON `fixture`
    FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM `leagueMembership`
        WHERE `leagueId` = NEW.`leagueId`
          AND `teamId` = NEW.`team_a_id`
          AND `isActive` = TRUE
    ) OR NOT EXISTS (
        SELECT 1 FROM `leagueMembership`
        WHERE `leagueId` = NEW.`leagueId`
          AND `teamId` = NEW.`team_b_id`
          AND `isActive` = TRUE
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Both fixture teams must be active league members';
END IF;

IF
EXISTS (
        SELECT 1 FROM `fixture`
        WHERE `leagueId` = NEW.`leagueId`
          AND `roundId` = NEW.`roundId`
          AND (
                `team_a_id` IN (NEW.`team_a_id`, NEW.`team_b_id`)
                OR `team_b_id` IN (NEW.`team_a_id`, NEW.`team_b_id`)
          )
          AND `status` <> 'CANCELLED'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A fantasy team can only appear once per league round';
END IF;
END$$

CREATE TRIGGER `trg_fixture_integrity_update`
    BEFORE UPDATE
    ON `fixture`
    FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM `matchResult`
        WHERE `fixtureId` = OLD.`fixtureId`
    ) AND (
        NEW.`leagueId` <> OLD.`leagueId`
        OR NEW.`roundId` <> OLD.`roundId`
        OR NEW.`team_a_id` <> OLD.`team_a_id`
        OR NEW.`team_b_id` <> OLD.`team_b_id`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Fixture participants, league, and round cannot change after simulation history exists';
END IF;

IF
NOT EXISTS (
        SELECT 1 FROM `leagueMembership`
        WHERE `leagueId` = NEW.`leagueId`
          AND `teamId` = NEW.`team_a_id`
          AND `isActive` = TRUE
    ) OR NOT EXISTS (
        SELECT 1 FROM `leagueMembership`
        WHERE `leagueId` = NEW.`leagueId`
          AND `teamId` = NEW.`team_b_id`
          AND `isActive` = TRUE
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Both fixture teams must be active league members';
END IF;

    IF
EXISTS (
        SELECT 1 FROM `fixture`
        WHERE `leagueId` = NEW.`leagueId`
          AND `roundId` = NEW.`roundId`
          AND `fixtureId` <> OLD.`fixtureId`
          AND (
                `team_a_id` IN (NEW.`team_a_id`, NEW.`team_b_id`)
                OR `team_b_id` IN (NEW.`team_a_id`, NEW.`team_b_id`)
          )
          AND `status` <> 'CANCELLED'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A fantasy team can only appear once per league round';
END IF;
END$$

CREATE TRIGGER `trg_matchResult_integrity_insert`
    BEFORE INSERT
    ON `matchResult`
    FOR EACH ROW
BEGIN
    IF NEW.`approved` = TRUE THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insert the result unapproved, add both team scores, then approve it';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM `fixture` f
        JOIN `fantasyRound` fr ON fr.`roundId` = f.`roundId`
        JOIN `simulationSettings` ss
          ON ss.`settingsId` = NEW.`settingsId`
         AND ss.`season` = fr.`season`
         AND ss.`isActive` = TRUE
        WHERE f.`fixtureId` = NEW.`fixtureId`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'An active simulation-settings version for the fixture season is required';
    END IF;

    IF NEW.`simulation_run_number` > 1
       AND NOT EXISTS (
            SELECT 1
            FROM `simulationSettings`
            WHERE `settingsId` = NEW.`settingsId`
              AND `allowResimulation` = TRUE
              AND NEW.`simulation_run_number` <= `maxResimulations` + 1
       ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'The selected settings do not permit this resimulation run';
    END IF;
END$$

CREATE TRIGGER `trg_match_team_score_insert`
    BEFORE INSERT
    ON `match_team_score`
    FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM `matchResult` mr
        JOIN `fixture` f ON f.`fixtureId` = mr.`fixtureId`
        WHERE mr.`resultId` = NEW.`resultId`
          AND (
                (NEW.`teamId` = f.`team_a_id`
                    AND NEW.`teamSide` = 'TEAM_A'
                    AND NEW.`playerPoints` + NEW.`captainBonus` - NEW.`transferPenalty` = mr.`team_a_score`)
                OR
                (NEW.`teamId` = f.`team_b_id`
                    AND NEW.`teamSide` = 'TEAM_B'
                    AND NEW.`playerPoints` + NEW.`captainBonus` - NEW.`transferPenalty` = mr.`team_b_score`)
          )
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Match score must belong to a fixture participant and match the stored result score';
END IF;
END$$

CREATE TRIGGER `trg_match_team_score_update`
    BEFORE UPDATE
    ON `match_team_score`
    FOR EACH ROW
BEGIN
    IF NEW.`resultId` <> OLD.`resultId` OR NEW.`teamId` <> OLD.`teamId` THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Match score result and team identity are immutable';
END IF;

IF
    NOT EXISTS (
        SELECT 1
        FROM `matchResult` mr
        JOIN `fixture` f ON f.`fixtureId` = mr.`fixtureId`
        WHERE mr.`resultId` = NEW.`resultId`
          AND (
                (NEW.`teamId` = f.`team_a_id`
                    AND NEW.`teamSide` = 'TEAM_A')
                OR
                (NEW.`teamId` = f.`team_b_id`
                    AND NEW.`teamSide` = 'TEAM_B')
          )
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Updated match score must retain the fixture participant and team side';
END IF;
END$$

CREATE TRIGGER `trg_matchResult_score_update`
    BEFORE UPDATE
    ON `matchResult`
    FOR EACH ROW
BEGIN
    IF NEW.`fixtureId` <> OLD.`fixtureId`
       OR NEW.`settingsId` <> OLD.`settingsId`
       OR NEW.`simulation_run_number` <> OLD.`simulation_run_number` THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Result fixture, settings version, and simulation run are immutable';
    END IF;

    IF EXISTS (
        SELECT 1 FROM `match_team_score`
        WHERE `resultId` = OLD.`resultId`
          AND `teamId` = (
                SELECT `team_a_id` FROM `fixture`
                WHERE `fixtureId` = OLD.`fixtureId`
          )
          AND `totalScore` <> NEW.`team_a_score`
    ) OR EXISTS (
        SELECT 1 FROM `match_team_score`
        WHERE `resultId` = OLD.`resultId`
          AND `teamId` = (
                SELECT `team_b_id` FROM `fixture`
                WHERE `fixtureId` = OLD.`fixtureId`
          )
          AND `totalScore` <> NEW.`team_b_score`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Result scores must match their existing score breakdown records';
END IF;

IF
NEW.`approved` = TRUE AND OLD.`approved` = FALSE
       AND (
            SELECT COUNT(*) FROM `match_team_score`
            WHERE `resultId` = NEW.`resultId`
       ) <> 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A result requires exactly two team score breakdowns before approval';
END IF;
END$$

CREATE TRIGGER `trg_playerStatistics_integrity_insert`
    BEFORE INSERT
    ON `playerStatistics`
    FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM `matchResult` mr
        JOIN `fixture` f ON f.`fixtureId` = mr.`fixtureId`
        WHERE mr.`resultId` = NEW.`resultId`
          AND NEW.`teamId` IN (f.`team_a_id`, f.`team_b_id`)
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Statistics team must participate in the result fixture';
END IF;

IF
NOT EXISTS (
        SELECT 1
        FROM `matchResult` mr
        JOIN `fixture` f ON f.`fixtureId` = mr.`fixtureId`
        JOIN `fantasy_team_round_selection` frs
          ON frs.`roundId` = f.`roundId`
         AND frs.`teamId` = NEW.`teamId`
         AND frs.`playerId` = NEW.`playerId`
        WHERE mr.`resultId` = NEW.`resultId`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Statistics player must belong to the team locked squad for the fixture round';
END IF;
END$$

CREATE TRIGGER `trg_playerStatistics_integrity_update`
    BEFORE UPDATE
    ON `playerStatistics`
    FOR EACH ROW
BEGIN
    IF NEW.`resultId` <> OLD.`resultId`
       OR NEW.`teamId` <> OLD.`teamId`
       OR NEW.`playerId` <> OLD.`playerId` THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Statistics result, team, and player identity are immutable';
END IF;

IF
NOT (
        NEW.`tries` <=> OLD.`tries`
        AND NEW.`assists` <=> OLD.`assists`
        AND NEW.`tackles` <=> OLD.`tackles`
        AND NEW.`missedTackles` <=> OLD.`missedTackles`
        AND NEW.`conversions` <=> OLD.`conversions`
        AND NEW.`penalties` <=> OLD.`penalties`
        AND NEW.`metersGained` <=> OLD.`metersGained`
        AND NEW.`yellowCards` <=> OLD.`yellowCards`
        AND NEW.`redCards` <=> OLD.`redCards`
    ) THEN
        IF NEW.`corrected_by_admin_user_id` IS NULL
           OR NEW.`correctionReason` IS NULL
           OR CHAR_LENGTH(TRIM(NEW.`correctionReason`)) = 0
           OR NEW.`correctedAt` IS NULL
           OR (OLD.`correctedAt` IS NOT NULL AND NEW.`correctedAt` <= OLD.`correctedAt`) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Statistic changes require a new administrator, reason, and correction timestamp';
END IF;
END IF;
END$$

CREATE TRIGGER `trg_playerStatistics_correction_history`
    AFTER UPDATE
    ON `playerStatistics`
    FOR EACH ROW
BEGIN
    IF NOT (
        NEW.`tries` <=> OLD.`tries`
        AND NEW.`assists` <=> OLD.`assists`
        AND NEW.`tackles` <=> OLD.`tackles`
        AND NEW.`missedTackles` <=> OLD.`missedTackles`
        AND NEW.`conversions` <=> OLD.`conversions`
        AND NEW.`penalties` <=> OLD.`penalties`
        AND NEW.`metersGained` <=> OLD.`metersGained`
        AND NEW.`yellowCards` <=> OLD.`yellowCards`
        AND NEW.`redCards` <=> OLD.`redCards`
    ) THEN
        INSERT INTO `player_statistics_correction`
        (
            `correctionId`,
            `statId`,
            `corrected_by_admin_user_id`,
            `reason`,
            `old_values_json`,
            `new_values_json`,
            `correctedAt`
        )
        VALUES
        (
            UUID(),
            NEW.`statId`,
            NEW.`corrected_by_admin_user_id`,
            NEW.`correctionReason`,
            JSON_OBJECT(
                'tries', OLD.`tries`,
                'assists', OLD.`assists`,
                'tackles', OLD.`tackles`,
                'missedTackles', OLD.`missedTackles`,
                'conversions', OLD.`conversions`,
                'penalties', OLD.`penalties`,
                'metersGained', OLD.`metersGained`,
                'yellowCards', OLD.`yellowCards`,
                'redCards', OLD.`redCards`
            ),
            JSON_OBJECT(
                'tries', NEW.`tries`,
                'assists', NEW.`assists`,
                'tackles', NEW.`tackles`,
                'missedTackles', NEW.`missedTackles`,
                'conversions', NEW.`conversions`,
                'penalties', NEW.`penalties`,
                'metersGained', NEW.`metersGained`,
                'yellowCards', NEW.`yellowCards`,
                'redCards', NEW.`redCards`
            ),
            NEW.`correctedAt`
        );
END IF;
END$$

CREATE TRIGGER `trg_round_selection_insert`
    BEFORE INSERT
    ON `fantasy_team_round_selection`
    FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM `fantasyRound`
        WHERE `roundId` = NEW.`roundId`
          AND `status` = 'LOCKED'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Round selections may only be captured while the fantasy round is locked';
END IF;

IF
EXISTS (
        SELECT 1
        FROM `fixture` f
        JOIN `matchResult` mr ON mr.`fixtureId` = f.`fixtureId`
        WHERE f.`roundId` = NEW.`roundId`
          AND NEW.`teamId` IN (f.`team_a_id`, f.`team_b_id`)
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A locked squad cannot be extended after simulation history exists';
END IF;
END$$

CREATE TRIGGER `trg_round_selection_immutable_update`
    BEFORE UPDATE
    ON `fantasy_team_round_selection`
    FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Locked fantasy-team round selections are immutable';
END$$

    CREATE TRIGGER `trg_round_selection_immutable_delete`
        BEFORE DELETE
        ON `fantasy_team_round_selection`
        FOR EACH ROW
    BEGIN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Locked fantasy-team round selections cannot be deleted';
END$$

        CREATE TRIGGER `trg_ranking_membership_insert`
            BEFORE INSERT
            ON `ranking`
            FOR EACH ROW
        BEGIN
            IF EXISTS (
        SELECT 1 FROM `leaderboard`
        WHERE `leaderboardId` = NEW.`leaderboardId`
          AND `scope` = 'LEAGUE'
    ) AND NOT EXISTS (
        SELECT 1
        FROM `leaderboard` lb
        JOIN `leagueMembership` lm
          ON lm.`leagueId` = lb.`leagueId`
         AND lm.`teamId` = NEW.`teamId`
        WHERE lb.`leaderboardId` = NEW.`leaderboardId`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'League ranking team must belong to the league';
        END IF;
        END$$

        CREATE TRIGGER `trg_ranking_membership_update`
            BEFORE UPDATE
            ON `ranking`
            FOR EACH ROW
        BEGIN
            IF EXISTS (
        SELECT 1 FROM `leaderboard`
        WHERE `leaderboardId` = NEW.`leaderboardId`
          AND `scope` = 'LEAGUE'
    ) AND NOT EXISTS (
        SELECT 1
        FROM `leaderboard` lb
        JOIN `leagueMembership` lm
          ON lm.`leagueId` = lb.`leagueId`
         AND lm.`teamId` = NEW.`teamId`
        WHERE lb.`leaderboardId` = NEW.`leaderboardId`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'League ranking team must belong to the league';
        END IF;
        END$$

        CREATE TRIGGER `trg_fantasy_point_breakdown_season_insert`
            BEFORE INSERT
            ON `fantasy_point_breakdown`
            FOR EACH ROW
        BEGIN
            IF NOT EXISTS (
        SELECT 1
        FROM `scoringRule` sr
        JOIN `fantasyPoints` fp ON fp.`pointsId` = NEW.`pointsId`
        JOIN `playerStatistics` ps ON ps.`statId` = fp.`statId`
        JOIN `matchResult` mr ON mr.`resultId` = ps.`resultId`
        JOIN `fixture` f ON f.`fixtureId` = mr.`fixtureId`
        JOIN `fantasyRound` fr ON fr.`roundId` = f.`roundId`
        WHERE sr.`ruleId` = NEW.`ruleId`
          AND sr.`season` = fr.`season`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Scoring rule season must match the fixture round season';
        END IF;
        END$$

CREATE TRIGGER `trg_fantasy_point_breakdown_season_update`
            BEFORE UPDATE
            ON `fantasy_point_breakdown`
            FOR EACH ROW
        BEGIN
            IF NEW.`pointsId` <> OLD.`pointsId` OR NEW.`ruleId` <> OLD.`ruleId` THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Fantasy point breakdown source identities are immutable';
        END IF;
        END$$

CREATE TRIGGER `trg_simulationSettings_version_update`
    BEFORE UPDATE
    ON `simulationSettings`
    FOR EACH ROW
BEGIN
    IF NEW.`settingsId` <> OLD.`settingsId`
       OR NEW.`season` <> OLD.`season`
       OR NEW.`settingsVersion` <> OLD.`settingsVersion` THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Simulation-settings identity and version fields are immutable';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM `matchResult`
        WHERE `settingsId` = OLD.`settingsId`
    ) AND NOT (
        NEW.`player_ability_weight` <=> OLD.`player_ability_weight`
        AND NEW.`player_form_weight` <=> OLD.`player_form_weight`
        AND NEW.`team_balance_weight` <=> OLD.`team_balance_weight`
        AND NEW.`random_variation_weight` <=> OLD.`random_variation_weight`
        AND NEW.`require_admin_approval` <=> OLD.`require_admin_approval`
        AND NEW.`allowResimulation` <=> OLD.`allowResimulation`
        AND NEW.`maxResimulations` <=> OLD.`maxResimulations`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Create a new settings version instead of changing settings used by results';
    END IF;
END$$

/*
  Scoring rules are the inputs that produce fantasy points, and fantasy points must stay equal to the match
  scores stored on matchResult (see trg_match_team_score_insert). A simulated match score is calculated from
  the rule set active for its season, and the point breakdown is recalculated from that same set later, at
  processing time. If the rule set changed in between, the two would disagree and the breakdown insert would
  be rejected, leaving the fixture unprocessable.

  These triggers close that window by making a season's rule set immutable once any result exists for it,
  matching how trg_simulationSettings_version_update already protects the other scoring input.
  `description` is deliberately still editable, as it does not affect any calculation.
*/
CREATE TRIGGER `trg_scoringRule_season_locked_insert`
    BEFORE INSERT
    ON `scoringRule`
    FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1
        FROM `matchResult` mr
                 JOIN `fixture` f ON f.`fixtureId` = mr.`fixtureId`
                 JOIN `fantasyRound` fr ON fr.`roundId` = f.`roundId`
        WHERE fr.`season` = NEW.`season`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Start a new season ruleset instead of adding scoring rules to a season that already has results';
    END IF;
END$$

CREATE TRIGGER `trg_scoringRule_season_locked_update`
    BEFORE UPDATE
    ON `scoringRule`
    FOR EACH ROW
BEGIN
    IF NOT (
        NEW.`season` <=> OLD.`season`
        AND NEW.`eventType` <=> OLD.`eventType`
        AND NEW.`pointsAwarded` <=> OLD.`pointsAwarded`
        AND NEW.`isDeduction` <=> OLD.`isDeduction`
        AND NEW.`isActive` <=> OLD.`isActive`
    ) AND EXISTS (
        SELECT 1
        FROM `matchResult` mr
                 JOIN `fixture` f ON f.`fixtureId` = mr.`fixtureId`
                 JOIN `fantasyRound` fr ON fr.`roundId` = f.`roundId`
        WHERE fr.`season` IN (OLD.`season`, NEW.`season`)
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Start a new season ruleset instead of changing scoring rules already used by results';
    END IF;
END$$

CREATE TRIGGER `trg_scoringRule_season_locked_delete`
    BEFORE DELETE
    ON `scoringRule`
    FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1
        FROM `matchResult` mr
                 JOIN `fixture` f ON f.`fixtureId` = mr.`fixtureId`
                 JOIN `fantasyRound` fr ON fr.`roundId` = f.`roundId`
        WHERE fr.`season` = OLD.`season`
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Scoring rules cannot be removed from a season that already has results';
    END IF;
END$$

DELIMITER ;

        /*
            Derived replacement for the old performanceHistory table.
            A row describes one player's simulated performance for one fantasy team
            during one round. Keeping teamId prevents unrelated fantasy-team
            appearances from being mixed together.
        */
        CREATE VIEW `player_round_performance` AS
        SELECT ps.`teamId`                        AS `teamId`,
               ps.`playerId`                      AS `playerId`,
               fr.`season`                        AS `season`,
               fr.`roundNumber`                   AS `roundNumber`,
               COUNT(DISTINCT mr.`fixtureId`)     AS `matchesPlayed`,
               SUM(ps.`tries`)                    AS `tries`,
               SUM(ps.`assists`)                  AS `assists`,
               SUM(ps.`tackles`)                  AS `tackles`,
               COALESCE(SUM(fp.`totalPoints`), 0) AS `fantasyPoints`
        FROM `playerStatistics` ps
                 JOIN `matchResult` mr
                      ON mr.`resultId` = ps.`resultId`
                          AND mr.`isCurrent` = TRUE
                 JOIN `fixture` f
                      ON f.`fixtureId` = mr.`fixtureId`
                 JOIN `fantasyRound` fr
                      ON fr.`roundId` = f.`roundId`
                 JOIN `simulationSettings` ss
                      ON ss.`settingsId` = mr.`settingsId`
                 LEFT JOIN `fantasyPoints` fp
                           ON fp.`statId` = ps.`statId`
                               AND fp.`isFinal` = TRUE
        WHERE f.`status` IN ('COMPLETED', 'PROCESSED')
          AND (mr.`approved` = TRUE OR ss.`require_admin_approval` = FALSE)
        GROUP BY ps.`teamId`,
                 ps.`playerId`,
                 fr.`season`,
                 fr.`roundNumber`;
