package com.gc.product.dto;

import java.time.Instant;

public class ProductDTO {

    private Long productId;
    private String productCd;
    private String productNmKanji;
    private String productNmKana;
    private String unitCd;
    private Instant createdTs;
    private String createdUserCd;
    private String createdProgram;
    private Instant updatedTs;
    private String updatedUserCd;
    private String updatedProgram;
    private Integer version;
    private String deletedFlag;

    public ProductDTO() {}

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
