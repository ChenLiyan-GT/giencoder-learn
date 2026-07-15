package com.example.demo.repository;

import com.example.demo.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 公司 Repository 接口
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * 根据公司代码查询公司信息
     * @param companyCd 公司代码
     * @return 公司信息
     */
    Optional<Company> findByCompanyCd(String companyCd);
}
