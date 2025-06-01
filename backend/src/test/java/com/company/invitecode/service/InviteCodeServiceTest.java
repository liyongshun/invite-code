package com.company.invitecode.service;

import com.company.invitecode.dto.InviteCodeDto;
import com.company.invitecode.dto.request.GenerateInviteCodeRequest;
import com.company.invitecode.dto.request.VerifyInviteCodeRequest;
import com.company.invitecode.model.InviteCode;
import com.company.invitecode.model.UsageRecord;
import com.company.invitecode.repository.InviteCodeRepository;
import com.company.invitecode.repository.UsageRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InviteCodeServiceTest {

    @Mock
    private InviteCodeRepository inviteCodeRepository;

    @Mock
    private UsageRecordRepository usageRecordRepository;

    @InjectMocks
    private InviteCodeServiceImpl inviteCodeService;

    private InviteCode inviteCode;
    private UsageRecord usageRecord;
    private final String testCode = "TEST12345678";

    @BeforeEach
    void setUp() {
        inviteCode = new InviteCode();
        inviteCode.setId(1L);
        inviteCode.setCode(testCode);
        inviteCode.setBatchId(UUID.randomUUID().toString());
        inviteCode.setCreatedAt(LocalDateTime.now());
        inviteCode.setCreatedBy("admin");
        inviteCode.setActive(true);
        inviteCode.setUsageRecords(new ArrayList<>());

        usageRecord = new UsageRecord();
        usageRecord.setId(1L);
        usageRecord.setInviteCode(inviteCode);
        usageRecord.setUserId("user123");
        usageRecord.setIpAddress("127.0.0.1");
        usageRecord.setUserAgent("Mozilla/5.0");
        usageRecord.setUsedAt(LocalDateTime.now());
    }

    @Test
    void generateInviteCodes_ShouldReturnGeneratedCodes() {
        // Arrange
        GenerateInviteCodeRequest request = new GenerateInviteCodeRequest();
        request.setCount(5);
        request.setDescription("测试批次");
        
        List<InviteCode> inviteCodes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            InviteCode code = new InviteCode();
            code.setId((long) i);
            code.setCode("TEST" + i);
            code.setBatchId(UUID.randomUUID().toString());
            code.setCreatedAt(LocalDateTime.now());
            code.setCreatedBy("admin");
            code.setActive(true);
            inviteCodes.add(code);
        }
        
        when(inviteCodeRepository.saveAll(anyList())).thenReturn(inviteCodes);

        // Act
        List<InviteCodeDto> result = inviteCodeService.generateInviteCodes(request, "admin");

        // Assert
        assertThat(result).hasSize(5);
        verify(inviteCodeRepository, times(1)).saveAll(anyList());
    }

    @Test
    void verifyInviteCode_WithValidCode_ShouldReturnTrue() {
        // Arrange
        VerifyInviteCodeRequest request = new VerifyInviteCodeRequest();
        request.setCode(testCode);
        request.setUserId("user123");
        
        when(inviteCodeRepository.findByCode(testCode)).thenReturn(Optional.of(inviteCode));
        when(usageRecordRepository.save(any(UsageRecord.class))).thenReturn(usageRecord);

        // Act
        boolean result = inviteCodeService.verifyInviteCode(request, "127.0.0.1", "Mozilla/5.0");

        // Assert
        assertThat(result).isTrue();
        verify(inviteCodeRepository, times(1)).findByCode(testCode);
        verify(usageRecordRepository, times(1)).save(any(UsageRecord.class));
    }

    @Test
    void verifyInviteCode_WithInvalidCode_ShouldReturnFalse() {
        // Arrange
        VerifyInviteCodeRequest request = new VerifyInviteCodeRequest();
        request.setCode("INVALID");
        request.setUserId("user123");
        
        when(inviteCodeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act
        boolean result = inviteCodeService.verifyInviteCode(request, "127.0.0.1", "Mozilla/5.0");

        // Assert
        assertThat(result).isFalse();
        verify(inviteCodeRepository, times(1)).findByCode("INVALID");
        verify(usageRecordRepository, never()).save(any(UsageRecord.class));
    }

    @Test
    void verifyInviteCode_WithInactiveCode_ShouldReturnFalse() {
        // Arrange
        inviteCode.setActive(false);
        VerifyInviteCodeRequest request = new VerifyInviteCodeRequest();
        request.setCode(testCode);
        request.setUserId("user123");
        
        when(inviteCodeRepository.findByCode(testCode)).thenReturn(Optional.of(inviteCode));

        // Act
        boolean result = inviteCodeService.verifyInviteCode(request, "127.0.0.1", "Mozilla/5.0");

        // Assert
        assertThat(result).isFalse();
        verify(inviteCodeRepository, times(1)).findByCode(testCode);
        verify(usageRecordRepository, never()).save(any(UsageRecord.class));
    }

    @Test
    void getInviteCode_ShouldReturnInviteCodeDto() {
        // Arrange
        when(inviteCodeRepository.findById(1L)).thenReturn(Optional.of(inviteCode));
        when(usageRecordRepository.countByInviteCode(inviteCode)).thenReturn(1);

        // Act
        Optional<InviteCodeDto> result = inviteCodeService.getInviteCode(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo(testCode);
        assertThat(result.get().getUsageCount()).isEqualTo(1);
        verify(inviteCodeRepository, times(1)).findById(1L);
        verify(usageRecordRepository, times(1)).countByInviteCode(inviteCode);
    }

    @Test
    void getAllInviteCodes_ShouldReturnPageOfInviteCodeDtos() {
        // Arrange
        List<InviteCode> inviteCodes = new ArrayList<>();
        inviteCodes.add(inviteCode);
        Page<InviteCode> page = new PageImpl<>(inviteCodes);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(inviteCodeRepository.findAll(pageable)).thenReturn(page);
        when(usageRecordRepository.countByInviteCode(any(InviteCode.class))).thenReturn(1);

        // Act
        Page<InviteCodeDto> result = inviteCodeService.getAllInviteCodes(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo(testCode);
        verify(inviteCodeRepository, times(1)).findAll(pageable);
    }

    @Test
    void getInviteCodeByCode_ShouldReturnInviteCodeDto() {
        // Arrange
        when(inviteCodeRepository.findByCode(testCode)).thenReturn(Optional.of(inviteCode));
        when(usageRecordRepository.countByInviteCode(inviteCode)).thenReturn(1);

        // Act
        Optional<InviteCodeDto> result = inviteCodeService.getInviteCodeByCode(testCode);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo(testCode);
        assertThat(result.get().getUsageCount()).isEqualTo(1);
        verify(inviteCodeRepository, times(1)).findByCode(testCode);
        verify(usageRecordRepository, times(1)).countByInviteCode(inviteCode);
    }
} 