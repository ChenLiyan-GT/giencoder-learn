package com.gc.outbound.dto;

public class OutboundOrderDTO {

    private String outboundOrderCd;
    private String companyCd;
    private String productCd;
    private Integer quantity;
    private String status;

    // Getters and Setters
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
}
