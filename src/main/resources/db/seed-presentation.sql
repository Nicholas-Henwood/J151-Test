/* ============================================================================
   Fantasy TryTons League  --  MERGED PRESENTATION SEED
   ----------------------------------------------------------------------------
   Single self-contained script. Run it against a freshly-created schema:

       mysql -u <user> -p tryton_fantasy_rugby < schema.sql
       mysql -u <user> -p tryton_fantasy_rugby < seed-presentation.sql

   It merges, in one session:
     Section 1  base seed.sql            (Cheetahs/Pumas club bug fixed)
     Section 2  seed-extra-players.sql   (rewired to this seed's real IDs)
     Section 3  presentation accounts    (3 admins, 3 users, 8 named people)
     Section 4  presentation leagues     (showcase + sunrise + midnight test)
     Section 5  seed-extra-leagues.sql   (pointed at user 'christan')

   -------------------------------  LOGIN CREDENTIALS  -------------------------
   ADMINISTRATORS  (password: Admin@123)
       admin1 / admin1@trytons.com
       admin2 / admin2@trytons.com
       admin3 / admin3@trytons.com
   DEMO USERS      (password: User@123)
       user1  / user1@trytons.com
       user2  / user2@trytons.com
       user3  / user3@trytons.com
   NAMED PARTICIPANTS (password: Trytons@123)
       christan  / christan@trytons.com     magdeli   / magdeli@trytons.com
       lindsay   / lindsay@trytons.com      sameer    / sameer@trytons.com
       jaunte    / jaunte@trytons.com       timothy   / timothy@trytons.com
       jarryd    / jarryd@trytons.com       sulaimaan / sulaimaan@trytons.com

   (Base seed accounts also remain: admin/Admin@12345, john/John@12345,
    sarah/Sarah@12345, mike/Mike@12345, emma/Emma@12345, david/David@12345,
    lisa/Lisa@12345, tom/Tom@12345.)
   ============================================================================ */

USE
`tryton_fantasy_rugby`;

START TRANSACTION;

SET
@adminId = UUID();
    SET
@johnId = UUID();
    SET
@sarahId = UUID();
    SET
@mikeId = UUID();
    SET
@emmaId = UUID();
    SET
@davidId = UUID();
    SET
@lisaId = UUID();
    SET
@tomId = UUID();
/*
admin	admin@tritan.com	Admin@12345
john	john@test.com	John@12345
sarah	sarah@test.com	Sarah@12345
mike	mike@test.com	Mike@12345
emma	emma@test.com	Emma@12345
david	david@test.com	David@12345
lisa	lisa@test.com	Lisa@12345
tom	tom@test.com	Tom@12345
 */
INSERT INTO `user`
    (userId, email, passwordHash, username, role)
VALUES (@adminId, 'admin@tritan.com', '$2a$12$ToCAGmBUmoJd5p1LWWewHeCDD8sy/lQmYzUUCv9Wf701EtxoqIpIC', 'admin',
        'ADMINISTRATOR'),
       (@johnId, 'john@test.com', '$2a$12$BwiRPiDDJb81VBpIJ.5D4u/xpaAY9fqM/PJzqsDAz703vHwPEXt4W', 'john',
        'REGISTERED_USER'),
       (@sarahId, 'sarah@test.com', '$2a$12$KxC6y3TRYzeHaVqEXoyoce5HK4Z4aefBlfMJtvoPouCsUuM92x7XO', 'sarah',
        'REGISTERED_USER'),
       (@mikeId, 'mike@test.com', '$2a$12$nc7G2MHlgbX7Cc5NlmiHKOhq1ZgKUBsGJ7k/l2.6e0YaWG4koPNie', 'mike',
        'REGISTERED_USER'),
       (@emmaId, 'emma@test.com', '$2a$12$ETILuthQlI3u/Yj1KZjZ9evmcyfyfEawvD0Hqe/KmLcgarWPPHHHy', 'emma',
        'REGISTERED_USER'),
       (@davidId, 'david@test.com', '$2a$12$.NzFJ.9//Ywv0ksWWno5V.8jrtWacV./tR2ZC31ySrUp36CxepChi', 'david',
        'REGISTERED_USER'),
       (@lisaId, 'lisa@test.com', '$2a$12$uWpaMQBKDHVQYBRvGSnuoubl8.CcC9IkyVKFSfVQIjP9ceJrSx.BG', 'lisa',
        'REGISTERED_USER'),
       (@tomId, 'tom@test.com', '$2a$12$F30/FCjnU6wbrvgMoaL2j.RaU8YQdqhz51NgaGBAoLNFCg.OIzmY6', 'tom',
        'REGISTERED_USER');

INSERT INTO `administrator`(userId, adminLevel)
VALUES (@adminId, 5);

INSERT INTO `registeredUser`(userId, registrationStatus)
VALUES (@johnId, 'ACTIVE'),
       (@sarahId, 'ACTIVE'),
       (@mikeId, 'ACTIVE'),
       (@emmaId, 'ACTIVE'),
       (@davidId, 'ACTIVE'),
       (@lisaId, 'ACTIVE'),
       (@tomId, 'ACTIVE');


SET @bullsClub = UUID();
SET @sharksClub = UUID();
SET @stormersClub = UUID();
SET @lionsClub = UUID();
SET @cheetahsClub = UUID();
SET @pumasClub = UUID();

SET @leinsterClub = UUID();
SET @munsterClub = UUID();
SET @ulsterClub = UUID();
SET @connachtClub = UUID();

SET @cardiffClub = UUID();
SET @dragonsClub = UUID();
SET @ospreysClub = UUID();
SET @scarletsClub = UUID();

SET @glasgowWarriorsClub = UUID();
SET @edinburghClub = UUID();

SET @benettonClub = UUID();
SET @zebreParmaClub = UUID();

INSERT INTO `club`
(clubId, clubName, location, homeVenue)
VALUES
    (@bullsClub, 'Bulls', 'Pretoria', 'Loftus Versfeld Stadium'),
    (@sharksClub, 'Sharks', 'Durban', 'Hollywoodbets Kings Park Stadium'),
    (@stormersClub, 'Stormers', 'Cape Town', 'DHL Stadium'),
    (@lionsClub, 'Lions', 'Johannesburg', 'Emirates Airline Park'),

    (@leinsterClub, 'Leinster', 'Dublin', 'Aviva Stadium'),
    (@munsterClub, 'Munster', 'Limerick', 'Thomond Park'),
    (@ulsterClub, 'Ulster', 'Belfast', 'Kingspan Stadium'),
    (@connachtClub, 'Connacht', 'Galway', 'Dexcom Stadium'),

    (@cardiffClub, 'Cardiff', 'Cardiff', 'Cardiff Arms Park'),
    (@dragonsClub, 'Dragons', 'Newport', 'Rodney Parade'),
    (@ospreysClub, 'Ospreys', 'Swansea', 'Swansea.com Stadium'),
    (@scarletsClub, 'Scarlets', 'Llanelli', 'Parc y Scarlets'),

    (@glasgowWarriorsClub, 'Glasgow Warriors', 'Glasgow', 'Scotstoun Stadium'),
    (@edinburghClub, 'Edinburgh', 'Edinburgh', 'Hive Stadium'),

    (@benettonClub, 'Benetton', 'Treviso', 'Stadio Comunale di Monigo'),
    (@zebreParmaClub, 'Zebre Parma', 'Parma', 'Stadio Sergio Lanfranchi'),
    (@cheetahsClub, 'Cheetahs', 'Bloemfontein', 'Toyota Stadium'),
    (@pumasClub, 'Pumas', 'Nelspruit', 'Mbombela Stadium');


SET
@propId = UUID();
    SET
@hookerId = UUID();
    SET
@lockId = UUID();
    SET
@looseForwardId = UUID();
    SET
@scrumhalfId = UUID();
    SET
@flyhalfId = UUID();
    SET
@centreId = UUID();
    SET
@wingId = UUID();
    SET
@fullbackId = UUID();

INSERT INTO `position`
    (positionId, positionName, positionCategory, minRequired, maxAllowed)
VALUES (@propId, 'Prop', 'FORWARD', 2, 4),
       (@hookerId, 'Hooker', 'FORWARD', 1, 2),
       (@lockId, 'Lock', 'FORWARD', 2, 4),
       (@looseForwardId, 'Loose Forward', 'FORWARD', 3, 5),
       (@scrumhalfId, 'Scrum Half', 'BACK', 1, 2),
       (@flyhalfId, 'Fly Half', 'BACK', 1, 2),
       (@centreId, 'Centre', 'BACK', 2, 4),
       (@wingId, 'Wing', 'BACK', 2, 4),
       (@fullbackId, 'Fullback', 'BACK', 1, 2);

SET
@p1 = UUID();
    SET
@p2 = UUID();
    SET
@p3 = UUID();
    SET
@p4 = UUID();
    SET
@p5 = UUID();
    SET
@p6 = UUID();
    SET
@p7 = UUID();
    SET
@p8 = UUID();
    SET
@p9 = UUID();
    SET
@p10 = UUID();
    SET
@p11 = UUID();
    SET
@p12 = UUID();
    SET
@p13 = UUID();
    SET
@p14 = UUID();
    SET
@p15 = UUID();
    SET
@p16 = UUID();
    SET
@p17 = UUID();
    SET
