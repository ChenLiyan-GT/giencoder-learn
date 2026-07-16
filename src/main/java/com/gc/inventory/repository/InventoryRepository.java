package com.gc.inventory.repository;

import com.common.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByCompanyCdAndProductCd(String companyCd, String productCd);
    
    Optional<Inventory> findByCompanyCdAndProductCdAndDeletedFlag(String companyCd, String productCd, String deletedFlag);
}
