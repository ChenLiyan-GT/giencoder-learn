package com.example.demo.dto;

import java.time.Instant;

/**
 * 公司 DTO
 * 用于封装查询结果
 */
public class CompanyDTO {

    private Long companyId;
    private String companyCd;
    private String companyNmKana;
    private String companyNmKanji;
    private String companyAbbreviation;
    private String postalCd;
    private String areaCd;
    private String address;
    private String phoneNo;
    private String faxNo;
    private Instant createdTs;
    private String createdUserCd;
    private String createdProgram;
    private Instant updatedTs;
    private String updatedUserCd;
    private String updatedProgram;
    private Integer version;
    private String deletedFlag;

    // Default constructor
    public CompanyDTO() {
    }

    // Getters and Setters
    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyCd() {
        return companyCd;
    }

    public void setCompanyCd(String companyCd) {
        this.companyCd = companyCd;
    }

    public String getCompanyNmKana() {
        return companyNmKana;
    }

    public void setCompanyNmKana(String companyNmKana) {
        this.companyNmKana = companyNmKana;
    }

    public String getCompanyNmKanji() {
        return companyNmKanji;
    }

    public void setCompanyNmKanji(String companyNmKanji) {
        this.companyNmKanji = companyNmKanji;
    }

    public String getCompanyAbbreviation() {
        return companyAbbreviation;
    }

    public void setCompanyAbbreviation(String companyAbbreviation) {
        this.companyAbbreviation = companyAbbreviation;
    }

    public String getPostalCd() {
        return postalCd;
    }

    public void setPostalCd(String postalCd) {
        this.postalCd = postalCd;
    }

    public String getAreaCd() {
        return areaCd;
    }

    public void setAreaCd(String areaCd) {
        this.areaCd = areaCd;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getFaxNo() {
        return faxNo;
    }

    public void setFaxNo(String faxNo) {
        this.faxNo = faxNo;
    }

    public Instant getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Instant createdTs) {
        this.createdTs = createdTs;
    }

    public String getCreatedUserCd() {
        return createdUserCd;
    }

    public void setCreatedUserCd(String createdUserCd) {
        this.createdUserCd = createdUserCd;
    }

    public String getCreatedProgram() {
        return createdProgram;
    }

    public void setCreatedProgram(String createdProgram) {
        this.createdProgram = createdProgram;
    }

    public Instant getUpdatedTs() {
        return updatedTs;
    }

    public void setUpdatedTs(Instant updatedTs) {
        this.updatedTs = updatedTs;
    }

    public String getUpdatedUserCd() {
        return updatedUserCd;
    }

    public void setUpdatedUserCd(String updatedUserCd) {
        this.updatedUserCd = updatedUserCd;
    }

    public String getUpdatedProgram() {
        return updatedProgram;
    }

    public void setUpdatedProgram(String updatedProgram) {
        this.updatedProgram = updatedProgram;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDeletedFlag() {
        return deletedFlag;
    }

    public void setDeletedFlag(String deletedFlag) {
        this.deletedFlag = deletedFlag;
    }
}
