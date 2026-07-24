package com.vzap.trytons.config;

import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.filter.RoleFilter;
import com.vzap.trytons.mapper.ApplicationExceptionMapper;
import com.vzap.trytons.mapper.JAXExceptionMapper;
import com.vzap.trytons.resource.admin.AdminUserResource;
import com.vzap.trytons.resource.admin.DatabaseHealthResource;
import com.vzap.trytons.resource.admin.LogResource;
import com.vzap.trytons.resource.admin.SystemReportResource;
import com.vzap.trytons.resource.auth.AuthResource;
import com.vzap.trytons.resource.auth.ProfileResource;
import com.vzap.trytons.resource.auth.UserResources;
import com.vzap.trytons.resource.catalog.ClubResource;
import com.vzap.trytons.resource.catalog.PlayerResource;
import com.vzap.trytons.resource.catalog.PositionResource;
import com.vzap.trytons.resource.fantasyteam.FantasyTeamResource;
import com.vzap.trytons.resource.fixture.FixtureResource;
import com.vzap.trytons.resource.fixture.LockStatusResource;
import com.vzap.trytons.resource.fixture.RoundResource;
import com.vzap.trytons.resource.history.UserHistoryResource;
import com.vzap.trytons.resource.leaderboard.LeaderboardResource;
import com.vzap.trytons.resource.league.LeagueResource;
import com.vzap.trytons.resource.notification.NotificationResource;
import com.vzap.trytons.resource.publicpreview.PublicPreviewResource;
import com.vzap.trytons.resource.results.MatchResultResource;
import com.vzap.trytons.resource.results.MatchTeamScoreResource;
import com.vzap.trytons.resource.results.PlayerStatisticsResource;
import com.vzap.trytons.resource.shared.ProtectedResource;
import com.vzap.trytons.resource.scoring.FantasyPointBreakdownResource;
import com.vzap.trytons.resource.scoring.FantasyPointsResource;
import com.vzap.trytons.resource.scoring.ScoringRuleResource;
import com.vzap.trytons.resource.simulation.CompetitionProcessingResource;
import com.vzap.trytons.resource.simulation.ControlledResimulationResource;
import com.vzap.trytons.resource.simulation.SimulationResource;
import com.vzap.trytons.resource.simulation.SimulationSettingResource;
import com.vzap.trytons.resource.transfer.TransferRecommendationResource;
import com.vzap.trytons.resource.transfer.TransferResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;

@ApplicationPath("/api")
public class RestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                ObjectMapperProvider.class,
                AdminUserResource.class,
                AuthResource.class,
                ClubResource.class,
                CompetitionProcessingResource.class,
                ControlledResimulationResource.class,
                DatabaseHealthResource.class,
                FantasyPointBreakdownResource.class,
                FantasyPointsResource.class,
                FantasyTeamResource.class,
                FixtureResource.class,
                LeaderboardResource.class,
                LeagueResource.class,
                LockStatusResource.class,
                LogResource.class,
                MatchResultResource.class,
                MatchTeamScoreResource.class,
                NotificationResource.class,
                PlayerResource.class,
                PlayerStatisticsResource.class,
                PositionResource.class,
                ProfileResource.class,
                PublicPreviewResource.class,
                RoundResource.class,
                ProtectedResource.class,
                ScoringRuleResource.class,
                SimulationResource.class,
                SimulationSettingResource.class,
                SystemReportResource.class,
                TransferRecommendationResource.class,
                TransferResource.class,
                UserHistoryResource.class,
                UserResources.class,
                AuthFilter.class,
                RoleFilter.class,
                ApplicationExceptionMapper.class,
                JAXExceptionMapper.class
        );
    }
}
