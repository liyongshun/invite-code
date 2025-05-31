package com.company.invitecode.service;

import com.company.invitecode.dto.InviteCodeDto;
import com.company.invitecode.dto.UsageRecordDto;
import com.company.invitecode.dto.request.GenerateInviteCodeRequest;
import com.company.invitecode.dto.request.VerifyInviteCodeRequest;
import com.company.invitecode.model.InviteCode;
import com.company.invitecode.model.UsageRecord;
import com.company.invitecode.repository.InviteCodeRepository;
import com.company.invitecode.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteCodeServiceImpl implements InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;
    private final UsageRecordRepository usageRecordRepository;

    /**
     * 生成随机邀请码
     * 
     * @return 随机邀请码
     */
    private String generateRandomCode() {
        // 生成8-10位随机字符串，包含数字和大写字母
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        return uuid.substring(0, 8 + (int) (Math.random() * 3)); // 8-10位
    }

    @Override
    @Transactional
    public List<InviteCodeDto> generateInviteCodes(GenerateInviteCodeRequest request, String createdBy) {
        int count = request.getCount();
        String batchId = UUID.randomUUID().toString();
        
        List<InviteCode> inviteCodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String code;
            do {
                code = generateRandomCode();
            } while (inviteCodeRepository.existsByCode(code));
            
            InviteCode inviteCode = InviteCode.builder()
                    .code(code)
                    .batchId(batchId)
                    .createdBy(createdBy)
                    .isActive(true)
                    .build();
            
            inviteCodes.add(inviteCode);
        }
        
        List<InviteCode> savedInviteCodes = inviteCodeRepository.saveAll(inviteCodes);
        return savedInviteCodes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean verifyInviteCode(VerifyInviteCodeRequest request, String ipAddress, String userAgent) {
        Optional<InviteCode> optionalInviteCode = inviteCodeRepository.findByCode(request.getCode());
        
        if (optionalInviteCode.isEmpty() || !optionalInviteCode.get().isActive()) {
            log.warn("无效的邀请码: {}", request.getCode());
            return false;
        }
        
        InviteCode inviteCode = optionalInviteCode.get();
        
        // 记录使用记录
        UsageRecord usageRecord = UsageRecord.builder()
                .inviteCode(inviteCode)
                .userId(request.getUserId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        
        usageRecordRepository.save(usageRecord);
        log.info("邀请码使用成功: {}, 用户: {}", request.getCode(), request.getUserId());
        
        return true;
    }

    @Override
    public Optional<InviteCodeDto> getInviteCode(Long id) {
        return inviteCodeRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public Optional<InviteCodeDto> getInviteCodeByCode(String code) {
        return inviteCodeRepository.findByCode(code)
                .map(this::convertToDto);
    }

    @Override
    public Page<InviteCodeDto> getAllInviteCodes(Pageable pageable) {
        Page<InviteCode> inviteCodePage = inviteCodeRepository.findAll(pageable);
        List<InviteCodeDto> inviteCodeDtos = inviteCodePage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(inviteCodeDtos, pageable, inviteCodePage.getTotalElements());
    }

    @Override
    public Page<UsageRecordDto> getInviteCodeUsageRecords(Long codeId, Pageable pageable) {
        Optional<InviteCode> optionalInviteCode = inviteCodeRepository.findById(codeId);
        if (optionalInviteCode.isEmpty()) {
            return Page.empty(pageable);
        }
        
        InviteCode inviteCode = optionalInviteCode.get();
        Page<UsageRecord> usageRecordPage = usageRecordRepository.findByInviteCode(inviteCode, pageable);
        
        List<UsageRecordDto> usageRecordDtos = usageRecordPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(usageRecordDtos, pageable, usageRecordPage.getTotalElements());
    }

    @Override
    @Transactional
    public Optional<InviteCodeDto> disableInviteCode(Long id) {
        Optional<InviteCode> optionalInviteCode = inviteCodeRepository.findById(id);
        if (optionalInviteCode.isEmpty()) {
            return Optional.empty();
        }
        
        InviteCode inviteCode = optionalInviteCode.get();
        inviteCode.setActive(false);
        InviteCode savedInviteCode = inviteCodeRepository.save(inviteCode);
        
        return Optional.of(convertToDto(savedInviteCode));
    }

    @Override
    @Transactional
    public Optional<InviteCodeDto> enableInviteCode(Long id) {
        Optional<InviteCode> optionalInviteCode = inviteCodeRepository.findById(id);
        if (optionalInviteCode.isEmpty()) {
            return Optional.empty();
        }
        
        InviteCode inviteCode = optionalInviteCode.get();
        inviteCode.setActive(true);
        InviteCode savedInviteCode = inviteCodeRepository.save(inviteCode);
        
        return Optional.of(convertToDto(savedInviteCode));
    }
    
    /**
     * 将实体转换为DTO
     * 
     * @param inviteCode 邀请码实体
     * @return 邀请码DTO
     */
    private InviteCodeDto convertToDto(InviteCode inviteCode) {
        int usageCount = usageRecordRepository.countByInviteCode(inviteCode);
        
        return InviteCodeDto.builder()
                .id(inviteCode.getId())
                .code(inviteCode.getCode())
                .batchId(inviteCode.getBatchId())
                .createdAt(inviteCode.getCreatedAt())
                .createdBy(inviteCode.getCreatedBy())
                .isActive(inviteCode.isActive())
                .usageCount(usageCount)
                .build();
    }
    
    /**
     * 将实体转换为DTO
     * 
     * @param usageRecord 使用记录实体
     * @return 使用记录DTO
     */
    private UsageRecordDto convertToDto(UsageRecord usageRecord) {
        return UsageRecordDto.builder()
                .id(usageRecord.getId())
                .inviteCode(usageRecord.getInviteCode().getCode())
                .userId(usageRecord.getUserId())
                .ipAddress(usageRecord.getIpAddress())
                .userAgent(usageRecord.getUserAgent())
                .usedAt(usageRecord.getUsedAt())
                .build();
    }
} 