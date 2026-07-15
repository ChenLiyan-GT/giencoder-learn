package com.gc.inbound.controller;

import com.gc.inbound.dto.InboundOrderDTO;
import com.gc.inbound.service.InboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inbound-orders")
public class InboundOrderController {

    @Autowired
    private InboundOrderService inboundOrderService;

    @PostMapping
    public ResponseEntity<InboundOrderDTO> create(@RequestBody InboundOrderDTO request) {
        InboundOrderDTO response = inboundOrderService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{inboundOrderCd}")
    public ResponseEntity<InboundOrderDTO> get(@PathVariable String inboundOrderCd) {
        try {
            InboundOrderDTO response = inboundOrderService.get(inboundOrderCd);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            throw e;
        }
    }
}
