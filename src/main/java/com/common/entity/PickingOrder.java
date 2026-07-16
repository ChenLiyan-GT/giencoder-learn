package com.common.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "s102_picking_order", schema = "scash")
public class PickingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "picking_order_id")
    private Long pickingOrderId;

    @Column(name = "picking_order_no", nullable = false, length = 20, unique = true)
    private String pickingOrderNo;

    @Column(name = "outbound_order_no", nullable = false, length = 20)
    private String outboundOrderNo;

    @Column(name = "product_cd", nullable = false, length = 20)
    private String productCd;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "picked_quantity", nullable = false)
    private Integer pickedQuantity;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, PICKING, COMPLETED, CANCELLED

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "deleted_flag", nullable = false, length = 1)
    private String deletedFlag;

    @Column(name = "created_user_cd", nullable = false, length = 50)
    private String createdUserCd;

    @Column(name = "created_ts", nullable = false)
    private Instant createdTs;

    @Column(name = "created_program", length = 50)
    private String createdProgram;

    @Column(name = "updated_user_cd", nullable = false, length = 50)
    private String updatedUserCd;

    @Column(name = "updated_ts", nullable = false)
    private Instant updatedTs;

    @Column(name = "updated_program", length = 50)
    private String updatedProgram;

    // Getters and Setters
    public Long getPickingOrderId() {
        return pickingOrderId;
    }

    public void setPickingOrderId(Long pickingOrderId) {
        this.pickingOrderId = pickingOrderId;
    }

    public String getPickingOrderNo() {
        return pickingOrderNo;
    }

    public void setPickingOrderNo(String pickingOrderNo) {
        this.pickingOrderNo = pickingOrderNo;
    }

    public String getOutboundOrderNo() {
        return outboundOrderNo;
    }

    public void setOutboundOrderNo(String outboundOrderNo) {
        this.outboundOrderNo = outboundOrderNo;
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

    public Integer getPickedQuantity() {
        return pickedQuantity;
    }

    public void setPickedQuantity(Integer pickedQuantity) {
        this.pickedQuantity = pickedQuantity;
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

    public String getCreatedProgram() {
        return createdProgram;
    }

    public void setCreatedProgram(String createdProgram) {
        this.createdProgram = createdProgram;
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

    public String getUpdatedProgram() {
        return updatedProgram;
    }

    public void setUpdatedProgram(String updatedProgram) {
        this.updatedProgram = updatedProgram;
    }
}
