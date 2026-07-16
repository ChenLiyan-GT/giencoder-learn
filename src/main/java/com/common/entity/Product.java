package com.common.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "a101_product", schema = "scash")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_cd", nullable = false, unique = true, length = 20)
    private String productCd;

    @Column(name = "product_nm_kanji", nullable = false, length = 40)
    private String productNmKanji;

    @Column(name = "product_nm_kana", length = 40)
    private String productNmKana;

    @Column(name = "unit_cd", length = 10)
    private String unitCd;

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

    public Product() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductCd() { return productCd; }
    public void setProductCd(String productCd) { this.productCd = productCd; }

    public String getProductNmKanji() { return productNmKanji; }
    public void setProductNmKanji(String productNmKanji) { this.productNmKanji = productNmKanji; }

    public String getProductNmKana() { return productNmKana; }
    public void setProductNmKana(String productNmKana) { this.productNmKana = productNmKana; }

    public String getUnitCd() { return unitCd; }
    public void setUnitCd(String unitCd) { this.unitCd = unitCd; }

    public Instant getCreatedTs() { return createdTs; }
    public void setCreatedTs(Instant createdTs) { this.createdTs = createdTs; }

    public String getCreatedUserCd() { return createdUserCd; }
    public void setCreatedUserCd(String createdUserCd) { this.createdUserCd = createdUserCd; }

    public String getCreatedProgram() { return createdProgram; }
    public void setCreatedProgram(String createdProgram) { this.createdProgram = createdProgram; }

    public Instant getUpdatedTs() { return updatedTs; }
    public void setUpdatedTs(Instant updatedTs) { this.updatedTs = updatedTs; }

    public String getUpdatedUserCd() { return updatedUserCd; }
    public void setUpdatedUserCd(String updatedUserCd) { this.updatedUserCd = updatedUserCd; }

    public String getUpdatedProgram() { return updatedProgram; }
    public void setUpdatedProgram(String updatedProgram) { this.updatedProgram = updatedProgram; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(String deletedFlag) { this.deletedFlag = deletedFlag; }
}