@p18 = UUID();
    SET
@p19 = UUID();
    SET
@p20 = UUID();
    SET
@p21 = UUID();
    SET
@p22 = UUID();
    SET
@p23 = UUID();
    SET
@p24 = UUID();
    SET
@p25 = UUID();
    SET
@p26 = UUID();
    SET
@p27 = UUID();
    SET
@p28 = UUID();
    SET
@p29 = UUID();
    SET
@p30 = UUID();
    SET
@p31 = UUID();
    SET
@p32 = UUID();
    SET
@p33 = UUID();

INSERT INTO `player`
(playerId, clubId, positionId, playerName, value,
 attackingAbility, defensiveAbility, kickingAbility,
 discipline, consistency, fitness, currentForm)
VALUES (@p1, @bullsClub, @flyhalfId, 'Johan van Wyk', 12.5, 88, 72, 91, 80, 85, 90, 87),
       (@p2, @bullsClub, @wingId, 'Chris Botha', 10.5, 85, 70, 60, 78, 80, 88, 82),
       (@p3, @bullsClub, @centreId, 'Andre Jacobs', 11.0, 82, 84, 55, 85, 79, 86, 81),
       (@p4, @bullsClub, @lockId, 'Pieter Smith', 9.0, 60, 90, 20, 88, 82, 92, 79),
       (@p5, @bullsClub, @propId, 'Franco Adams', 8.0, 50, 92, 10, 87, 78, 90, 75),

       (@p6, @sharksClub, @flyhalfId, 'Ryan Williams', 13.0, 90, 73, 92, 81, 88, 90, 89),
       (@p7, @sharksClub, @centreId, 'Luke Daniels', 11.5, 84, 83, 50, 84, 80, 87, 83),
       (@p8, @sharksClub, @wingId, 'David Jacobs', 10.0, 88, 68, 45, 82, 77, 85, 80),
       (@p9, @sharksClub, @hookerId, 'Jason Brown', 8.5, 65, 89, 10, 88, 81, 90, 78),
       (@p10, @sharksClub, @looseForwardId, 'Grant White', 9.2, 72, 91, 20, 86, 84, 91, 82),

       (@p11, @stormersClub, @flyhalfId, 'Peter Adams', 13.5, 91, 76, 93, 85, 88, 92, 90),
       (@p12, @stormersClub, @centreId, 'Mark Taylor', 11.0, 82, 82, 50, 84, 80, 88, 81),
       (@p13, @stormersClub, @wingId, 'Kyle Petersen', 10.8, 89, 67, 40, 82, 79, 89, 84),
       (@p14, @stormersClub, @lockId, 'Dean Miller', 8.9, 58, 93, 10, 87, 82, 91, 80),
       (@p15, @stormersClub, @fullbackId, 'Neil Thomas', 11.2, 85, 74, 80, 84, 83, 88, 85),

       (@p16, @lionsClub, @flyhalfId, 'Morne Venter', 12.0, 84, 71, 89, 80, 82, 86, 81),
       (@p17, @lionsClub, @centreId, 'Ruan Smith', 10.5, 81, 80, 40, 84, 79, 85, 79),
       (@p18, @lionsClub, @wingId, 'Jaco Meyer', 10.1, 86, 66, 30, 80, 77, 87, 80),
       (@p19, @lionsClub, @looseForwardId, 'Willem Botha', 9.0, 68, 90, 10, 86, 80, 90, 78),
       (@p20, @lionsClub, @propId, 'Hendrik Fourie', 8.1, 52, 92, 5, 88, 81, 91, 76),

       (@p21, @cheetahsClub, @flyhalfId, 'Stefan Ross', 11.5, 82, 69, 85, 79, 80, 84, 78),
       (@p22, @cheetahsClub, @centreId, 'Chris Nel', 10.2, 80, 78, 40, 82, 78, 84, 77),
       (@p23, @cheetahsClub, @wingId, 'Brandon Visser', 9.8, 84, 65, 20, 80, 77, 85, 76),
       (@p24, @cheetahsClub, @hookerId, 'Paul Kruger', 8.0, 60, 87, 10, 86, 80, 89, 75),
       (@p25, @cheetahsClub, @lockId, 'Jacques Swanepoel', 8.4, 55, 89, 5, 87, 81, 90, 76),

       (@p26, @pumasClub, @flyhalfId, 'Kevin Roberts', 11.0, 80, 68, 82, 78, 79, 83, 75),
       (@p27, @pumasClub, @centreId, 'Sean Peters', 9.9, 78, 76, 35, 80, 77, 84, 74),
       (@p28, @pumasClub, @wingId, 'Alan Brooks', 9.5, 82, 64, 15, 79, 76, 85, 73),
       (@p29, @pumasClub, @looseForwardId, 'Dylan Green', 8.6, 66, 88, 10, 84, 80, 89, 75),
       (@p30, @pumasClub, @fullbackId, 'Ethan Lewis', 10.4, 81, 72, 78, 82, 79, 86, 77),

       (@p31, @bullsClub, @propId, 'Sibusiso Dlamini', 8.3, 51, 90, 8, 86, 80, 89, 77),
       (@p32, @sharksClub, @scrumhalfId, 'Thabo Mokoena', 10.7, 83, 75, 72, 84, 82, 88, 84),
       (@p33, @stormersClub, @scrumhalfId, 'Daniel van Zyl', 10.3, 81, 74, 70, 85, 83, 87, 82);

INSERT INTO `playerAvailability`
    (availabilityId, playerId, status, effectiveDate)
VALUES (UUID(), @p1, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p2, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p3, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p4, 'INJURED', CURRENT_DATE()),
       (UUID(), @p5, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p6, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p7, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p8, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p9, 'SUSPENDED', CURRENT_DATE()),
       (UUID(), @p10, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p11, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p12, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p13, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p14, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p15, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p16, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p17, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p18, 'INJURED', CURRENT_DATE()),
       (UUID(), @p19, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p20, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p21, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p22, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p23, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p24, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p25, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p26, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p27, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p28, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p29, 'SUSPENDED', CURRENT_DATE()),
       (UUID(), @p30, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p31, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p32, 'ACTIVE', CURRENT_DATE()),
       (UUID(), @p33, 'ACTIVE', CURRENT_DATE());

SET
@team1 = UUID();
    SET
@team2 = UUID();
    SET
@team3 = UUID();
    SET
@team4 = UUID();
    SET
@team5 = UUID();

INSERT INTO `fantasyTeam`
(teamId,
 owner_user_id,
 teamName,
 remainingBudget,
 isValid)
VALUES (@team1, @johnId, 'John Warriors', 5.00, TRUE),
       (@team2, @sarahId, 'Sarah Sharks', 7.50, TRUE),
       (@team3, @mikeId, 'Mike Titans', 10.00, TRUE),
       (@team4, @emmaId, 'Emma Eagles', 6.00, TRUE),
       (@team5, @davidId, 'David Dragons', 11.00, TRUE);

INSERT INTO `team_player_selection`
    (selectionId, teamId, playerId, squadRole, isCaptain, is_vice_captain)
SELECT UUID(),
       teams.teamId,
       players.playerId,
       CASE
           WHEN players.playerId IN (@p31, @p24, @p25, @p33, @p6) THEN 'BENCH'
           ELSE 'STARTING'
           END,
       players.playerId = @p1,
       players.playerId = @p3
FROM (SELECT @team1 AS teamId
      UNION ALL
      SELECT @team2
      UNION ALL
      SELECT @team3
      UNION ALL
      SELECT @team4
      UNION ALL
      SELECT @team5) AS teams
         CROSS JOIN (SELECT @p5 AS playerId
                     UNION ALL
                     SELECT @p20
                     UNION ALL
                     SELECT @p31
                     UNION ALL
                     SELECT @p9
                     UNION ALL
                     SELECT @p24
                     UNION ALL
                     SELECT @p4
                     UNION ALL
                     SELECT @p14
                     UNION ALL
                     SELECT @p25
                     UNION ALL
                     SELECT @p10
                     UNION ALL
                     SELECT @p19
                     UNION ALL
                     SELECT @p29
                     UNION ALL
                     SELECT @p32
                     UNION ALL
                     SELECT @p33
                     UNION ALL
                     SELECT @p1
                     UNION ALL
                     SELECT @p6
                     UNION ALL
                     SELECT @p3
                     UNION ALL
                     SELECT @p7
                     UNION ALL
                     SELECT @p2
                     UNION ALL
                     SELECT @p8
                     UNION ALL
                     SELECT @p15) AS players;



/* Functional recommendation data: current player is in Team 1's editable squad; recommended player is not. */
SET @recommendation1 = UUID();

INSERT INTO `playerRecommendation`
    (recommendationId, teamId, current_player_id, recommended_player_id, reason, score, isDismissed)
VALUES (@recommendation1,
        @team1,
        @p1,
        @p21,
        'Lower-cost fly-half alternative for transfer-planning tests',
        8.25,
        FALSE);

SET
@publicLeague = UUID();
    SET
@privateLeague = UUID();

INSERT INTO `league`
(leagueId,
 manager_user_id,
 leagueName,
 description,
 leagueType,
 maxMembers)
VALUES (@publicLeague,
        NULL,
        'Global Fantasy Rugby',
        'Official public league for all players',
        'PUBLIC',
        100);

