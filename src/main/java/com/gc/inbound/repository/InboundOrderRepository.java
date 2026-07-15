package com.gc.inbound.repository;

import com.common.entity.InboundOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {
    Optional<InboundOrder> findByInboundOrderCdAndDeletedFlag(String inboundOrderCd, String deletedFlag);
    
    default Optional<InboundOrder> findByInboundOrderCd(String inboundOrderCd) {
        return findByInboundOrderCdAndDeletedFlag(inboundOrderCd, "0");
    }
}
