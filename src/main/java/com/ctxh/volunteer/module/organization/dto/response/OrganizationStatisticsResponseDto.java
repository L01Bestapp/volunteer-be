package com.ctxh.volunteer.module.organization.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationStatisticsResponseDto {
    private ActivityStatsDto activityStats;
    private ParticipantStatsDto participantStats;
    private ImpactStatsDto impactStats;
}
