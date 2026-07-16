package com.gc.transfer.dto;

import java.time.Instant;

public class TransferOrderDTO {

    private Long transferOrderId;
    private String transferOrderNo;
    private String fromCompanyCd;
    private String toCompanyCd;
    private String productCd;
    private Integer quantity;
    private Integer transferredQuantity;
    private String status;
    private Integer version;
    private String createdUserCd;
    private Instant createdTs;
    private String updatedUserCd;
    private Instant updatedTs;

    // Getters and Setters
    public Long getTransferOrderId() {
        return transferOrderId;
    }

    public void setTransferOrderId(Long transferOrderId) {
        this.transferOrderId = transferOrderId;
    }

    public String getTransferOrderNo() {
        return transferOrderNo;
    }

    public void setTransferOrderNo(String transferOrderNo) {
        this.transferOrderNo = transferOrderNo;
    }

    public String getFromCompanyCd() {
        return fromCompanyCd;
    }

    public void setFromCompanyCd(String fromCompanyCd) {
        this.fromCompanyCd = fromCompanyCd;
    }

    public String getToCompanyCd() {
        return toCompanyCd;
    }

    public void setToCompanyCd(String toCompanyCd) {
        this.toCompanyCd = toCompanyCd;
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

    public Integer getTransferredQuantity() {
        return transferredQuantity;
    }

    public void setTransferredQuantity(Integer transferredQuantity) {
        this.transferredQuantity = transferredQuantity;
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
