package com.company.invitecode.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class GenerateInviteCodeRequest {
    
    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "生成数量必须大于0")
    private Integer count;
    
    private String description;
    
    public GenerateInviteCodeRequest() {
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "GenerateInviteCodeRequest{" +
                "count=" + count +
                ", description='" + description + '\'' +
                '}';
    }
} 