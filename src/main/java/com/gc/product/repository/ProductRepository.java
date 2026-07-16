package com.gc.product.repository;

import com.common.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCd(String productCd);
    
    Optional<Product> findByProductCdAndDeletedFlag(String productCd, String deletedFlag);
}
