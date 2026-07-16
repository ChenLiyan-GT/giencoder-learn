package com.gc.picking.controller;

import com.gc.picking.dto.PickingOrderDTO;
import com.gc.picking.service.PickingOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/picking-orders")
public class PickingOrderController {

    private final PickingOrderService pickingOrderService;

    public PickingOrderController(PickingOrderService pickingOrderService) {
        this.pickingOrderService = pickingOrderService;
    }

    /**
     * 创建拣货单
     * POST /api/picking-orders
     */
    @PostMapping
    public ResponseEntity<PickingOrderDTO> create(@RequestBody PickingOrderDTO dto) {
        PickingOrderDTO result = pickingOrderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * 按拣货单号查询
     * GET /api/picking-orders/{pickingOrderNo}
     */
    @GetMapping("/{pickingOrderNo}")
    public ResponseEntity<PickingOrderDTO> getByPickingOrderNo(@PathVariable String pickingOrderNo) {
        PickingOrderDTO result = pickingOrderService.getByPickingOrderNo(pickingOrderNo);
        return ResponseEntity.ok(result);
    }

    /**
     * 执行拣货
     * PUT /api/picking-orders/{pickingOrderNo}/pick
     */
    @PutMapping("/{pickingOrderNo}/pick")
    public ResponseEntity<PickingOrderDTO> pick(@PathVariable String pickingOrderNo,
                                                @RequestBody PickingOrderDTO dto) {
        PickingOrderDTO result = pickingOrderService.pick(pickingOrderNo, dto.getPickedQuantity());
        return ResponseEntity.ok(result);
    }

    /**
     * 完成拣货
     * PUT /api/picking-orders/{pickingOrderNo}/complete
     */
    @PutMapping("/{pickingOrderNo}/complete")
    public ResponseEntity<PickingOrderDTO> complete(@PathVariable String pickingOrderNo) {
        PickingOrderDTO result = pickingOrderService.complete(pickingOrderNo);
        return ResponseEntity.ok(result);
    }

    /**
     * 取消拣货
     * PUT /api/picking-orders/{pickingOrderNo}/cancel
     */
    @PutMapping("/{pickingOrderNo}/cancel")
    public ResponseEntity<PickingOrderDTO> cancel(@PathVariable String pickingOrderNo) {
        PickingOrderDTO result = pickingOrderService.cancel(pickingOrderNo);
        return ResponseEntity.ok(result);
    }
}
