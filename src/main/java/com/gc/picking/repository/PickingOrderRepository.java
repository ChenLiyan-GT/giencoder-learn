package com.gc.picking.repository;

import com.common.entity.PickingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PickingOrderRepository extends JpaRepository<PickingOrder, Long> {

    Optional<PickingOrder> findByPickingOrderNo(String pickingOrderNo);

    boolean existsByPickingOrderNo(String pickingOrderNo);
}
