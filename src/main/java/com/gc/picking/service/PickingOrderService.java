package com.gc.picking.service;

import com.common.entity.Inventory;
import com.common.entity.OutboundOrder;
import com.common.entity.PickingOrder;
import com.gc.inventory.repository.InventoryRepository;
import com.gc.outbound.repository.OutboundOrderRepository;
import com.gc.picking.dto.PickingOrderDTO;
import com.gc.picking.repository.PickingOrderRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@Transactional
public class PickingOrderService {

    private final PickingOrderRepository pickingOrderRepository;
    private final OutboundOrderRepository outboundOrderRepository;
    private final InventoryRepository inventoryRepository;

    public PickingOrderService(PickingOrderRepository pickingOrderRepository,
                               OutboundOrderRepository outboundOrderRepository,
                               InventoryRepository inventoryRepository) {
        this.pickingOrderRepository = pickingOrderRepository;
        this.outboundOrderRepository = outboundOrderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * 创建拣货单
     */
    public PickingOrderDTO create(PickingOrderDTO dto) {
        // 检查拣货单号是否重复
        if (pickingOrderRepository.existsByPickingOrderNo(dto.getPickingOrderNo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "拣货单号已存在");
        }

        // 校验出库单是否存在
        OutboundOrder outboundOrder = outboundOrderRepository.findByOutboundOrderCd(dto.getOutboundOrderNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "出库单不存在"));

        // 创建拣货单
        PickingOrder pickingOrder = new PickingOrder();
        BeanUtils.copyProperties(dto, pickingOrder);
        pickingOrder.setPickedQuantity(0);
        pickingOrder.setStatus("PENDING");
        pickingOrder.setCreatedUserCd("TEST_USER");
        pickingOrder.setUpdatedUserCd("TEST_USER");
        pickingOrder.setCreatedProgram("PickingOrderService");
        pickingOrder.setUpdatedProgram("PickingOrderService");
        pickingOrder.setCreatedTs(Instant.now());
        pickingOrder.setUpdatedTs(Instant.now());
        pickingOrder.setDeletedFlag("0");

        PickingOrder saved = pickingOrderRepository.save(pickingOrder);

        PickingOrderDTO result = new PickingOrderDTO();
        BeanUtils.copyProperties(saved, result);
        return result;
    }

    /**
     * 按拣货单号查询
     */
    @Transactional(readOnly = true)
    public PickingOrderDTO getByPickingOrderNo(String pickingOrderNo) {
        PickingOrder pickingOrder = pickingOrderRepository.findByPickingOrderNo(pickingOrderNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "拣货单不存在"));

        PickingOrderDTO dto = new PickingOrderDTO();
        BeanUtils.copyProperties(pickingOrder, dto);
        return dto;
    }

    /**
     * 执行拣货
     */
    public PickingOrderDTO pick(String pickingOrderNo, Integer pickedQuantity) {
        PickingOrder pickingOrder = pickingOrderRepository.findByPickingOrderNo(pickingOrderNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "拣货单不存在"));

        // 状态校验：只有 PENDING 或 PICKING 可以执行拣货
        if (!"PENDING".equals(pickingOrder.getStatus()) && !"PICKING".equals(pickingOrder.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前状态不可执行拣货");
        }

        // 校验库存是否充足
        Inventory inventory = inventoryRepository.findByCompanyCdAndProductCd("C001", pickingOrder.getProductCd())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "库存不存在"));

        int availableQuantity = inventory.getQuantity() - inventory.getReservedQuantity();
        if (availableQuantity < pickedQuantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "库存不足");
        }

        // 累加 picked_quantity
        pickingOrder.setPickedQuantity(pickingOrder.getPickedQuantity() + pickedQuantity);
        pickingOrder.setStatus("PICKING");
        pickingOrder.setUpdatedUserCd("TEST_USER");
        pickingOrder.setUpdatedProgram("PickingOrderService");
        pickingOrder.setUpdatedTs(Instant.now());

        PickingOrder saved = pickingOrderRepository.save(pickingOrder);

        PickingOrderDTO dto = new PickingOrderDTO();
        BeanUtils.copyProperties(saved, dto);
        return dto;
    }

    /**
     * 完成拣货
     */
    public PickingOrderDTO complete(String pickingOrderNo) {
        PickingOrder pickingOrder = pickingOrderRepository.findByPickingOrderNo(pickingOrderNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "拣货单不存在"));

        // 状态校验：只有 PICKING 可以完成
        if (!"PICKING".equals(pickingOrder.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前状态不可完成拣货");
        }

        // 校验 picked_quantity >= quantity
        if (pickingOrder.getPickedQuantity() < pickingOrder.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "拣货数量不足");
        }

        // 更新状态为 COMPLETED
        pickingOrder.setStatus("COMPLETED");
        pickingOrder.setUpdatedUserCd("TEST_USER");
        pickingOrder.setUpdatedProgram("PickingOrderService");
        pickingOrder.setUpdatedTs(Instant.now());

        PickingOrder saved = pickingOrderRepository.save(pickingOrder);

        // 更新关联的出库单状态为 PICKED
        OutboundOrder outboundOrder = outboundOrderRepository.findByOutboundOrderCd(pickingOrder.getOutboundOrderNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "出库单不存在"));
        outboundOrder.setStatus("PICKED");
        outboundOrder.setUpdatedUserCd("TEST_USER");
        outboundOrder.setUpdatedProgram("PickingOrderService");
        outboundOrder.setUpdatedTs(Instant.now());
        outboundOrderRepository.save(outboundOrder);

        PickingOrderDTO dto = new PickingOrderDTO();
        BeanUtils.copyProperties(saved, dto);
        return dto;
    }

    /**
     * 取消拣货
     */
    public PickingOrderDTO cancel(String pickingOrderNo) {
        PickingOrder pickingOrder = pickingOrderRepository.findByPickingOrderNo(pickingOrderNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "拣货单不存在"));

        // 状态校验：只有 PENDING 或 PICKING 可以取消
        if (!"PENDING".equals(pickingOrder.getStatus()) && !"PICKING".equals(pickingOrder.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前状态不可取消");
        }

        // 更新状态为 CANCELLED
        pickingOrder.setStatus("CANCELLED");
        pickingOrder.setUpdatedUserCd("TEST_USER");
        pickingOrder.setUpdatedProgram("PickingOrderService");
        pickingOrder.setUpdatedTs(Instant.now());

        PickingOrder saved = pickingOrderRepository.save(pickingOrder);

        PickingOrderDTO dto = new PickingOrderDTO();
        BeanUtils.copyProperties(saved, dto);
        return dto;
    }
}
