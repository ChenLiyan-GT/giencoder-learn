package com.gc.inbound.dto;

public class InboundOrderDTO {

    private String inboundOrderCd;
    private String companyCd;
    private String productCd;
    private Integer quantity;
    private String status;

    // Getters and Setters
    public String getInboundOrderCd() {
        return inboundOrderCd;
    }

    public void setInboundOrderCd(String inboundOrderCd) {
        this.inboundOrderCd = inboundOrderCd;
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
}
