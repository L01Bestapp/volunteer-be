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
public class ParticipantStatsDto {
    private Long totalSlots;           // Tổng số lượng max_participants của tất cả hoạt động
    private Long totalRegistrations;   // Tổng số đơn đăng ký (bao gồm cả pending) - thể hiện độ quan tâm
    private Long totalApproved;        // Tổng số sinh viên đã được duyệt tham gia
    private Long totalAttended;        // Tổng số sinh viên đã check-in/hoàn thành thực tế
    private Double avgAttendanceRate;  // Tỷ lệ đi thực tế so với duyệt (Attended / Approved * 100)
}
