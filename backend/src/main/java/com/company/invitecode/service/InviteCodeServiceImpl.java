package com.company.invitecode.service;

import com.company.invitecode.dto.InviteCodeDto;
import com.company.invitecode.dto.UsageRecordDto;
import com.company.invitecode.dto.request.GenerateInviteCodeRequest;
import com.company.invitecode.dto.request.VerifyInviteCodeRequest;
import com.company.invitecode.model.InviteCode;
import com.company.invitecode.model.UsageRecord;
import com.company.invitecode.repository.InviteCodeRepository;
import com.company.invitecode.repository.UsageRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class InviteCodeServiceImpl implements InviteCodeService {

    private static final Logger log = LoggerFactory.getLogger(InviteCodeServiceImpl.class);

    private final InviteCodeRepository inviteCodeRepository;
    private final UsageRecordRepository usageRecordRepository;

    public InviteCodeServiceImpl(InviteCodeRepository inviteCodeRepository, UsageRecordRepository usageRecordRepository) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.usageRecordRepository = usageRecordRepository;
    }

    /**
     * 生成随机邀请码
     * 
     * @return 随机邀请码
     */
    private String generateRandomCode() {
        // 生成8-10位随机字符串，包含数字和大写字母
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        String code = uuid.substring(0, 8 + (int) (Math.random() * 3)); // 8-10位
        log.debug("生成随机邀请码: {}", code);
        return code;
    }

    @Override
    @Transactional
    public List<InviteCodeDto> generateInviteCodes(GenerateInviteCodeRequest request, String createdBy) {
        log.info("开始生成邀请码，数量: {}, 描述: {}, 创建人: {}", request.getCount(), request.getDescription(), createdBy);
        
        try {
            int count = request.getCount();
            String batchId = UUID.randomUUID().toString();
            log.debug("生成批次ID: {}", batchId);
            
            List<InviteCode> inviteCodes = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String code;
                int attempts = 0;
                do {
                    code = generateRandomCode();
                    attempts++;
                    if (attempts > 5) {
                        log.warn("尝试{}次后仍未生成唯一邀请码", attempts);
                    }
                } while (inviteCodeRepository.existsByCode(code));
                
                log.debug("创建第{}个邀请码: {}", i+1, code);
                
                InviteCode inviteCode = new InviteCode();
                inviteCode.setCode(code);
                inviteCode.setBatchId(batchId);
                inviteCode.setCreatedBy(createdBy);
                inviteCode.setActive(true);
                inviteCode.setDescription(request.getDescription());
                
                inviteCodes.add(inviteCode);
            }
            
            log.debug("开始保存{}个邀请码到数据库", inviteCodes.size());
            List<InviteCode> savedInviteCodes = inviteCodeRepository.saveAll(inviteCodes);
            log.info("成功保存{}个邀请码到数据库", savedInviteCodes.size());
            
            List<InviteCodeDto> dtos = savedInviteCodes.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            log.info("生成邀请码完成，返回{}个DTO对象", dtos.size());
            return dtos;
        } catch (Exception e) {
            log.error("生成邀请码异常", e);
            throw e;
        }
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
        UsageRecord usageRecord = new UsageRecord();
        usageRecord.setInviteCode(inviteCode);
        usageRecord.setUserId(request.getUserId());
        usageRecord.setIpAddress(ipAddress);
        usageRecord.setUserAgent(userAgent);
        
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
        
        InviteCodeDto dto = new InviteCodeDto();
        dto.setId(inviteCode.getId());
        dto.setCode(inviteCode.getCode());
        dto.setBatchId(inviteCode.getBatchId());
        dto.setCreatedAt(inviteCode.getCreatedAt());
        dto.setCreatedBy(inviteCode.getCreatedBy());
        dto.setDescription(inviteCode.getDescription());
        dto.setActive(inviteCode.isActive());
        dto.setUsageCount(usageCount);
        return dto;
    }
    
    /**
     * 将实体转换为DTO
     * 
     * @param usageRecord 使用记录实体
     * @return 使用记录DTO
     */
    private UsageRecordDto convertToDto(UsageRecord usageRecord) {
        UsageRecordDto dto = new UsageRecordDto();
        dto.setId(usageRecord.getId());
        dto.setInviteCode(usageRecord.getInviteCode().getCode());
        dto.setUserId(usageRecord.getUserId());
        dto.setIpAddress(usageRecord.getIpAddress());
        dto.setUserAgent(usageRecord.getUserAgent());
        dto.setUsedAt(usageRecord.getUsedAt());
        return dto;
    }
} 