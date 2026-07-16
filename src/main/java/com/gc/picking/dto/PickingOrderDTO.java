package com.gc.picking.dto;

public class PickingOrderDTO {

    private Long pickingOrderId;
    private String pickingOrderNo;
    private String outboundOrderNo;
    private String productCd;
    private Integer quantity;
    private Integer pickedQuantity;
    private String status;

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
}
