package com.gc.outbound.controller;

import com.gc.outbound.dto.OutboundOrderDTO;
import com.gc.outbound.service.OutboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/outbound-orders")
public class OutboundOrderController {

    @Autowired
    private OutboundOrderService outboundOrderService;

    @PostMapping
    public ResponseEntity<OutboundOrderDTO> create(@RequestBody OutboundOrderDTO dto) {
        OutboundOrderDTO created = outboundOrderService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{outboundOrderCd}")
    public ResponseEntity<OutboundOrderDTO> get(@PathVariable String outboundOrderCd) {
        OutboundOrderDTO dto = outboundOrderService.get(outboundOrderCd);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