INSERT INTO `league`
(leagueId,
 manager_user_id,
 leagueName,
 description,
 leagueType,
 leagueCode,
 maxMembers)
VALUES (@privateLeague,
        NULL,
        'Friends Rugby League',
        'Private code-entry league',
        'PRIVATE',
        'ABC123',
        20);


INSERT INTO `leagueMembership`
(membershipId,
 leagueId,
 registered_user_id,
 teamId)
VALUES (UUID(), @publicLeague, @johnId, @team1),
       (UUID(), @publicLeague, @sarahId, @team2),
       (UUID(), @publicLeague, @mikeId, @team3),
       (UUID(), @publicLeague, @emmaId, @team4),
       (UUID(), @publicLeague, @davidId, @team5),

       (UUID(), @privateLeague, @sarahId, @team2),
       (UUID(), @privateLeague, @johnId, @team1),
       (UUID(), @privateLeague, @mikeId, @team3);

UPDATE `league`
SET manager_user_id = @johnId
WHERE leagueId = @publicLeague;
UPDATE `league`
SET manager_user_id = @sarahId
WHERE leagueId = @privateLeague;

SET
@round1 = UUID();
SET
@round2 = UUID();

INSERT INTO `fantasyRound`
    (roundId, season, roundNumber, openDate, lockDeadline, endDate, status)
VALUES (@round1, '2026', 1, '2026-07-01 00:00:00', '2026-07-07 23:59:59', '2026-07-14 23:59:59', 'LOCKED'),
       (@round2, '2026', 2, '2026-07-15 00:00:00', '2026-07-21 23:59:59', '2026-07-28 23:59:59', 'OPEN');



/* Pending transfer request for the open round. It does not alter the editable squad until confirmed by application logic. */
SET @transfer1 = UUID();

INSERT INTO `transfer`
    (transferId, teamId, roundId, removed_player_id, added_player_id,
     removed_player_value, added_player_value, penaltyPoints, status, confirmedAt, created_by_user_id)
VALUES (@transfer1,
        @team1,
        @round2,
        @p1,
        @p21,
        12.50,
        11.50,
        0,
        'PENDING',
        NULL,
        @johnId);

INSERT INTO `fantasy_team_round_selection`
(selectionId, roundId, teamId, playerId, squadRole, isCaptain, is_vice_captain)
SELECT UUID(),
       @round1,
       currentSelection.teamId,
       currentSelection.playerId,
       currentSelection.squadRole,
       currentSelection.isCaptain,
       currentSelection.is_vice_captain
FROM `team_player_selection` AS currentSelection
WHERE currentSelection.teamId IN (@team1, @team2, @team3, @team4, @team5);

INSERT INTO `roundLock`
    (lockId, roundId, lockAction, action_by_admin_user_id, reason)
VALUES (UUID(), @round1, 'LOCKED', @adminId, 'Round 1 locked for seed simulation data');

/* Round 1 is left IN_PROGRESS (not COMPLETED) so its already-simulated fixtures
   remain resimulatable during the presentation. ControlledResimulationServiceImpl
   only permits resimulation while the round is LOCKED or IN_PROGRESS. */
UPDATE `fantasyRound`
SET status = 'IN_PROGRESS'
WHERE roundId = @round1;

SET
@fixture1 = UUID();
    SET
@fixture2 = UUID();
    SET
@fixture3 = UUID();

INSERT INTO `fixture`
(fixtureId,
 leagueId,
 roundId,
 team_a_id,
 team_b_id,
 fixtureDate,
 fixtureTime,
 status,
 simulationDate)
VALUES (@fixture1,
        @publicLeague,
        @round1,
        @team1,
        @team2,
        '2026-07-08',
        '15:00:00',
        'COMPLETED',
        '2026-07-08 16:30:00'),

       (@fixture2,
        @publicLeague,
        @round1,
        @team3,
        @team4,
        '2026-07-08',
        '18:00:00',
        'COMPLETED',
        '2026-07-08 19:30:00'),

       (@fixture3,
        @privateLeague,
        @round1,
        @team2,
        @team1,
        '2026-07-09',
        '16:00:00',
        'COMPLETED',
        '2026-07-09 17:30:00');

SET
@ruleTry = UUID();
    SET
@ruleAssist = UUID();
    SET
@ruleTackle = UUID();
    SET
@ruleConversion = UUID();
    SET
@ruleMissedTackle = UUID();
    SET
@ruleYellowCard = UUID();
    SET
@ruleRedCard = UUID();

INSERT INTO `scoringRule`
(ruleId,
 season,
 eventType,
 pointsAwarded,
 isDeduction,
 description)
VALUES (@ruleTry,
        '2026',
        'TRY',
        5,
        FALSE,
        'Points awarded for scoring a try'),

       (@ruleAssist,
        '2026',
        'ASSIST',
        3,
        FALSE,
        'Points awarded for a try assist'),

       (@ruleTackle,
        '2026',
        'TACKLE',
        1,
        FALSE,
        'Points awarded for a successful tackle'),

       (@ruleConversion,
        '2026',
        'CONVERSION',
        2,
        FALSE,
        'Points awarded for a successful conversion'),

       (@ruleMissedTackle,
        '2026',
        'MISSED_TACKLE',
        1,
        TRUE,
        'Deduction for a missed tackle'),

       (@ruleYellowCard,
        '2026',
        'YELLOW_CARD',
        3,
        TRUE,
        'Deduction for a yellow card'),

       (@ruleRedCard,
        '2026',
        'RED_CARD',
        10,
        TRUE,
        'Deduction for a red card');

SET
@masterLeaderboard = UUID();
    SET
@publicLeaderboard = UUID();
    SET
@privateLeaderboard = UUID();

INSERT INTO `leaderboard`
(leaderboardId,
 leagueId,
 season,
 scope)
VALUES (@masterLeaderboard,
        NULL,
        '2026',
        'MASTER'),
       (@publicLeaderboard,
        @publicLeague,
        '2026',
        'LEAGUE'),
       (@privateLeaderboard,
        @privateLeague,
        '2026',
        'LEAGUE');

INSERT INTO `ranking`
(rankingId,
 leaderboardId,
 teamId,
 currentRanking,
 previousRanking,
 matchesPlayed,
 matchesWon,
 matchesDrawn,
 matchesLost,
 pointsFor,
 pointsAgainst,
 leaguePoints,
 total_fantasy_points)
/* pointsFor/pointsAgainst/total_fantasy_points are derived from the corrected match scores
   (result1 15-14, result2 14-14, result3 18-15). Win/draw/loss counts, leaguePoints and
   ranking positions are unchanged, since the corrected scores preserve every outcome. */
VALUES (UUID(), @masterLeaderboard, @team2, 1, NULL, 2, 1, 0, 1, 32, 30, 4, 32),
       (UUID(), @masterLeaderboard, @team1, 2, NULL, 2, 1, 0, 1, 30, 32, 4, 30),
       (UUID(), @masterLeaderboard, @team3, 3, NULL, 1, 0, 1, 0, 14, 14, 2, 14),
       (UUID(), @masterLeaderboard, @team4, 4, NULL, 1, 0, 1, 0, 14, 14, 2, 14),
       (UUID(), @masterLeaderboard, @team5, 5, NULL, 0, 0, 0, 0, 0, 0, 0, 0),

       (UUID(), @publicLeaderboard, @team1, 1, NULL, 1, 1, 0, 0, 15, 14, 4, 15),
       (UUID(), @publicLeaderboard, @team3, 2, NULL, 1, 0, 1, 0, 14, 14, 2, 14),
       (UUID(), @publicLeaderboard, @team4, 3, NULL, 1, 0, 1, 0, 14, 14, 2, 14),
       (UUID(), @publicLeaderboard, @team5, 4, NULL, 0, 0, 0, 0, 0, 0, 0, 0),
       (UUID(), @publicLeaderboard, @team2, 5, NULL, 1, 0, 0, 1, 14, 15, 0, 14),

       (UUID(), @privateLeaderboard, @team2, 1, NULL, 1, 1, 0, 0, 18, 15, 4, 18),
       (UUID(), @privateLeaderboard, @team3, 2, NULL, 0, 0, 0, 0, 0, 0, 0, 0),
       (UUID(), @privateLeaderboard, @team1, 3, NULL, 1, 0, 0, 1, 15, 18, 0, 15);

INSERT INTO `notification`
(notificationId,
 userId,
 type,
 body,
 related_entity_type,
 related_entity_id)
VALUES (UUID(),
        @johnId,
        'LEADERBOARD_CHANGE',
        'Your team moved to rank 1.',
        'LEADERBOARD',
        @publicLeaderboard),

       (UUID(),
        @sarahId,
        'ROUND_LOCK',
        'Round 1 has been locked.',
        'ROUND',
        @round1),

       (UUID(),
        @mikeId,
        'POINTS_UPDATE',
        'Weekly points have been updated.',
        'TEAM',
        @team3),

       (UUID(),
        @emmaId,
        'SIMULATED_RESULT',
        'Fixture simulation completed.',
        'FIXTURE',
        @fixture1);

SET
@simulationSettingsId = UUID();

INSERT INTO `simulationSettings`
(settingsId,
 season,
 settingsVersion,
 player_ability_weight,
 player_form_weight,
 team_balance_weight,
 random_variation_weight,
 require_admin_approval,
 allowResimulation,
 maxResimulations,
 isActive)
