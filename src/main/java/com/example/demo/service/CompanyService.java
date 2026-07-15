package com.example.demo.service;

import com.example.demo.dto.CompanyDTO;
import com.example.demo.entity.Company;
import com.example.demo.repository.CompanyRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 公司 Service
 */
@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * 根据公司代码查询公司信息
     * @param companyCd 公司代码
     * @return 公司 DTO，不存在时返回 null
     */
    @Transactional(readOnly = true)
    public CompanyDTO getCompanyByCompanyCd(String companyCd) {
        Optional<Company> companyOptional = companyRepository.findByCompanyCd(companyCd);
        
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            return convertToDTO(company);
        }
        
        return null;
    }

    /**
     * 将 Entity 转换为 DTO
     * @param company 公司实体
     * @return 公司 DTO
     */
    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        BeanUtils.copyProperties(company, dto);
        return dto;
    }

    /**
     * 创建公司
     * @param companyDTO 公司 DTO
     * @return 创建后的公司 DTO
     */
    public CompanyDTO createCompany(CompanyDTO companyDTO) {
        // 将 DTO 属性复制到实体
        Company company = new Company();
        BeanUtils.copyProperties(companyDTO, company);
        // 删除标识默认设置为 "0"（未删除）
        if (company.getDeletedFlag() == null) {
            company.setDeletedFlag("0");
        }
        // 版本号默认设置为 0
        if (company.getVersion() == null) {
            company.setVersion(0);
        }
        // 保存实体并返回 DTO
        Company saved = companyRepository.save(company);
        return convertToDTO(saved);
    }

    /**
     * 根据公司代码更新公司信息
     * @param companyCd 公司代码
     * @param companyDTO 公司 DTO（包含更新内容）
     * @return 更新后的公司 DTO，公司不存在时返回 null
     */
    public CompanyDTO updateCompany(String companyCd, CompanyDTO companyDTO) {
        // 根据公司代码查询已有记录
        Optional<Company> companyOptional = companyRepository.findByCompanyCd(companyCd);
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            // 复制属性到实体，排除主键、公司代码、创建信息、版本号和删除标识等不可修改字段
            BeanUtils.copyProperties(companyDTO, company, "companyId", "companyCd", "createdTs", "createdUserCd", "createdProgram", "version", "deletedFlag");
            // 保存更新后的实体并返回 DTO
            Company updated = companyRepository.save(company);
            return convertToDTO(updated);
        }
        // 公司不存在时返回 null
        return null;
    }

    /**
     * 根据公司代码删除公司
     * @param companyCd 公司代码
     * @return 删除成功返回 true，公司不存在时返回 false
     */
    public boolean deleteCompany(String companyCd) {
        // 根据公司代码查询记录
        Optional<Company> companyOptional = companyRepository.findByCompanyCd(companyCd);
        if (companyOptional.isPresent()) {
            // 存在则删除并返回 true
            companyRepository.delete(companyOptional.get());
            return true;
        }
        // 公司不存在时返回 false
        return false;
    }

}