package com.company.invitecode.repository;

import com.company.invitecode.model.InviteCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {
    
    Optional<InviteCode> findByCode(String code);
    
    boolean existsByCode(String code);
    
    Page<InviteCode> findByBatchId(String batchId, Pageable pageable);
    
    Page<InviteCode> findByActive(boolean isActive, Pageable pageable);
} 