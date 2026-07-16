package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 公司实体类
 * 对应表：scash.m101_company
 */
@Entity
@Table(name = "m101_company", schema = "scash")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_cd", nullable = false, length = 20)
    private String companyCd;

    @Column(name = "company_nm_kana", length = 40)
    private String companyNmKana;

    @Column(name = "company_nm_kanji", nullable = false, length = 40)
    private String companyNmKanji;

    @Column(name = "company_abbreviation", length = 20)
    private String companyAbbreviation;

    @Column(name = "postal_cd", length = 7)
    private String postalCd;

    @Column(name = "area_cd", length = 5)
    private String areaCd;

    @Column(name = "address", length = 240)
    private String address;

    @Column(name = "phone_no", length = 20)
    private String phoneNo;

    @Column(name = "fax_no", length = 20)
    private String faxNo;

    @Column(name = "created_ts")
    private Instant createdTs;

    @Column(name = "created_user_cd", length = 16)
    private String createdUserCd;

    @Column(name = "created_program", length = 50)
    private String createdProgram;

    @Column(name = "updated_ts")
    private Instant updatedTs;

    @Column(name = "updated_user_cd", length = 16)
    private String updatedUserCd;

    @Column(name = "updated_program", length = 50)
    private String updatedProgram;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "deleted_flag", nullable = false, length = 1)
    private String deletedFlag;

    // Default constructor
    public Company() {
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
