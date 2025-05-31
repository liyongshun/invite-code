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
public class UsageRecordDto {
    private Long id;
    private String inviteCode;
    private String userId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime usedAt;
} 