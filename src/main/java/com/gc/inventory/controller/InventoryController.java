package com.gc.inventory.controller;

import com.gc.inventory.dto.InventoryDTO;
import com.gc.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * VP-047: 分页查询库存列表
     * @param page 页码
     * @param size 每页大小
     * @return 库存列表
     */
    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<InventoryDTO> list = inventoryService.getList(page, size);
        return ResponseEntity.ok(list);
    }

    /**
     * VP-048/049: 按公司 + 商品查询库存
     * @param companyCd 公司代码
     * @param productCd 商品代码
     * @return 库存详情，不存在返回 404
     */
    @GetMapping("/{companyCd}/{productCd}")
    public ResponseEntity<InventoryDTO> getInventory(
            @PathVariable String companyCd,
            @PathVariable String productCd) {
        InventoryDTO dto = inventoryService.getInventory(companyCd, productCd);
        
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * VP-050/051: 库存调整
     * @param companyCd 公司代码
     * @param productCd 商品代码
     * @param dto 调整请求（包含 quantity 和 adjustmentReason）
     * @return 调整后的库存
     */
    @PutMapping("/{companyCd}/{productCd}/adjust")
    public ResponseEntity<InventoryDTO> adjustInventory(
            @PathVariable String companyCd,
            @PathVariable String productCd,
            @RequestBody InventoryDTO dto) {
        try {
            InventoryDTO updated = inventoryService.adjustInventory(companyCd, productCd, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
