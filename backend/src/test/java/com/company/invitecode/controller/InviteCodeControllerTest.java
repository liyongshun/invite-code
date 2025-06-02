package com.company.invitecode.controller;

import com.company.invitecode.dto.InviteCodeDto;
import com.company.invitecode.dto.request.GenerateInviteCodeRequest;
import com.company.invitecode.dto.response.ApiResponse;
import com.company.invitecode.service.InviteCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InviteCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InviteCodeService inviteCodeService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void generateInviteCodes_ShouldReturnGeneratedCodes() throws Exception {
        // Arrange
        GenerateInviteCodeRequest request = new GenerateInviteCodeRequest();
        request.setCount(5);
        request.setDescription("测试批次");

        List<InviteCodeDto> generatedCodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            InviteCodeDto dto = new InviteCodeDto();
            dto.setId((long) i);
            dto.setCode("TEST" + i);
            dto.setBatchId(UUID.randomUUID().toString());
            dto.setCreatedAt(LocalDateTime.now());
            dto.setCreatedBy("admin");
            dto.setActive(true);
            dto.setUsageCount(0);
            dto.setDescription("测试批次");
            generatedCodes.add(dto);
        }

        when(inviteCodeService.generateInviteCodes(any(GenerateInviteCodeRequest.class), eq("admin")))
                .thenReturn(generatedCodes);

        // Act & Assert
        mockMvc.perform(post("/invite-codes/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("成功生成5个邀请码"))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].code").exists());
    }
    
    @Test
    public void generateInviteCodes_WithoutAuth_ShouldReturn401() throws Exception {
        // Arrange
        GenerateInviteCodeRequest request = new GenerateInviteCodeRequest();
        request.setCount(5);
        request.setDescription("测试批次");

        // Act & Assert
        mockMvc.perform(post("/invite-codes/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void generateInviteCodes_WithoutAdminRole_ShouldReturn403() throws Exception {
        // Arrange
        GenerateInviteCodeRequest request = new GenerateInviteCodeRequest();
        request.setCount(5);
        request.setDescription("测试批次");

        // Act & Assert
        mockMvc.perform(post("/invite-codes/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
} 