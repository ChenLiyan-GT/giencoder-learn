package com.gc.inbound.service;

import com.common.entity.InboundOrder;
import com.common.entity.Inventory;
import com.common.entity.Product;
import com.gc.inbound.dto.InboundOrderDTO;
import com.gc.inbound.repository.InboundOrderRepository;
import com.gc.inventory.repository.InventoryRepository;
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

    @Autowired
    private InventoryRepository inventoryRepository;

    public InboundOrderDTO create(InboundOrderDTO dto) {
        // 检查单号是否重复
        if (inboundOrderRepository.findByInboundOrderCd(dto.getInboundOrderCd()).isPresent()) {
            throw new RuntimeException("入库单号已存在：" + dto.getInboundOrderCd());
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
            throw new RuntimeException("入库数量必须大于 0");
        }

        InboundOrder entity = new InboundOrder();
        entity.setInboundOrderCd(dto.getInboundOrderCd());
        entity.setCompanyCd(dto.getCompanyCd());
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

    /**
     * 确认入库 - 状态变更为 CONFIRMED，同时增加库存
     */
    public InboundOrderDTO confirm(String inboundOrderCd) {
        InboundOrder entity = inboundOrderRepository.findByInboundOrderCd(inboundOrderCd)
                .orElseThrow(() -> new RuntimeException("入库单不存在：" + inboundOrderCd));

        // 检查状态是否为 RECEIVED
        if (!"RECEIVED".equals(entity.getStatus())) {
            throw new RuntimeException("只有 RECEIVED 状态的入库单才能确认");
        }

        // 更新状态
        entity.setStatus("CONFIRMED");
        entity.setUpdatedTs(Instant.now());
        entity.setUpdatedUserCd("system");

        // 增加库存
        Inventory inventory = inventoryRepository.findByCompanyCdAndProductCdAndDeletedFlag(
                entity.getCompanyCd(), entity.getProductCd(), "0").orElse(null);

        if (inventory == null) {
            // 创建新的库存记录
            inventory = new Inventory();
            inventory.setCompanyCd(entity.getCompanyCd());
            inventory.setProductCd(entity.getProductCd());
            inventory.setQuantity(entity.getQuantity());
            inventory.setReservedQuantity(0);
            inventory.setVersion(0);
            inventory.setDeletedFlag("0");
            inventory.setCreatedUserCd("system");
            inventory.setCreatedTs(Instant.now());
            inventory.setUpdatedUserCd("system");
            inventory.setUpdatedTs(Instant.now());
        } else {
            // 更新现有库存
            inventory.setQuantity(inventory.getQuantity() + entity.getQuantity());
            inventory.setUpdatedTs(Instant.now());
            inventory.setUpdatedUserCd("system");
        }

        inventoryRepository.save(inventory);
        InboundOrder updated = inboundOrderRepository.save(entity);
        return toDTO(updated);
    }

    /**
     * 拒绝入库 - 状态变更为 REJECTED，库存不变
     */
    public InboundOrderDTO reject(String inboundOrderCd) {
        InboundOrder entity = inboundOrderRepository.findByInboundOrderCd(inboundOrderCd)
                .orElseThrow(() -> new RuntimeException("入库单不存在：" + inboundOrderCd));

        // 检查状态是否为 RECEIVED
        if (!"RECEIVED".equals(entity.getStatus())) {
            throw new RuntimeException("只有 RECEIVED 状态的入库单才能拒绝");
        }

        // 更新状态
        entity.setStatus("REJECTED");
        entity.setUpdatedTs(Instant.now());
        entity.setUpdatedUserCd("system");

        InboundOrder updated = inboundOrderRepository.save(entity);
        return toDTO(updated);
    }

    private InboundOrderDTO toDTO(InboundOrder entity) {
        InboundOrderDTO dto = new InboundOrderDTO();
        dto.setInboundOrderCd(entity.getInboundOrderCd());
        dto.setCompanyCd(entity.getCompanyCd());
        dto.setProductCd(entity.getProductCd());
        dto.setQuantity(entity.getQuantity());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
