package com.company.invitecode.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyInviteCodeRequest {
    
    @NotBlank(message = "邀请码不能为空")
    private String code;
    
    private String userId;
} 