/* require_admin_approval is FALSE so simulated results are usable and visible
   without an admin approval step, and allowResimulation is TRUE (up to 3 runs)
   so fixtures can be resimulated live during the presentation. */
VALUES (@simulationSettingsId,
        '2026',
        1,
        35.00,
        25.00,
        20.00,
        20.00,
        FALSE,
        TRUE,
        3,
        TRUE);


SET
@result1 = UUID();
SET
@result2 = UUID();
SET
@result3 = UUID();

INSERT INTO `matchResult`
(resultId,
 fixtureId,
 settingsId,
 team_a_score,
 team_b_score,
 winnerSide,
 isDraw,
 approved,
 simulation_run_number)
/* Scores are the fantasy-point totals of each side's selected players, matching the
   match_team_score breakdown rows inserted below. winnerSide/isDraw are unchanged:
   15 > 14 and 18 > 15 still resolve to TEAM_A, and 14 = 14 is still a draw. */
VALUES (@result1,
        @fixture1,
        @simulationSettingsId,
        15,
        14,
        'TEAM_A',
        FALSE,
        FALSE,
        1),
       (@result2,
        @fixture2,
        @simulationSettingsId,
        14,
        14,
        NULL,
        TRUE,
        FALSE,
        1),
       (@result3,
        @fixture3,
        @simulationSettingsId,
        18,
        15,
        'TEAM_A',
        FALSE,
        FALSE,
        1);

INSERT INTO `match_team_score`
(scoreId, resultId, teamId, teamSide, playerPoints, captainBonus, transferPenalty)
/* playerPoints must equal the sum of the seeded fantasyPoints.totalPoints for that team's
   selected players, because trg_match_team_score_insert requires
   playerPoints + captainBonus - transferPenalty to equal the stored matchResult score.
   captainBonus is 0 to mirror TeamScoreServiceImpl, which currently always writes 0. */
VALUES (UUID(), @result1, @team1, 'TEAM_A', 15, 0, 0),
       (UUID(), @result1, @team2, 'TEAM_B', 14, 0, 0),
       (UUID(), @result2, @team3, 'TEAM_A', 14, 0, 0),
       (UUID(), @result2, @team4, 'TEAM_B', 14, 0, 0),
       (UUID(), @result3, @team2, 'TEAM_A', 18, 0, 0),
       (UUID(), @result3, @team1, 'TEAM_B', 15, 0, 0);

/* Results are deliberately LEFT UNAPPROVED (approved = FALSE). An approved match
   result cannot be resimulated (ControlledResimulationServiceImpl), so keeping
   every simulated result unapproved lets the presenter demonstrate resimulation
   on any of these fixtures. With require_admin_approval = FALSE on the active
   settings, unapproved results still display and count. */

SET
@stat1 = UUID();
SET
@stat2 = UUID();
SET
@stat3 = UUID();
SET
@stat4 = UUID();
SET
@stat5 = UUID();
SET
@stat6 = UUID();

INSERT INTO `playerStatistics`
(statId,
 resultId,
 teamId,
 playerId,
 tries,
 assists,
 tackles,
 conversions,
 metersGained)
VALUES (@stat1,
        @result1,
        @team1,
        @p1,
        1,
        1,
        7,
        0,
        120),

       (@stat2,
        @result1,
        @team2,
        @p6,
        2,
        0,
        4,
        0,
        150),

       (@stat3,
        @result2,
        @team3,
        @p3,
        1,
        1,
        6,
        0,
        95),

       (@stat4,
        @result2,
        @team4,
        @p7,
        0,
        2,
        8,
        0,
        88),

       (@stat5,
        @result3,
        @team2,
        @p8,
        2,
        1,
        5,
        0,
        142),

       (@stat6,
        @result3,
        @team1,
        @p15,
        1,
        0,
        6,
        2,
        110);

SET
@points1 = UUID();
SET
@points2 = UUID();
SET
@points3 = UUID();
SET
@points4 = UUID();
SET
@points5 = UUID();
SET
@points6 = UUID();

INSERT INTO `fantasyPoints`
(pointsId,
 statId,
 totalPoints,
 calculationVersion,
 isFinal)
VALUES (@points1, @stat1, 15, 1, TRUE),
       (@points2, @stat2, 14, 1, TRUE),
       (@points3, @stat3, 14, 1, TRUE),
       (@points4, @stat4, 14, 1, TRUE),
       (@points5, @stat5, 18, 1, TRUE),
       (@points6, @stat6, 15, 1, TRUE);

INSERT INTO `fantasy_point_breakdown`
    (breakdownId, pointsId, ruleId, eventCount, pointsEarned)
VALUES (UUID(), @points1, @ruleTry, 1, 5),
       (UUID(), @points1, @ruleAssist, 1, 3),
       (UUID(), @points1, @ruleTackle, 7, 7),
       (UUID(), @points2, @ruleTry, 2, 10),
       (UUID(), @points2, @ruleTackle, 4, 4),
       (UUID(), @points3, @ruleTry, 1, 5),
       (UUID(), @points3, @ruleAssist, 1, 3),
       (UUID(), @points3, @ruleTackle, 6, 6),
       (UUID(), @points4, @ruleAssist, 2, 6),
       (UUID(), @points4, @ruleTackle, 8, 8),
       (UUID(), @points5, @ruleTry, 2, 10),
       (UUID(), @points5, @ruleAssist, 1, 3),
       (UUID(), @points5, @ruleTackle, 5, 5),
       (UUID(), @points6, @ruleTry, 1, 5),
       (UUID(), @points6, @ruleConversion, 2, 4),
       (UUID(), @points6, @ruleTackle, 6, 6);

INSERT INTO `systemReport`
(reportId,
 generated_by_admin_user_id,
 reportType,
 reportTitle)
VALUES (UUID(),
        @adminId,
        'ACTIVE_USERS',
        'Active Users Report');

INSERT INTO `log`
(logId,
 userId,
 entityType,
 entityId,
 actionType,
 description)
VALUES (UUID(),
        @johnId,
        'LEAGUE',
        @publicLeague,
        'CREATE',
        'Created public league'),

       (UUID(),
        @sarahId,
        'LEAGUE',
        @privateLeague,
        'CREATE',
        'Created private league');

COMMIT;

/* ============================================================================
   SECTION 2 -- Extra available players (from seed-extra-players.sql), rewired
   to the position/club variables generated by Section 1 rather than the
   hard-coded UUIDs the standalone file used.
   ============================================================================ */
START TRANSACTION;

SET @xClub   = @bullsClub;
SET @xLoose  = @looseForwardId;
SET @xHooker = @hookerId;
SET @xLock   = @lockId;

SET @lf1 = UUID();
SET @lf2 = UUID();
SET @hk1 = UUID();
SET @lk1 = UUID();

INSERT INTO `player`
    (playerId, clubId, positionId, playerName, value,
     attackingAbility, defensiveAbility, kickingAbility, discipline, consistency, fitness, currentForm, isActive)
VALUES
    (@lf1, @xClub, @xLoose,  'Tank Coetzee', 8.80, 78, 84, 30, 80, 79, 85, 80, 1),
    (@lf2, @xClub, @xLoose,  'Bandi Nkosi',  9.10, 80, 82, 35, 78, 80, 86, 82, 1),
    (@hk1, @xClub, @xHooker, 'Wian du Toit', 8.40, 66, 86, 20, 85, 81, 88, 79, 1),
    (@lk1, @xClub, @xLock,   'Ruben Steyn',  8.70, 58, 90, 15, 87, 82, 90, 78, 1);

INSERT INTO `playerAvailability`
    (availabilityId, playerId, status, effectiveDate, endDate, notes)
