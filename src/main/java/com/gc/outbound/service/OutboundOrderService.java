package com.gc.outbound.service;

import com.common.entity.Inventory;
import com.common.entity.OutboundOrder;
import com.common.entity.Product;
import com.gc.inventory.repository.InventoryRepository;
import com.gc.outbound.dto.OutboundOrderDTO;
import com.gc.outbound.repository.OutboundOrderRepository;
import com.gc.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class OutboundOrderService {

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public OutboundOrderDTO create(OutboundOrderDTO dto) {
        // 检查单号是否重复
        if (outboundOrderRepository.findByOutboundOrderCd(dto.getOutboundOrderCd()).isPresent()) {
            throw new RuntimeException("出库单号已存在：" + dto.getOutboundOrderCd());
        }

        // 检查商品代码是否为空
        if (dto.getProductCd() == null || dto.getProductCd().trim().isEmpty()) {
            throw new RuntimeException("商品代码不能为空");
        }

        // 检查商品是否存在
        Product product = productRepository.findByProductCd(dto.getProductCd())
                .orElseThrow(() -> new RuntimeException("商品不存在：" + dto.getProductCd()));

        // 检查商品是否已被删除
        if ("1".equals(product.getDeletedFlag())) {
            throw new RuntimeException("商品已被删除：" + dto.getProductCd());
        }

        // 检查数量合法性
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new RuntimeException("出库数量必须大于 0");
        }

        // 检查库存是否充足（可用库存 = quantity - reservedQuantity）
        String companyCd = dto.getCompanyCd() != null ? dto.getCompanyCd() : "C001";
        Inventory inventory = inventoryRepository.findByCompanyCdAndProductCdAndDeletedFlag(companyCd, dto.getProductCd(), "0")
                .orElseThrow(() -> new RuntimeException("库存不存在：" + dto.getProductCd()));

        if (inventory.getAvailableQuantity() < dto.getQuantity()) {
            throw new RuntimeException("库存不足：可用库存=" + inventory.getAvailableQuantity() + ", 请求数量=" + dto.getQuantity());
        }

        OutboundOrder entity = new OutboundOrder();
        entity.setOutboundOrderCd(dto.getOutboundOrderCd());
        entity.setCompanyCd(companyCd);
        entity.setProductCd(dto.getProductCd());
        entity.setQuantity(dto.getQuantity());
        entity.setStatus("PENDING"); // 默认状态
        entity.setVersion(0);
        entity.setDeletedFlag("0");
        entity.setCreatedUserCd("system");
        entity.setCreatedTs(Instant.now());
        entity.setUpdatedUserCd("system");
        entity.setUpdatedTs(Instant.now());

        OutboundOrder saved = outboundOrderRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public OutboundOrderDTO get(String outboundOrderCd) {
        OutboundOrder entity = outboundOrderRepository.findByOutboundOrderCd(outboundOrderCd)
                .orElseThrow(() -> new RuntimeException("出库单不存在：" + outboundOrderCd));
        return toDTO(entity);
    }

    private OutboundOrderDTO toDTO(OutboundOrder entity) {
        OutboundOrderDTO dto = new OutboundOrderDTO();
        dto.setOutboundOrderCd(entity.getOutboundOrderCd());
        dto.setCompanyCd(entity.getCompanyCd());
        dto.setProductCd(entity.getProductCd());
        dto.setQuantity(entity.getQuantity());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
