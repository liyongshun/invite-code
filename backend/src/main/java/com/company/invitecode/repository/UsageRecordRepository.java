package com.company.invitecode.repository;

import com.company.invitecode.model.InviteCode;
import com.company.invitecode.model.UsageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {
    
    List<UsageRecord> findByInviteCode(InviteCode inviteCode);
    
    Page<UsageRecord> findByInviteCode(InviteCode inviteCode, Pageable pageable);
    
    Page<UsageRecord> findByUserId(String userId, Pageable pageable);
    
    int countByInviteCode(InviteCode inviteCode);
} 