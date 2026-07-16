package com.common.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "s101_inbound_order", schema = "scash")
public class InboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_order_id")
    private Long inboundOrderId;

    @Column(name = "inbound_order_cd", nullable = false, unique = true, length = 20)
    private String inboundOrderCd;

    @Column(name = "company_cd", nullable = false, length = 20)
    private String companyCd;

    @Column(name = "product_cd", nullable = false, length = 20)
    private String productCd;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

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

    public InboundOrder() {}

    public Long getInboundOrderId() { return inboundOrderId; }
    public void setInboundOrderId(Long inboundOrderId) { this.inboundOrderId = inboundOrderId; }

    public String getInboundOrderCd() { return inboundOrderCd; }
    public void setInboundOrderCd(String inboundOrderCd) { this.inboundOrderCd = inboundOrderCd; }

    public String getCompanyCd() { return companyCd; }
    public void setCompanyCd(String companyCd) { this.companyCd = companyCd; }

    public String getProductCd() { return productCd; }
    public void setProductCd(String productCd) { this.productCd = productCd; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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
