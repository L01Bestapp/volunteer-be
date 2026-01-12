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
public class ImpactStatsDto {
    private Double totalCtxhDaysGenerated;  // Tổng số ngày CTXH đã cấp cho sinh viên
}
