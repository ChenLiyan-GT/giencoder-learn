package com.gc.inbound.service;

import com.common.entity.InboundOrder;
import com.common.entity.Product;
import com.gc.inbound.dto.InboundOrderDTO;
import com.gc.inbound.repository.InboundOrderRepository;
import com.gc.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class InboundOrderService {

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    public InboundOrderDTO create(InboundOrderDTO dto) {
        // 检查单号是否重复
        if (inboundOrderRepository.findByInboundOrderCd(dto.getInboundOrderCd()).isPresent()) {
            throw new RuntimeException("入库单号已存在：" + dto.getInboundOrderCd());
        }

        // 检查商品代码是否为空
        if (dto.getProductCd() == null || dto.getProductCd().trim().isEmpty()) {
            throw new RuntimeException("商品代码不能为空");
        }

        // 检查商品是否存在且未被删除
        Product product = productRepository.findByProductCd(dto.getProductCd())
                .orElseThrow(() -> new RuntimeException("商品不存在：" + dto.getProductCd()));
        
        // 检查商品是否已被删除
        if ("1".equals(product.getDeletedFlag())) {
            throw new RuntimeException("商品已被删除：" + dto.getProductCd());
        }

        // 检查数量合法性
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new RuntimeException("入库数量必须大于 0");
        }

        InboundOrder entity = new InboundOrder();
        entity.setInboundOrderCd(dto.getInboundOrderCd());
        entity.setProductCd(dto.getProductCd());
        entity.setQuantity(dto.getQuantity());
        entity.setStatus("RECEIVED"); // 默认状态
        entity.setVersion(0);
        entity.setDeletedFlag("0");
        entity.setCreatedUserCd("system");
        entity.setCreatedTs(Instant.now());
        entity.setUpdatedUserCd("system");
        entity.setUpdatedTs(Instant.now());

        InboundOrder saved = inboundOrderRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public InboundOrderDTO get(String inboundOrderCd) {
        InboundOrder entity = inboundOrderRepository.findByInboundOrderCd(inboundOrderCd)
                .orElseThrow(() -> new RuntimeException("入库单不存在：" + inboundOrderCd));
        return toDTO(entity);
    }

    private InboundOrderDTO toDTO(InboundOrder entity) {
        InboundOrderDTO dto = new InboundOrderDTO();
        dto.setInboundOrderCd(entity.getInboundOrderCd());
        dto.setProductCd(entity.getProductCd());
        dto.setQuantity(entity.getQuantity());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
