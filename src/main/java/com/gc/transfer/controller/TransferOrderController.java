package com.gc.transfer.controller;

import com.gc.transfer.dto.TransferOrderDTO;
import com.gc.transfer.service.TransferOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfer-orders")
public class TransferOrderController {

    private final TransferOrderService transferOrderService;

    public TransferOrderController(TransferOrderService transferOrderService) {
        this.transferOrderService = transferOrderService;
    }

    @PostMapping
    public ResponseEntity<TransferOrderDTO> create(@RequestBody TransferOrderDTO dto) {
        TransferOrderDTO created = transferOrderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{transferOrderNo}")
    public ResponseEntity<TransferOrderDTO> get(@PathVariable String transferOrderNo) {
        TransferOrderDTO dto = transferOrderService.getByTransferOrderNo(transferOrderNo);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{transferOrderNo}/approve")
    public ResponseEntity<TransferOrderDTO> approve(@PathVariable String transferOrderNo) {
        TransferOrderDTO dto = transferOrderService.approve(transferOrderNo);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{transferOrderNo}/ship")
    public ResponseEntity<TransferOrderDTO> ship(@PathVariable String transferOrderNo) {
        TransferOrderDTO dto = transferOrderService.ship(transferOrderNo);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{transferOrderNo}/receive")
    public ResponseEntity<TransferOrderDTO> receive(@PathVariable String transferOrderNo) {
        TransferOrderDTO dto = transferOrderService.receive(transferOrderNo);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{transferOrderNo}/cancel")
    public ResponseEntity<TransferOrderDTO> cancel(@PathVariable String transferOrderNo) {
        TransferOrderDTO dto = transferOrderService.cancel(transferOrderNo);
        return ResponseEntity.ok(dto);
    }
}
