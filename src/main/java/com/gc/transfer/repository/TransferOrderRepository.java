package com.gc.transfer.repository;

import com.common.entity.TransferOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {
    
    Optional<TransferOrder> findByTransferOrderNo(String transferOrderNo);
    
    boolean existsByTransferOrderNo(String transferOrderNo);
}
