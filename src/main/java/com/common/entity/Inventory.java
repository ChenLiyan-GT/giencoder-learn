package com.common.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 库存实体类
 * 对应表：scash.a102_inventory
 */
@Entity
@Table(name = "a102_inventory", schema = "scash")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "company_cd", nullable = false, length = 20)
    private String companyCd;

    @Column(name = "product_cd", nullable = false, length = 20)
    private String productCd;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

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
    public Inventory() {
    }

    // Getters and Setters
    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
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

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
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

    /**
     * 计算可用库存数量
     * @return 可用数量 = 总数量 - 预留数量
     */
    public Integer getAvailableQuantity() {
        if (this.quantity == null || this.reservedQuantity == null) {
            return 0;
        }
        return this.quantity - this.reservedQuantity;
    }
}