VALUES
    (UUID(), @lf1, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available loose forward'),
    (UUID(), @lf2, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available loose forward'),
    (UUID(), @hk1, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available hooker'),
    (UUID(), @lk1, 'ACTIVE', CURRENT_DATE, NULL, 'Seed: available lock');

COMMIT;


/* ============================================================================
   SECTION 3 -- Presentation accounts
     3 admins   (Admin@123)
     3 users    (User@123)
     8 named    (Trytons@123): Christan, Lindsay, Jaunte, Magdeli, Sameer,
                Timothy, Jarryd, Sulaimaan
   Named + demo users each get an EMPTY fantasy team (full budget, isValid=FALSE)
   so squads can be built live during the presentation.
   ============================================================================ */
START TRANSACTION;

SET @uAdmin1 = UUID();
SET @uAdmin2 = UUID();
SET @uAdmin3 = UUID();
SET @uUser1  = UUID();
SET @uUser2  = UUID();
SET @uUser3  = UUID();
SET @uChristan  = UUID();
SET @uLindsay   = UUID();
SET @uJaunte    = UUID();
SET @uMagdeli   = UUID();
SET @uSameer    = UUID();
SET @uTimothy   = UUID();
SET @uJarryd    = UUID();
SET @uSulaimaan = UUID();

INSERT INTO `user` (userId, email, passwordHash, username, role)
VALUES
    (@uAdmin1, 'admin1@trytons.com', '$2a$12$d1rLiJtFwXVY41UexdTOSuUwZRSHiwDdclhkhX3GGpbq/iWJ4kLxC', 'admin1', 'ADMINISTRATOR'),
    (@uAdmin2, 'admin2@trytons.com', '$2a$12$iTXPHdQFXV4YoOAZ21dl1uay0qfBSnMthWdFa2jNL6VK.OEAKikPS', 'admin2', 'ADMINISTRATOR'),
    (@uAdmin3, 'admin3@trytons.com', '$2a$12$DMwyEbVXwxgRlR1oih/iZO2mXLboiXZt91jHbnAa1mfGfX7ZSjdEW', 'admin3', 'ADMINISTRATOR'),
    (@uUser1,  'user1@trytons.com',  '$2a$12$bbay0zN.BP.OdYkPa.cwYeaYuXO91lzSVQVq8Q4swhGM2n3QKPQ9W', 'user1',  'REGISTERED_USER'),
    (@uUser2,  'user2@trytons.com',  '$2a$12$niqsE/yVx/sQ.Fn8yAHi2ufVGs9YlqQe3YMmGxFYGzsDpJ3GaaVgK', 'user2',  'REGISTERED_USER'),
    (@uUser3,  'user3@trytons.com',  '$2a$12$.nqlXIH5jbJWgaYqx8.g6.qy4D0vySmAr6qaX2nVoOyhUnYm3kC7q', 'user3',  'REGISTERED_USER'),
    (@uChristan,  'christan@trytons.com',  '$2a$12$ah2A/BnjiRpjRV3Mp5pE..tBhhDJ.9jzdh.dECWiJ1KCel5Sqq7ji', 'christan',  'REGISTERED_USER'),
    (@uLindsay,   'lindsay@trytons.com',   '$2a$12$HNbLLapT8.jVEvALlw.SKeCAREsuOzXQTwf23bvx.OwV4ymo4eju6', 'lindsay',   'REGISTERED_USER'),
    (@uJaunte,    'jaunte@trytons.com',    '$2a$12$xIHlmX37cxWEkT19TJFQeu6AyREPxoxOrzd6wsyGKBwKNFBH/Odca', 'jaunte',    'REGISTERED_USER'),
    (@uMagdeli,   'magdeli@trytons.com',   '$2a$12$TxqAjtuPv0PdFKkohzM1weBsxDBP3rg7iEqcHNn66ilS4Fy86Tlne', 'magdeli',   'REGISTERED_USER'),
    (@uSameer,    'sameer@trytons.com',    '$2a$12$oqy2BFf3jEzoQmffOA5kEuidx3U2yOi1At3UTbMU2ZVnFdXZwVzjm', 'sameer',    'REGISTERED_USER'),
    (@uTimothy,   'timothy@trytons.com',   '$2a$12$Y0xamAnRzJICEnRU7P/pHeol1pkiAzs4t9AuL6bByzz0WtpNJYTqO', 'timothy',   'REGISTERED_USER'),
    (@uJarryd,    'jarryd@trytons.com',    '$2a$12$cDA7q9mezv2poohpXj7lrOxVnrjZQq20djvrKw9fxjr35Lb/YZWNK', 'jarryd',    'REGISTERED_USER'),
    (@uSulaimaan, 'sulaimaan@trytons.com', '$2a$12$nkH05WodTmzrPTlpBD2D1.vnTwwRumIYXWCM4ANmKLf78kr9rfgm2', 'sulaimaan', 'REGISTERED_USER');

INSERT INTO `administrator` (userId, adminLevel)
VALUES (@uAdmin1, 5), (@uAdmin2, 3), (@uAdmin3, 1);

INSERT INTO `registeredUser` (userId, registrationStatus)
VALUES
    (@uUser1, 'ACTIVE'), (@uUser2, 'ACTIVE'), (@uUser3, 'ACTIVE'),
    (@uChristan, 'ACTIVE'), (@uLindsay, 'ACTIVE'), (@uJaunte, 'ACTIVE'),
    (@uMagdeli, 'ACTIVE'), (@uSameer, 'ACTIVE'), (@uTimothy, 'ACTIVE'),
    (@uJarryd, 'ACTIVE'), (@uSulaimaan, 'ACTIVE');

SET @tUser1  = UUID();
SET @tUser2  = UUID();
SET @tUser3  = UUID();
SET @tChristan  = UUID();
SET @tLindsay   = UUID();
SET @tJaunte    = UUID();
SET @tMagdeli   = UUID();
SET @tSameer    = UUID();
SET @tTimothy   = UUID();
SET @tJarryd    = UUID();
SET @tSulaimaan = UUID();

INSERT INTO `fantasyTeam` (teamId, owner_user_id, teamName, remainingBudget, isValid)
VALUES
    (@tUser1,  @uUser1,  'User One XV',   196.00, FALSE),
    (@tUser2,  @uUser2,  'User Two XV',   196.00, FALSE),
    (@tUser3,  @uUser3,  'User Three XV', 196.00, FALSE),
    (@tChristan,  @uChristan,  'Christan XV',  196.00, FALSE),
    (@tLindsay,   @uLindsay,   'Lindsay XV',   196.00, FALSE),
    (@tJaunte,    @uJaunte,    'Jaunte XV',    196.00, FALSE),
    (@tMagdeli,   @uMagdeli,   'Magdeli XV',   196.00, FALSE),
    (@tSameer,    @uSameer,    'Sameer XV',    196.00, FALSE),
    (@tTimothy,   @uTimothy,   'Timothy XV',   196.00, FALSE),
    (@tJarryd,    @uJarryd,    'Jarryd XV',    196.00, FALSE),
    (@tSulaimaan, @uSulaimaan, 'Sulaimaan XV', 196.00, FALSE);

COMMIT;


/* ============================================================================
   SECTION 4 -- Presentation leagues, fantasy rounds and fixtures
   ----------------------------------------------------------------------------
   Rounds reuse season '2026' (base seed used round numbers 1-2, so these
   start at 3). All three leagues are PUBLIC (leagueCode must be NULL).

     Showcase league : rounds 3-10  open 24/07 11:00, lock 12:30, end 14:00
                       round 11 is the special one, lock 11:45
                       full 8-team round-robin -> 4 fixtures per round
     Sunrise league  : round 12  open 24/07 08:00, lock 09:45, end 10:00
     Test league     : round 13  open 23/07 12:00, lock 23:00, end 24/07 00:00
   ============================================================================ */
START TRANSACTION;

SET @rA3  = UUID();
SET @rA4  = UUID();
SET @rA5  = UUID();
SET @rA6  = UUID();
SET @rA7  = UUID();
SET @rA8  = UUID();
SET @rA9  = UUID();
SET @rA10 = UUID();
SET @rA11 = UUID();
SET @rB   = UUID();
SET @rT   = UUID();

INSERT INTO `fantasyRound`
    (roundId, season, roundNumber, openDate, lockDeadline, endDate, status)
VALUES
    (@rA3,  '2026',  3, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'OPEN'),
    (@rA4,  '2026',  4, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA5,  '2026',  5, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA6,  '2026',  6, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA7,  '2026',  7, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA8,  '2026',  8, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA9,  '2026',  9, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA10, '2026', 10, '2026-07-24 11:00:00', '2026-07-24 12:30:00', '2026-07-24 14:00:00', 'UPCOMING'),
    (@rA11, '2026', 11, '2026-07-24 11:00:00', '2026-07-24 11:45:00', '2026-07-24 14:00:00', 'OPEN'),
    (@rB,   '2026', 12, '2026-07-24 08:00:00', '2026-07-24 09:45:00', '2026-07-24 10:00:00', 'OPEN'),
    (@rT,   '2026', 13, '2026-07-23 12:00:00', '2026-07-23 23:00:00', '2026-07-24 00:00:00', 'OPEN');

SET @lgShowcase = UUID();
SET @lgSunrise  = UUID();
SET @lgTest     = UUID();

INSERT INTO `league`
    (leagueId, manager_user_id, leagueName, description, leagueType, leagueCode, maxMembers)
VALUES
    (@lgShowcase, NULL, 'Fantasy TryTons Showcase',
     'Main presentation league. Eight-team round-robin across rounds 3-11.',
     'PUBLIC', NULL, 50),
    (@lgSunrise, NULL, 'TryTons Sunrise Sevens',
     'Fun quick-fire sunrise league for the group. One morning round.',
     'PUBLIC', NULL, 30),
    (@lgTest, NULL, 'Quick Test League',
     'Simple test league that closes at midnight tonight.',
     'PUBLIC', NULL, 30);

/* Memberships. Showcase carries the 8 named players plus the 3 demo users;
   Sunrise and Test carry the 8 named players. */
INSERT INTO `leagueMembership` (membershipId, leagueId, registered_user_id, teamId)
VALUES
    (UUID(), @lgShowcase, @uChristan,  @tChristan),
    (UUID(), @lgShowcase, @uLindsay,   @tLindsay),
    (UUID(), @lgShowcase, @uJaunte,    @tJaunte),
    (UUID(), @lgShowcase, @uMagdeli,   @tMagdeli),
    (UUID(), @lgShowcase, @uSameer,    @tSameer),
    (UUID(), @lgShowcase, @uTimothy,   @tTimothy),
    (UUID(), @lgShowcase, @uJarryd,    @tJarryd),
    (UUID(), @lgShowcase, @uSulaimaan, @tSulaimaan),
    (UUID(), @lgShowcase, @uUser1,     @tUser1),
    (UUID(), @lgShowcase, @uUser2,     @tUser2),
    (UUID(), @lgShowcase, @uUser3,     @tUser3),

    (UUID(), @lgSunrise, @uChristan,  @tChristan),
    (UUID(), @lgSunrise, @uLindsay,   @tLindsay),
    (UUID(), @lgSunrise, @uJaunte,    @tJaunte),
    (UUID(), @lgSunrise, @uMagdeli,   @tMagdeli),
    (UUID(), @lgSunrise, @uSameer,    @tSameer),
    (UUID(), @lgSunrise, @uTimothy,   @tTimothy),
    (UUID(), @lgSunrise, @uJarryd,    @tJarryd),
    (UUID(), @lgSunrise, @uSulaimaan, @tSulaimaan),

    (UUID(), @lgTest, @uChristan,  @tChristan),
    (UUID(), @lgTest, @uLindsay,   @tLindsay),
    (UUID(), @lgTest, @uJaunte,    @tJaunte),
    (UUID(), @lgTest, @uMagdeli,   @tMagdeli),
    (UUID(), @lgTest, @uSameer,    @tSameer),
    (UUID(), @lgTest, @uTimothy,   @tTimothy),
    (UUID(), @lgTest, @uJarryd,    @tJarryd),
    (UUID(), @lgTest, @uSulaimaan, @tSulaimaan);

/* Managers can only be assigned once the membership exists (trg_league_manager_update). */
UPDATE `league` SET manager_user_id = @uChristan WHERE leagueId = @lgShowcase;
UPDATE `league` SET manager_user_id = @uJarryd   WHERE leagueId = @lgSunrise;
UPDATE `league` SET manager_user_id = @uTimothy  WHERE leagueId = @lgTest;

/* Showcase fixtures: 8-team round-robin.
   T1=Christan T2=Lindsay T3=Jaunte T4=Magdeli T5=Sameer T6=Timothy T7=Jarryd T8=Sulaimaan */
INSERT INTO `fixture`
    (fixtureId, leagueId, roundId, team_a_id, team_b_id, fixtureDate, fixtureTime, status)
VALUES
    (UUID(), @lgShowcase, @rA3, @tChristan,  @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA3, @tLindsay,   @tJarryd,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA3, @tJaunte,    @tTimothy,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA3, @tMagdeli,   @tSameer,    '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA4, @tLindsay,   @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA4, @tJaunte,    @tChristan,  '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA4, @tMagdeli,   @tJarryd,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA4, @tSameer,    @tTimothy,   '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA5, @tJaunte,    @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA5, @tMagdeli,   @tLindsay,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA5, @tSameer,    @tChristan,  '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA5, @tTimothy,   @tJarryd,    '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA6, @tMagdeli,   @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA6, @tSameer,    @tJaunte,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA6, @tTimothy,   @tLindsay,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA6, @tJarryd,    @tChristan,  '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA7, @tSameer,    @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA7, @tTimothy,   @tMagdeli,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA7, @tJarryd,    @tJaunte,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA7, @tChristan,  @tLindsay,   '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA8, @tTimothy,   @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA8, @tJarryd,    @tSameer,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA8, @tChristan,  @tMagdeli,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA8, @tLindsay,   @tJaunte,    '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA9, @tJarryd,    @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA9, @tChristan,  @tTimothy,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA9, @tLindsay,   @tSameer,    '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA9, @tJaunte,    @tMagdeli,   '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA10, @tSulaimaan, @tChristan, '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA10, @tJarryd,    @tLindsay,  '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA10, @tTimothy,   @tJaunte,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA10, @tSameer,    @tMagdeli,  '2026-07-24', '12:00:00', 'UPCOMING'),

    (UUID(), @lgShowcase, @rA11, @tChristan,  @tLindsay,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA11, @tJaunte,    @tMagdeli,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA11, @tSameer,    @tTimothy,   '2026-07-24', '12:00:00', 'UPCOMING'),
    (UUID(), @lgShowcase, @rA11, @tJarryd,    @tSulaimaan, '2026-07-24', '12:00:00', 'UPCOMING');

/* Sunrise league fixtures (round 12). */
INSERT INTO `fixture`
    (fixtureId, leagueId, roundId, team_a_id, team_b_id, fixtureDate, fixtureTime, status)
VALUES
    (UUID(), @lgSunrise, @rB, @tChristan, @tSulaimaan, '2026-07-24', '09:00:00', 'UPCOMING'),
    (UUID(), @lgSunrise, @rB, @tLindsay,  @tJaunte,    '2026-07-24', '09:00:00', 'UPCOMING'),
    (UUID(), @lgSunrise, @rB, @tMagdeli,  @tSameer,    '2026-07-24', '09:00:00', 'UPCOMING'),
    (UUID(), @lgSunrise, @rB, @tTimothy,  @tJarryd,    '2026-07-24', '09:00:00', 'UPCOMING');

/* Test league fixtures (round 13). */
INSERT INTO `fixture`
    (fixtureId, leagueId, roundId, team_a_id, team_b_id, fixtureDate, fixtureTime, status)
VALUES
    (UUID(), @lgTest, @rT, @tChristan, @tMagdeli,   '2026-07-23', '20:00:00', 'UPCOMING'),
    (UUID(), @lgTest, @rT, @tLindsay,  @tTimothy,   '2026-07-23', '20:00:00', 'UPCOMING'),
    (UUID(), @lgTest, @rT, @tJaunte,   @tJarryd,    '2026-07-23', '20:00:00', 'UPCOMING'),
    (UUID(), @lgTest, @rT, @tSameer,   @tSulaimaan, '2026-07-23', '20:00:00', 'UPCOMING');

/* LEAGUE-scope leaderboards + zeroed rankings so the new leagues show standings. */
SET @lbShowcase = UUID();
SET @lbSunrise  = UUID();
SET @lbTest     = UUID();

INSERT INTO `leaderboard` (leaderboardId, leagueId, season, scope)
VALUES
    (@lbShowcase, @lgShowcase, '2026', 'LEAGUE'),
    (@lbSunrise,  @lgSunrise,  '2026', 'LEAGUE'),
    (@lbTest,     @lgTest,     '2026', 'LEAGUE');

INSERT INTO `ranking`
    (rankingId, leaderboardId, teamId, currentRanking, previousRanking,
     matchesPlayed, matchesWon, matchesDrawn, matchesLost,
     pointsFor, pointsAgainst, leaguePoints, total_fantasy_points)
SELECT UUID(), r.leaderboardId, r.teamId, r.rn, NULL,
       0, 0, 0, 0, 0, 0, 0, 0
FROM (
    SELECT lb.leaderboardId,
           m.teamId,
           ROW_NUMBER() OVER (PARTITION BY lb.leaderboardId ORDER BY m.joinDate, m.teamId) AS rn
    FROM `leaderboard` lb
    JOIN `leagueMembership` m ON m.leagueId = lb.leagueId AND m.isActive = TRUE
    WHERE lb.leaderboardId IN (@lbShowcase, @lbSunrise, @lbTest)
) AS r;

/* Register the 11 new teams on the MASTER leaderboard as well, ranked after
   the teams the base seed already placed there. */
SET @masterLb  = (SELECT leaderboardId FROM `leaderboard` WHERE scope = 'MASTER' AND season = '2026');
SET @masterMax = (SELECT COALESCE(MAX(currentRanking), 0) FROM `ranking` WHERE leaderboardId = @masterLb);

INSERT INTO `ranking`
    (rankingId, leaderboardId, teamId, currentRanking, previousRanking,
     matchesPlayed, matchesWon, matchesDrawn, matchesLost,
     pointsFor, pointsAgainst, leaguePoints, total_fantasy_points)
SELECT UUID(), @masterLb, t.teamId,
       @masterMax + ROW_NUMBER() OVER (ORDER BY t.teamName), NULL,
       0, 0, 0, 0, 0, 0, 0, 0
FROM `fantasyTeam` t
WHERE t.teamId IN (@tChristan, @tLindsay, @tJaunte, @tMagdeli, @tSameer,
                   @tTimothy, @tJarryd, @tSulaimaan, @tUser1, @tUser2, @tUser3)
  AND @masterLb IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `ranking` rk
                  WHERE rk.leaderboardId = @masterLb AND rk.teamId = t.teamId);

COMMIT;


/* ============================================================================
   SECTION 5 -- seed-extra-leagues.sql (Highveld Heroes, Coastal Cup, Varsity
   Challenge, Office Rugby Pool, Old Boys XV). Adapted: @meUsername -> 'christan'
   (created in Section 3). Runs verbatim otherwise, including its own guards,
   transaction and before/after report queries.
   ============================================================================ */
-- ============================================================================
--  Additive league seed — extra public/private leagues, with one chosen user
--  joined to three of them.
--
--  Safe to run against an already-seeded database: it only INSERTs, never
--  drops or truncates, and every insert is guarded so a re-run cannot violate
--  the unique constraints (uk_leagueMembership_user, uk_ranking_position,
--  uk_leaderboard_scope_season, uk_league_code).
--
--  HOW TO RUN
--    mysql -u <user> -p tryton_fantasy_rugby < seed-extra-leagues.sql
--
--  SET YOUR USERNAME on the @meUsername line below. It must match
--  user.username of the account you log into the app with.
-- ============================================================================


-- ---------------------------------------------------------------------------
-- 0. Before/after picture, so it is obvious whether the rows were already there
-- ---------------------------------------------------------------------------
SELECT 'BEFORE' AS stage,
       COUNT(*)                                    AS leagues,
       SUM(leagueType = 'PUBLIC')                  AS public_leagues,
       SUM(leagueType = 'PRIVATE')                 AS private_leagues
FROM `league`;

START TRANSACTION;

-- ---------------------------------------------------------------------------
-- 1. Who am I?  <<< CHANGE THIS to your login username
-- ---------------------------------------------------------------------------
SET @meUsername = 'christan';

SET @meId = (SELECT userId FROM `user` WHERE username = @meUsername);

-- A membership needs a team (leagueMembership.teamId is NOT NULL), and
-- uk_fantasyTeam_owner allows exactly one team per user. Create one only if
-- this account does not already have it. Budget matches
-- FantasyTeamServiceImpl.INITIAL_BUDGET, which is in millions.
INSERT INTO `fantasyTeam` (teamId, owner_user_id, teamName, remainingBudget, isValid)
SELECT UUID(), @meId, CONCAT(@meUsername, '''s XV'), 196.00, TRUE
FROM DUAL
WHERE @meId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `fantasyTeam` WHERE owner_user_id = @meId);

SET @myTeamId = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @meId);

-- ---------------------------------------------------------------------------
-- 2. New leagues — 3 public, 2 private
-- ---------------------------------------------------------------------------
SET @lgHighveld = UUID();
SET @lgCoastal  = UUID();
SET @lgVarsity  = UUID();
SET @lgOffice   = UUID();
SET @lgOldBoys  = UUID();

-- PUBLIC leagues must have a NULL leagueCode (chk_league_code_type), and
-- trg_league_manager_insert forbids setting manager_user_id on INSERT: the
-- manager has to be an active member first. Managers are assigned in step 3c.
INSERT INTO `league` (leagueId, manager_user_id, leagueName, description, leagueType, leagueCode, maxMembers)
SELECT * FROM (
    SELECT @lgHighveld AS leagueId, NULL AS manager_user_id, 'Highveld Heroes' AS leagueName,
           'Open league for Bulls, Lions and Cheetahs supporters. Weekly bragging rights on the line.' AS description,
           'PUBLIC' AS leagueType, NULL AS leagueCode, 24 AS maxMembers
    UNION ALL
    SELECT @lgCoastal, NULL, 'Coastal Cup',
           'Sharks and Stormers country. A relaxed public league for the coastal sides.',
           'PUBLIC', NULL, 32
    UNION ALL
    SELECT @lgVarsity, NULL, 'Varsity Challenge',
           'Student league open to everyone. Fast turnover and plenty of transfers.',
           'PUBLIC', NULL, 16
    UNION ALL
    SELECT @lgOffice, NULL, 'Office Rugby Pool',
           'Private league for the team at work. Ask the manager for the code.',
           'PRIVATE', 'OFF123', 12
    UNION ALL
    SELECT @lgOldBoys, NULL, 'Old Boys XV',
           'Invite-only league for the alumni side. Long memories, longer arguments.',
           'PRIVATE', 'OLD456', 10
) AS newLeagues
-- Skip any league whose name is already present, so a re-run is a no-op.
WHERE NOT EXISTS (SELECT 1 FROM `league` l WHERE l.leagueName = newLeagues.leagueName);

-- Re-resolve the ids: on a re-run the UUID()s above were not inserted, so read
-- back whatever is actually in the table.
SET @lgHighveld = (SELECT leagueId FROM `league` WHERE leagueName = 'Highveld Heroes');
SET @lgCoastal  = (SELECT leagueId FROM `league` WHERE leagueName = 'Coastal Cup');
SET @lgVarsity  = (SELECT leagueId FROM `league` WHERE leagueName = 'Varsity Challenge');
SET @lgOffice   = (SELECT leagueId FROM `league` WHERE leagueName = 'Office Rugby Pool');
SET @lgOldBoys  = (SELECT leagueId FROM `league` WHERE leagueName = 'Old Boys XV');

-- ---------------------------------------------------------------------------
-- 3. Memberships
--    You join three: Highveld Heroes and Coastal Cup (public) and Office Rugby
--    Pool (private). Varsity Challenge and Old Boys XV are deliberately left
--    without you, so the Discover tab and the join-by-code path both have
--    something to show.
-- ---------------------------------------------------------------------------

-- 3a. You
INSERT INTO `leagueMembership` (membershipId, leagueId, registered_user_id, teamId)
SELECT UUID(), lg.leagueId, @meId, @myTeamId
FROM (SELECT @lgHighveld AS leagueId
      UNION ALL SELECT @lgCoastal
      UNION ALL SELECT @lgOffice) AS lg
WHERE @meId IS NOT NULL
  AND @myTeamId IS NOT NULL
  AND lg.leagueId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `leagueMembership` m
                  WHERE m.leagueId = lg.leagueId AND m.registered_user_id = @meId);

-- 3b. Other seeded managers, so the member counts are not all 1.
--     Only users who already have a team can join, and never you twice.
INSERT INTO `leagueMembership` (membershipId, leagueId, registered_user_id, teamId)
SELECT UUID(), lg.leagueId, u.userId, ft.teamId
FROM `user` u
JOIN `fantasyTeam` ft ON ft.owner_user_id = u.userId
JOIN (SELECT @lgHighveld AS leagueId, 'sarah' AS username
      UNION ALL SELECT @lgHighveld, 'mike'
      UNION ALL SELECT @lgHighveld, 'emma'
      UNION ALL SELECT @lgCoastal,  'david'
      UNION ALL SELECT @lgCoastal,  'sarah'
      UNION ALL SELECT @lgOffice,   'mike'
      UNION ALL SELECT @lgVarsity,  'emma'
      UNION ALL SELECT @lgVarsity,  'david'
      UNION ALL SELECT @lgOldBoys,  'sarah') AS lg ON lg.username = u.username
WHERE lg.leagueId IS NOT NULL
  AND (@meId IS NULL OR u.userId <> @meId)
  AND NOT EXISTS (SELECT 1 FROM `leagueMembership` m
                  WHERE m.leagueId = lg.leagueId AND m.registered_user_id = u.userId)
  AND NOT EXISTS (SELECT 1 FROM `leagueMembership` m
                  WHERE m.leagueId = lg.leagueId AND m.teamId = ft.teamId);

-- 3c. Now that the memberships exist, the manager can be assigned.
--     trg_league_manager_update still checks the membership, so this only
--     succeeds for leagues you actually joined above.
UPDATE `league` l
SET l.manager_user_id = @meId
WHERE l.leagueName IN ('Highveld Heroes', 'Office Rugby Pool')
  AND l.manager_user_id IS NULL
  AND @meId IS NOT NULL
  AND EXISTS (SELECT 1 FROM `leagueMembership` m
              WHERE m.leagueId = l.leagueId AND m.registered_user_id = @meId AND m.isActive = TRUE);

-- ---------------------------------------------------------------------------
-- 4. LEAGUE-scope leaderboards for the new leagues, so the cards on the
--    leagues page have mini standings to show.
--    Season matches the existing seed ('2026'); uk_leaderboard_scope_season
--    allows one per league per season.
-- ---------------------------------------------------------------------------
INSERT INTO `leaderboard` (leaderboardId, leagueId, season, scope)
SELECT UUID(), lg.leagueId, '2026', 'LEAGUE'
FROM (SELECT @lgHighveld AS leagueId
      UNION ALL SELECT @lgCoastal
      UNION ALL SELECT @lgVarsity
      UNION ALL SELECT @lgOffice
      UNION ALL SELECT @lgOldBoys) AS lg
WHERE lg.leagueId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `leaderboard` lb
                  WHERE lb.leagueId = lg.leagueId AND lb.season = '2026');

-- ---------------------------------------------------------------------------
-- 5. Rankings for every member of the new leagues.
--    currentRanking must be unique per leaderboard (uk_ranking_position), so
--    positions come from ROW_NUMBER(). Points descend with rank so the table
--    reads consistently, and previousRanking is one lower to give the rank
--    movement chips something to show.
-- ---------------------------------------------------------------------------
INSERT INTO `ranking` (rankingId, leaderboardId, teamId, currentRanking, previousRanking,
                       matchesPlayed, matchesWon, matchesDrawn, matchesLost,
                       pointsFor, pointsAgainst, leaguePoints, total_fantasy_points)
SELECT UUID(), r.leaderboardId, r.teamId, r.rn, r.rn + 1,
       3, 3 - r.rn % 3, 0, r.rn % 3,
       60 - (r.rn * 4), 40 + (r.rn * 3), 12 - (r.rn * 2), 48 - (r.rn * 6)
FROM (
    SELECT lb.leaderboardId,
           m.teamId,
           ROW_NUMBER() OVER (PARTITION BY lb.leaderboardId ORDER BY m.joinDate, m.teamId) AS rn
    FROM `leaderboard` lb
    JOIN `leagueMembership` m ON m.leagueId = lb.leagueId AND m.isActive = TRUE
    WHERE lb.season = '2026'
      AND lb.scope = 'LEAGUE'
      AND lb.leagueId IN (@lgHighveld, @lgCoastal, @lgVarsity, @lgOffice, @lgOldBoys)
) AS r
WHERE NOT EXISTS (SELECT 1 FROM `ranking` rk
                  WHERE rk.leaderboardId = r.leaderboardId AND rk.teamId = r.teamId);

COMMIT;

-- ---------------------------------------------------------------------------
-- 6. Verify
-- ---------------------------------------------------------------------------
SELECT 'AFTER' AS stage,
       COUNT(*)                    AS leagues,
       SUM(leagueType = 'PUBLIC')  AS public_leagues,
       SUM(leagueType = 'PRIVATE') AS private_leagues
FROM `league`;

SELECT l.leagueName,
       l.leagueType,
       l.maxMembers,
       COUNT(m.membershipId)                                              AS members,
       MAX(m.registered_user_id = @meId)                                  AS you_are_a_member
FROM `league` l
LEFT JOIN `leagueMembership` m ON m.leagueId = l.leagueId AND m.isActive = TRUE
GROUP BY l.leagueId, l.leagueName, l.leagueType, l.maxMembers
ORDER BY l.leagueType, l.leagueName;


/* ============================================================================
   SECTION 6 -- Past-season simulation settings and scoring rules
   ----------------------------------------------------------------------------
   Historical, INACTIVE simulation-settings versions for prior seasons so the
   admin simulation-settings screen (SimulationSettingServiceImpl.listSimulation-
   Settings returns every season) can show and manage past seasons alongside the
   active 2026 configuration. Only the 2026 row stays active -- the generated
   uk_simulationSettings_active_season key allows exactly one active season, so
   these must be isActive = FALSE (and therefore allowResimulation = FALSE with
   maxResimulations = 0, per chk_simulationSettings_resimulation_enabled).
   Matching scoring-rule sets are added for each past season; they have no
   results yet, so they stay editable.
   ============================================================================ */
START TRANSACTION;

SET @simSettings2024 = UUID();
SET @simSettings2025 = UUID();

INSERT INTO `simulationSettings`
    (settingsId, season, settingsVersion,
     player_ability_weight, player_form_weight, team_balance_weight, random_variation_weight,
     require_admin_approval, allowResimulation, maxResimulations, isActive)
VALUES
    (@simSettings2024, '2024', 1, 40.00, 25.00, 20.00, 15.00, TRUE, FALSE, 0, FALSE),
    (@simSettings2025, '2025', 1, 35.00, 30.00, 20.00, 15.00, TRUE, FALSE, 0, FALSE);

INSERT INTO `scoringRule`
    (ruleId, season, eventType, pointsAwarded, isDeduction, description)
VALUES
    (UUID(), '2024', 'TRY',           5, FALSE, 'Points awarded for scoring a try'),
    (UUID(), '2024', 'ASSIST',        3, FALSE, 'Points awarded for a try assist'),
    (UUID(), '2024', 'TACKLE',        1, FALSE, 'Points awarded for a successful tackle'),
    (UUID(), '2024', 'CONVERSION',    2, FALSE, 'Points awarded for a successful conversion'),
    (UUID(), '2024', 'MISSED_TACKLE', 1, TRUE,  'Deduction for a missed tackle'),
    (UUID(), '2024', 'YELLOW_CARD',   3, TRUE,  'Deduction for a yellow card'),
    (UUID(), '2024', 'RED_CARD',     10, TRUE,  'Deduction for a red card'),

    (UUID(), '2025', 'TRY',           5, FALSE, 'Points awarded for scoring a try'),
    (UUID(), '2025', 'ASSIST',        3, FALSE, 'Points awarded for a try assist'),
    (UUID(), '2025', 'TACKLE',        1, FALSE, 'Points awarded for a successful tackle'),
    (UUID(), '2025', 'CONVERSION',    2, FALSE, 'Points awarded for a successful conversion'),
    (UUID(), '2025', 'MISSED_TACKLE', 1, TRUE,  'Deduction for a missed tackle'),
    (UUID(), '2025', 'YELLOW_CARD',   3, TRUE,  'Deduction for a yellow card'),
    (UUID(), '2025', 'RED_CARD',     10, TRUE,  'Deduction for a red card');

COMMIT;


/* ============================================================================
   SECTION 7 -- Closed (finished) leagues
   ----------------------------------------------------------------------------
   Two leagues with isActive = FALSE, each carrying final standings, so the
   presentation can show the "closed league" state next to the live ones.
   Closed leagues remain visible in the full leagues list
   (LeagueServiceImpl.getAllLeagues does not filter on isActive) but are hidden
   from the joinable public preview (getPublicLeaguePreviews skips
   isActive = FALSE), so watchers cannot accidentally join a finished league.
   Members reuse the base demo teams (john/sarah/mike/emma/david), re-resolved
   by username so the section is independent of Section 1's session variables.
   ============================================================================ */
START TRANSACTION;

SET @cuJohn  = (SELECT userId FROM `user` WHERE username = 'john');
SET @cuSarah = (SELECT userId FROM `user` WHERE username = 'sarah');
SET @cuMike  = (SELECT userId FROM `user` WHERE username = 'mike');
SET @cuEmma  = (SELECT userId FROM `user` WHERE username = 'emma');
SET @cuDavid = (SELECT userId FROM `user` WHERE username = 'david');

SET @ctJohn  = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @cuJohn);
SET @ctSarah = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @cuSarah);
SET @ctMike  = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @cuMike);
SET @ctEmma  = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @cuEmma);
SET @ctDavid = (SELECT teamId FROM `fantasyTeam` WHERE owner_user_id = @cuDavid);

SET @lgAutumn  = UUID();
SET @lgLegends = UUID();

/* Created active with a NULL manager (trg_league_manager_insert forbids setting
   a manager on INSERT); the manager is assigned after memberships exist and the
   leagues are closed at the very end of the section. */
INSERT INTO `league`
    (leagueId, manager_user_id, leagueName, description, leagueType, leagueCode, maxMembers)
VALUES
    (@lgAutumn, NULL, 'Autumn Classic 2025',
     'Completed 2025 public league. Final standings only - this league is closed.',
     'PUBLIC', NULL, 20),
    (@lgLegends, NULL, 'Legends Invitational 2025',
     'Completed 2025 invitational. Closed - kept for its final leaderboard.',
     'PRIVATE', 'LEG789', 12);

INSERT INTO `leagueMembership` (membershipId, leagueId, registered_user_id, teamId)
VALUES
    (UUID(), @lgAutumn, @cuJohn,  @ctJohn),
    (UUID(), @lgAutumn, @cuSarah, @ctSarah),
    (UUID(), @lgAutumn, @cuMike,  @ctMike),
    (UUID(), @lgAutumn, @cuEmma,  @ctEmma),
    (UUID(), @lgAutumn, @cuDavid, @ctDavid),

    (UUID(), @lgLegends, @cuSarah, @ctSarah),
    (UUID(), @lgLegends, @cuMike,  @ctMike),
    (UUID(), @lgLegends, @cuEmma,  @ctEmma);

UPDATE `league` SET manager_user_id = @cuJohn  WHERE leagueId = @lgAutumn;
UPDATE `league` SET manager_user_id = @cuSarah WHERE leagueId = @lgLegends;

SET @lbAutumn  = UUID();
SET @lbLegends = UUID();

INSERT INTO `leaderboard` (leaderboardId, leagueId, season, scope)
VALUES
    (@lbAutumn,  @lgAutumn,  '2025', 'LEAGUE'),
    (@lbLegends, @lgLegends, '2025', 'LEAGUE');

INSERT INTO `ranking`
    (rankingId, leaderboardId, teamId, currentRanking, previousRanking,
     matchesPlayed, matchesWon, matchesDrawn, matchesLost,
     pointsFor, pointsAgainst, leaguePoints, total_fantasy_points)
VALUES
    (UUID(), @lbAutumn, @ctJohn,  1, 2, 4, 3, 0, 1, 82, 60, 12, 82),
    (UUID(), @lbAutumn, @ctSarah, 2, 1, 4, 2, 1, 1, 74, 66,  8, 74),
    (UUID(), @lbAutumn, @ctMike,  3, 3, 4, 2, 0, 2, 68, 70,  8, 68),
    (UUID(), @lbAutumn, @ctEmma,  4, 4, 4, 1, 1, 2, 61, 72,  5, 61),
    (UUID(), @lbAutumn, @ctDavid, 5, 5, 4, 0, 2, 2, 55, 72,  2, 55),

    (UUID(), @lbLegends, @ctSarah, 1, 1, 3, 3, 0, 0, 60, 40,  9, 60),
    (UUID(), @lbLegends, @ctMike,  2, 3, 3, 1, 1, 1, 48, 47,  5, 48),
    (UUID(), @lbLegends, @ctEmma,  3, 2, 3, 0, 1, 2, 38, 59,  1, 38);

/* Close both leagues now that their memberships, manager and standings exist. */
UPDATE `league` SET isActive = FALSE WHERE leagueId IN (@lgAutumn, @lgLegends);

COMMIT;

/* Presentation cheat-sheet: which leagues are live vs closed, and joinable room. */
SELECT l.leagueName,
       l.leagueType,
       CASE WHEN l.isActive THEN 'LIVE' ELSE 'CLOSED' END                             AS state,
       l.maxMembers,
       (SELECT COUNT(*) FROM `leagueMembership` m
         WHERE m.leagueId = l.leagueId AND m.isActive = TRUE)                          AS members
FROM `league` l
ORDER BY l.isActive DESC, l.leagueType, l.leagueName;
