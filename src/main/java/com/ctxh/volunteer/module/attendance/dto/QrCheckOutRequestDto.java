package com.ctxh.volunteer.module.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCheckOutRequestDto {

    @NotNull(message = "Activity ID is required")
    private Long activityId;

    @NotBlank(message = "QR code data is required")
    private String qrCodeData;
}
