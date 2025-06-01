package com.company.invitecode.dto.request;

import javax.validation.constraints.NotBlank;

public class VerifyInviteCodeRequest {
    
    @NotBlank(message = "邀请码不能为空")
    private String code;
    
    private String userId;
    
    public VerifyInviteCodeRequest() {
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return "VerifyInviteCodeRequest{" +
                "code='" + code + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
} 