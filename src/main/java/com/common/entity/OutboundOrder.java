package com.common.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "s103_outbound_order", schema = "scash")
public class OutboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbound_order_id")
    private Long outboundOrderId;

    @Column(name = "outbound_order_cd", nullable = false, length = 50)
    private String outboundOrderCd;

    @Column(name = "company_cd", nullable = false, length = 20)
    private String companyCd;

    @Column(name = "product_cd", nullable = false, length = 50)
    private String productCd;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, SHIPPED, CANCELLED

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "deleted_flag", nullable = false, length = 1)
    private String deletedFlag;

    @Column(name = "created_user_cd", nullable = false, length = 50)
    private String createdUserCd;

    @Column(name = "created_ts", nullable = false)
    private Instant createdTs;

    @Column(name = "updated_user_cd", nullable = false, length = 50)
    private String updatedUserCd;

    @Column(name = "updated_ts", nullable = false)
    private Instant updatedTs;

    // Getters and Setters
    public Long getOutboundOrderId() {
        return outboundOrderId;
    }

    public void setOutboundOrderId(Long outboundOrderId) {
        this.outboundOrderId = outboundOrderId;
    }

    public String getOutboundOrderCd() {
        return outboundOrderCd;
    }

    public void setOutboundOrderCd(String outboundOrderCd) {
        this.outboundOrderCd = outboundOrderCd;
    }

    public String getCompanyCd() {
        return companyCd;
    }

    public void setCompanyCd(String companyCd) {
        this.companyCd = companyCd;
    }

    public String getProductCd() {
        return productCd;
    }

    public void setProductCd(String productCd) {
        this.productCd = productCd;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCreatedUserCd() {
        return createdUserCd;
    }

    public void setCreatedUserCd(String createdUserCd) {
        this.createdUserCd = createdUserCd;
    }

    public Instant getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Instant createdTs) {
        this.createdTs = createdTs;
    }

    public String getUpdatedUserCd() {
        return updatedUserCd;
    }

    public void setUpdatedUserCd(String updatedUserCd) {
        this.updatedUserCd = updatedUserCd;
    }

    public Instant getUpdatedTs() {
        return updatedTs;
    }

    public void setUpdatedTs(Instant updatedTs) {
        this.updatedTs = updatedTs;
    }
}
