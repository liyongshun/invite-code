package com.company.invitecode.controller;

import com.company.invitecode.dto.InviteCodeDto;
import com.company.invitecode.dto.UsageRecordDto;
import com.company.invitecode.dto.request.GenerateInviteCodeRequest;
import com.company.invitecode.dto.request.VerifyInviteCodeRequest;
import com.company.invitecode.dto.response.ApiResponse;
import com.company.invitecode.service.InviteCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/invite-codes")
public class InviteCodeController {

    private static final Logger log = LoggerFactory.getLogger(InviteCodeController.class);

    private final InviteCodeService inviteCodeService;

    public InviteCodeController(InviteCodeService inviteCodeService) {
        this.inviteCodeService = inviteCodeService;
    }

    /**
     * 生成邀请码
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<InviteCodeDto>>> generateInviteCodes(
            @Valid @RequestBody GenerateInviteCodeRequest request,
            HttpServletRequest httpRequest) {
        log.info("收到生成邀请码请求: {}, 请求源: {}", request, httpRequest.getRemoteAddr());
        try {
            log.info("开始处理生成邀请码请求");
            List<InviteCodeDto> inviteCodes = inviteCodeService.generateInviteCodes(request, "admin");
            log.info("成功生成{}个邀请码", inviteCodes.size());
            
            ApiResponse<List<InviteCodeDto>> response = ApiResponse.success("成功生成" + inviteCodes.size() + "个邀请码", inviteCodes);
            log.info("生成邀请码响应: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("生成邀请码异常", e);
            return ResponseEntity.ok(ApiResponse.error("生成邀请码失败: " + e.getMessage()));
        }
    }

    /**
     * 验证邀请码
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyInviteCode(
            @Valid @RequestBody VerifyInviteCodeRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        
        log.info("验证邀请码请求: {}, IP: {}", request, ipAddress);
        boolean isValid = inviteCodeService.verifyInviteCode(request, ipAddress, userAgent);
        
        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("邀请码验证成功", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("无效的邀请码"));
        }
    }

    /**
     * 获取所有邀请码
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InviteCodeDto>>> getAllInviteCodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<InviteCodeDto> inviteCodes = inviteCodeService.getAllInviteCodes(pageable);
        return ResponseEntity.ok(ApiResponse.success(inviteCodes));
    }

    /**
     * 获取邀请码详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InviteCodeDto>> getInviteCode(@PathVariable Long id) {
        return inviteCodeService.getInviteCode(id)
                .map(inviteCodeDto -> ResponseEntity.ok(ApiResponse.success(inviteCodeDto)))
                .orElse(ResponseEntity.ok(ApiResponse.error("邀请码不存在")));
    }

    /**
     * 根据邀请码字符串获取详情
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<InviteCodeDto>> getInviteCodeByCode(@PathVariable String code) {
        return inviteCodeService.getInviteCodeByCode(code)
                .map(inviteCodeDto -> ResponseEntity.ok(ApiResponse.success(inviteCodeDto)))
                .orElse(ResponseEntity.ok(ApiResponse.error("邀请码不存在")));
    }

    /**
     * 获取邀请码使用记录
     */
    @GetMapping("/{id}/usage-records")
    public ResponseEntity<ApiResponse<Page<UsageRecordDto>>> getInviteCodeUsageRecords(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "usedAt"));
        Page<UsageRecordDto> usageRecords = inviteCodeService.getInviteCodeUsageRecords(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(usageRecords));
    }

    /**
     * 禁用邀请码
     */
    @PutMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<InviteCodeDto>> disableInviteCode(@PathVariable Long id) {
        return inviteCodeService.disableInviteCode(id)
                .map(inviteCodeDto -> ResponseEntity.ok(ApiResponse.success("邀请码已禁用", inviteCodeDto)))
                .orElse(ResponseEntity.ok(ApiResponse.error("邀请码不存在")));
    }

    /**
     * 启用邀请码
     */
    @PutMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<InviteCodeDto>> enableInviteCode(@PathVariable Long id) {
        return inviteCodeService.enableInviteCode(id)
                .map(inviteCodeDto -> ResponseEntity.ok(ApiResponse.success("邀请码已启用", inviteCodeDto)))
                .orElse(ResponseEntity.ok(ApiResponse.error("邀请码不存在")));
    }
} 