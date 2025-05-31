package com.company.invitecode.service;

import com.company.invitecode.dto.InviteCodeDto;
import com.company.invitecode.dto.UsageRecordDto;
import com.company.invitecode.dto.request.GenerateInviteCodeRequest;
import com.company.invitecode.dto.request.VerifyInviteCodeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InviteCodeService {
    
    /**
     * 批量生成邀请码
     * 
     * @param request 生成请求参数
     * @param createdBy 创建人
     * @return 生成的邀请码列表
     */
    List<InviteCodeDto> generateInviteCodes(GenerateInviteCodeRequest request, String createdBy);
    
    /**
     * 验证邀请码
     * 
     * @param request 验证请求参数
     * @param ipAddress 用户IP地址
     * @param userAgent 用户浏览器信息
     * @return 验证结果
     */
    boolean verifyInviteCode(VerifyInviteCodeRequest request, String ipAddress, String userAgent);
    
    /**
     * 根据ID获取邀请码
     * 
     * @param id 邀请码ID
     * @return 邀请码信息
     */
    Optional<InviteCodeDto> getInviteCode(Long id);
    
    /**
     * 根据邀请码字符串获取邀请码
     * 
     * @param code 邀请码字符串
     * @return 邀请码信息
     */
    Optional<InviteCodeDto> getInviteCodeByCode(String code);
    
    /**
     * 分页获取所有邀请码
     * 
     * @param pageable 分页参数
     * @return 邀请码分页结果
     */
    Page<InviteCodeDto> getAllInviteCodes(Pageable pageable);
    
    /**
     * 获取邀请码的使用记录
     * 
     * @param codeId 邀请码ID
     * @param pageable 分页参数
     * @return 使用记录分页结果
     */
    Page<UsageRecordDto> getInviteCodeUsageRecords(Long codeId, Pageable pageable);
    
    /**
     * 禁用邀请码
     * 
     * @param id 邀请码ID
     * @return 更新后的邀请码信息
     */
    Optional<InviteCodeDto> disableInviteCode(Long id);
    
    /**
     * 启用邀请码
     * 
     * @param id 邀请码ID
     * @return 更新后的邀请码信息
     */
    Optional<InviteCodeDto> enableInviteCode(Long id);
} 