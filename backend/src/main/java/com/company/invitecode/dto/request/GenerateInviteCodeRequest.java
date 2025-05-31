package com.company.invitecode.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateInviteCodeRequest {
    
    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "生成数量必须大于0")
    private Integer count;
    
    private String description;
} 