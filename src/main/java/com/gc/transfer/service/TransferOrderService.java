package com.gc.transfer.service;

import com.common.entity.Inventory;
import com.common.entity.TransferOrder;
import com.gc.inventory.repository.InventoryRepository;
import com.gc.transfer.dto.TransferOrderDTO;
import com.gc.transfer.repository.TransferOrderRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class TransferOrderService {

    private final TransferOrderRepository transferOrderRepository;
    private final InventoryRepository inventoryRepository;

    public TransferOrderService(TransferOrderRepository transferOrderRepository, 
                                InventoryRepository inventoryRepository) {
        this.transferOrderRepository = transferOrderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public TransferOrderDTO create(TransferOrderDTO dto) {
        // 验证调出方 != 调入方
        if (dto.getFromCompanyCd().equals(dto.getToCompanyCd())) {
            throw new IllegalArgumentException("调出方和调入方不能相同");
        }

        // 验证单号不重复
        if (transferOrderRepository.existsByTransferOrderNo(dto.getTransferOrderNo())) {
            throw new IllegalArgumentException("调拨单号已存在");
        }

        // 验证调出方库存充足
        Inventory fromInventory = inventoryRepository.findByCompanyCdAndProductCd(
            dto.getFromCompanyCd(), dto.getProductCd())
            .orElseThrow(() -> new IllegalArgumentException("调出方库存不存在"));

        if (fromInventory.getQuantity() < dto.getQuantity()) {
            throw new IllegalArgumentException("调出方库存不足");
        }

        // 创建调拨单
        TransferOrder order = new TransferOrder();
        order.setTransferOrderNo(dto.getTransferOrderNo());
        order.setFromCompanyCd(dto.getFromCompanyCd());
        order.setToCompanyCd(dto.getToCompanyCd());
        order.setProductCd(dto.getProductCd());
        order.setQuantity(dto.getQuantity());
        order.setTransferredQuantity(0);
        order.setStatus("PENDING");
        order.setDeletedFlag("0");
        order.setCreatedUserCd("test_user");
        order.setCreatedTs(Instant.now());
        order.setUpdatedUserCd("test_user");
        order.setUpdatedTs(Instant.now());
        order.setVersion(0);

        TransferOrder saved = transferOrderRepository.save(order);
        return toDTO(saved);
    }

    public TransferOrderDTO getByTransferOrderNo(String transferOrderNo) {
        TransferOrder order = transferOrderRepository.findByTransferOrderNo(transferOrderNo)
            .orElseThrow(() -> new RuntimeException("调拨单不存在"));
        return toDTO(order);
    }

    public TransferOrderDTO approve(String transferOrderNo) {
        TransferOrder order = transferOrderRepository.findByTransferOrderNo(transferOrderNo)
            .orElseThrow(() -> new RuntimeException("调拨单不存在"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalArgumentException("只有 PENDING 状态的调拨单可以审批");
        }

        // 预占调出方库存
        Inventory fromInventory = inventoryRepository.findByCompanyCdAndProductCd(
            order.getFromCompanyCd(), order.getProductCd())
            .orElseThrow(() -> new RuntimeException("调出方库存不存在"));

        fromInventory.setReservedQuantity(fromInventory.getReservedQuantity() + order.getQuantity());
        inventoryRepository.save(fromInventory);

        order.setStatus("APPROVED");
        order.setUpdatedUserCd("test_user");
        order.setUpdatedTs(Instant.now());

        return toDTO(transferOrderRepository.save(order));
    }

    public TransferOrderDTO ship(String transferOrderNo) {
        TransferOrder order = transferOrderRepository.findByTransferOrderNo(transferOrderNo)
            .orElseThrow(() -> new RuntimeException("调拨单不存在"));

        if (!"APPROVED".equals(order.getStatus())) {
            throw new IllegalArgumentException("只有 APPROVED 状态的调拨单可以出库");
        }

        // 扣减调出方库存（数量 - 预占）
        Inventory fromInventory = inventoryRepository.findByCompanyCdAndProductCd(
            order.getFromCompanyCd(), order.getProductCd())
            .orElseThrow(() -> new RuntimeException("调出方库存不存在"));

        fromInventory.setQuantity(fromInventory.getQuantity() - order.getQuantity());
        fromInventory.setReservedQuantity(fromInventory.getReservedQuantity() - order.getQuantity());
        inventoryRepository.save(fromInventory);

        order.setStatus("SHIPPED");
        order.setUpdatedUserCd("test_user");
        order.setUpdatedTs(Instant.now());

        return toDTO(transferOrderRepository.save(order));
    }

    public TransferOrderDTO receive(String transferOrderNo) {
        TransferOrder order = transferOrderRepository.findByTransferOrderNo(transferOrderNo)
            .orElseThrow(() -> new RuntimeException("调拨单不存在"));

        if (!"SHIPPED".equals(order.getStatus())) {
            throw new IllegalArgumentException("只有 SHIPPED 状态的调拨单可以入库");
        }

        // 增加调入方库存
        Inventory toInventory = inventoryRepository.findByCompanyCdAndProductCd(
            order.getToCompanyCd(), order.getProductCd())
            .orElseThrow(() -> new RuntimeException("调入方库存不存在"));

        toInventory.setQuantity(toInventory.getQuantity() + order.getQuantity());
        inventoryRepository.save(toInventory);

        order.setStatus("COMPLETED");
        order.setTransferredQuantity(order.getQuantity());
        order.setUpdatedUserCd("test_user");
        order.setUpdatedTs(Instant.now());

        return toDTO(transferOrderRepository.save(order));
    }

    public TransferOrderDTO cancel(String transferOrderNo) {
        TransferOrder order = transferOrderRepository.findByTransferOrderNo(transferOrderNo)
            .orElseThrow(() -> new RuntimeException("调拨单不存在"));

        if (!"PENDING".equals(order.getStatus()) && !"APPROVED".equals(order.getStatus())) {
            throw new IllegalArgumentException("只有 PENDING 或 APPROVED 状态的调拨单可以取消");
        }

        // 如果是 APPROVED 状态，需要恢复预占
        if ("APPROVED".equals(order.getStatus())) {
            Inventory fromInventory = inventoryRepository.findByCompanyCdAndProductCd(
                order.getFromCompanyCd(), order.getProductCd())
                .orElseThrow(() -> new RuntimeException("调出方库存不存在"));

            fromInventory.setReservedQuantity(fromInventory.getReservedQuantity() - order.getQuantity());
            inventoryRepository.save(fromInventory);
        }

        order.setStatus("CANCELLED");
        order.setUpdatedUserCd("test_user");
        order.setUpdatedTs(Instant.now());

        return toDTO(transferOrderRepository.save(order));
    }

    private TransferOrderDTO toDTO(TransferOrder order) {
        TransferOrderDTO dto = new TransferOrderDTO();
        BeanUtils.copyProperties(order, dto);
        return dto;
    }
}
