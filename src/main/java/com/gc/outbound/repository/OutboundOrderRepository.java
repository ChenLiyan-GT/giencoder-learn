package com.gc.outbound.repository;

import com.common.entity.OutboundOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutboundOrderRepository extends JpaRepository<OutboundOrder, Long> {
    Optional<OutboundOrder> findByOutboundOrderCdAndDeletedFlag(String outboundOrderCd, String deletedFlag);
    Optional<OutboundOrder> findByOutboundOrderCd(String outboundOrderCd);
}
