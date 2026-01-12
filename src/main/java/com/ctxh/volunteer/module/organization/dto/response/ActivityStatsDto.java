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
public class ActivityStatsDto {
    private Long totalActivities;      // Tổng số hoạt động đã tạo từ trước tới nay
    private Long upcomingCount;        // Số hoạt động sắp diễn ra (để chuẩn bị)
    private Long ongoingCount;         // Số hoạt động đang diễn ra (cần theo dõi check-in)
    private Long completedCount;       // Số hoạt động đã kết thúc
    private Long canceledCount;        // Số hoạt động bị hủy
}
