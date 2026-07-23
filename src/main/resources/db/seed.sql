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


SET
@bullsClub = UUID();
    SET
@sharksClub = UUID();
    SET
@stormersClub = UUID();
    SET
@lionsClub = UUID();
    SET
@cheetahsClub = UUID();
    SET
@pumasClub = UUID();

INSERT INTO `club`
    (clubId, clubName, location, homeVenue)
VALUES (@bullsClub, 'Bulls', 'Pretoria', 'Loftus Versfeld'),
       (@sharksClub, 'Sharks', 'Durban', 'Kings Park'),
       (@stormersClub, 'Stormers', 'Cape Town', 'DHL Stadium'),
       (@lionsClub, 'Lions', 'Johannesburg', 'Ellis Park'),
       (@cheetahsClub, 'Cheetahs', 'Bloemfontein', 'Free State Stadium'),
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

UPDATE `fantasyRound`
SET status = 'COMPLETED'
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
VALUES (@simulationSettingsId,
        '2026',
        1,
        35.00,
        25.00,
        20.00,
        20.00,
        TRUE,
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

UPDATE `matchResult`
SET approved                  = TRUE,
    approved_by_admin_user_id = @adminId
WHERE resultId IN (@result1, @result2, @result3);

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