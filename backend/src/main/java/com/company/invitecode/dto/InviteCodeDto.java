package com.company.invitecode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteCodeDto {
    private Long id;
    private String code;
    private String batchId;
    private LocalDateTime createdAt;
    private String createdBy;
    private boolean isActive;
    private int usageCount;
} 