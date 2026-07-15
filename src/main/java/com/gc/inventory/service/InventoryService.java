package com.gc.inventory.service;

import com.common.entity.Inventory;
import com.gc.inventory.dto.InventoryDTO;
import com.gc.inventory.repository.InventoryRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * 分页查询库存列表
     * @param page 页码
     * @param size 每页大小
     * @return 库存 DTO 列表
     */
    @Transactional(readOnly = true)
    public List<InventoryDTO> getList(int page, int size) {
        List<Inventory> inventories = inventoryRepository.findAll()
                .stream()
                .filter(inv -> "0".equals(inv.getDeletedFlag()))
                .collect(Collectors.toList());
        
        int start = page * size;
        int end = Math.min(start + size, inventories.size());
        
        if (start >= inventories.size()) {
            return List.of();
        }
        
        return inventories.subList(start, end)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 按公司 + 商品查询库存
     * @param companyCd 公司代码
     * @param productCd 商品代码
     * @return 库存 DTO，不存在则返回 null
     */
    @Transactional(readOnly = true)
    public InventoryDTO getInventory(String companyCd, String productCd) {
        Optional<Inventory> inventoryOptional = inventoryRepository.findByCompanyCdAndProductCdAndDeletedFlag(companyCd, productCd, "0");
        
        if (inventoryOptional.isPresent()) {
            Inventory inventory = inventoryOptional.get();
            return convertToDTO(inventory);
        }
        
        return null;
    }

    /**
     * 库存调整
     * @param companyCd 公司代码
     * @param productCd 商品代码
     * @param dto 调整请求 DTO（包含 quantity 和 adjustmentReason）
     * @return 调整后的库存 DTO
     */
    public InventoryDTO adjustInventory(String companyCd, String productCd, InventoryDTO dto) {
        // 验证数量不能为负
        if (dto.getQuantity() != null && dto.getQuantity() < 0) {
            throw new IllegalArgumentException("库存数量不能为负数");
        }
        
        Optional<Inventory> inventoryOptional = inventoryRepository.findByCompanyCdAndProductCdAndDeletedFlag(companyCd, productCd, "0");
        if (inventoryOptional.isEmpty()) {
            throw new IllegalStateException("库存记录不存在");
        }
        
        Inventory inventory = inventoryOptional.get();
        
        // 更新库存数量
        if (dto.getQuantity() != null) {
            inventory.setQuantity(dto.getQuantity());
        }
        
        // 更新审计信息
        inventory.setUpdatedTs(Instant.now());
        inventory.setUpdatedUserCd(dto.getAdjustmentReason() != null ? dto.getAdjustmentReason() : "system");
        
        Inventory updated = inventoryRepository.save(inventory);
        return convertToDTO(updated);
    }

    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        BeanUtils.copyProperties(inventory, dto);
        return dto;
    }
}
