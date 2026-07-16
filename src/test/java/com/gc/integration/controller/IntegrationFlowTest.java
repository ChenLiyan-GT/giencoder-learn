package com.gc.integration.controller;

import com.common.entity.Product;
import com.gc.inbound.dto.InboundOrderDTO;
import com.gc.inbound.repository.InboundOrderRepository;
import com.gc.inventory.dto.InventoryDTO;
import com.gc.inventory.repository.InventoryRepository;
import com.gc.outbound.dto.OutboundOrderDTO;
import com.gc.outbound.repository.OutboundOrderRepository;
import com.gc.picking.dto.PickingOrderDTO;
import com.gc.picking.repository.PickingOrderRepository;
import com.gc.product.repository.ProductRepository;
import com.gc.transfer.dto.TransferOrderDTO;
import com.gc.transfer.repository.TransferOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationFlowTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private PickingOrderRepository pickingOrderRepository;

    @Autowired
    private TransferOrderRepository transferOrderRepository;

    private String baseUrl;
    private String inboundBaseUrl;
    private String outboundBaseUrl;
    private String pickingBaseUrl;
    private String transferBaseUrl;
    private String inventoryBaseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        inboundBaseUrl = baseUrl + "/inbound-orders";
        outboundBaseUrl = baseUrl + "/outbound-orders";
        pickingBaseUrl = baseUrl + "/picking-orders";
        transferBaseUrl = baseUrl + "/transfer-orders";
        inventoryBaseUrl = baseUrl + "/inventory";
        resetTestData();
    }

    private void resetTestData() {
        // 清理所有测试数据
        inboundOrderRepository.findByInboundOrderCd("IO_INT_001").ifPresent(i -> inboundOrderRepository.delete(i));
        outboundOrderRepository.findByOutboundOrderCd("OO_INT_001").ifPresent(o -> outboundOrderRepository.delete(o));
        pickingOrderRepository.findByPickingOrderNo("PK_INT_001").ifPresent(p -> pickingOrderRepository.delete(p));
        transferOrderRepository.findByTransferOrderNo("TR_INT_001").ifPresent(t -> transferOrderRepository.delete(t));
        
        // 清理库存和商品
        inventoryRepository.findByCompanyCdAndProductCdAndDeletedFlag("C001", "P_INT_001", "0").ifPresent(i -> inventoryRepository.delete(i));
        inventoryRepository.findByCompanyCdAndProductCdAndDeletedFlag("C002", "P_INT_001", "0").ifPresent(i -> inventoryRepository.delete(i));
        productRepository.findByProductCd("P_INT_001").ifPresent(p -> productRepository.delete(p));
        
        // 创建测试商品
        Instant now = Instant.now();
        Product product = new Product();
        product.setProductCd("P_INT_001");
        product.setProductNmKanji("集成测试商品");
        product.setProductNmKana("セイカイテストショウヒン");
        product.setUnitCd("PCS");
        product.setVersion(0);
        product.setDeletedFlag("0");
        product.setCreatedUserCd("mock_user");
        product.setCreatedTs(now);
        product.setUpdatedUserCd("mock_user");
        product.setUpdatedTs(now);
        productRepository.save(product);
        
        // 创建 C001 和 C002 的库存记录（用于调拨测试）
        com.common.entity.Inventory inventory1 = new com.common.entity.Inventory();
        inventory1.setCompanyCd("C001");
        inventory1.setProductCd("P_INT_001");
        inventory1.setQuantity(0);
        inventory1.setReservedQuantity(0);
        inventory1.setDeletedFlag("0");
        inventory1.setCreatedUserCd("mock_user");
        inventory1.setCreatedTs(now);
        inventory1.setUpdatedUserCd("mock_user");
        inventory1.setUpdatedTs(now);
        inventory1.setVersion(0);
        inventoryRepository.save(inventory1);
        
        com.common.entity.Inventory inventory2 = new com.common.entity.Inventory();
        inventory2.setCompanyCd("C002");
        inventory2.setProductCd("P_INT_001");
        inventory2.setQuantity(0);
        inventory2.setReservedQuantity(0);
        inventory2.setDeletedFlag("0");
        inventory2.setCreatedUserCd("mock_user");
        inventory2.setCreatedTs(now);
        inventory2.setUpdatedUserCd("mock_user");
        inventory2.setUpdatedTs(now);
        inventory2.setVersion(0);
        inventoryRepository.save(inventory2);
    }

    @Test
    @DisplayName("VP-073: 入库→出库全流程集成测试")
    void testInboundToOutboundFullFlow() {
        // Step 1: 创建入库单
        InboundOrderDTO inboundRequest = new InboundOrderDTO();
        inboundRequest.setInboundOrderCd("IO_INT_001");
        inboundRequest.setCompanyCd("C001");
        inboundRequest.setProductCd("P_INT_001");
        inboundRequest.setQuantity(100);
        
        ResponseEntity<InboundOrderDTO> inboundResponse = restTemplate.postForEntity(inboundBaseUrl, inboundRequest, InboundOrderDTO.class);
        assertEquals(HttpStatus.CREATED, inboundResponse.getStatusCode());
        assertEquals("RECEIVED", inboundResponse.getBody().getStatus());

        // Step 2: 确认入库→库存增加
        ResponseEntity<InboundOrderDTO> confirmResponse = restTemplate.exchange(
            inboundBaseUrl + "/IO_INT_001/confirm",
            HttpMethod.PUT,
            null,
            InboundOrderDTO.class
        );
        assertEquals(HttpStatus.OK, confirmResponse.getStatusCode());
        assertEquals("CONFIRMED", confirmResponse.getBody().getStatus());

        // 验证库存：数量应该为 100
        ResponseEntity<InventoryDTO> inventoryResponse = restTemplate.getForEntity(
            inventoryBaseUrl + "/C001/P_INT_001",
            InventoryDTO.class
        );
        assertEquals(HttpStatus.OK, inventoryResponse.getStatusCode());
        assertEquals(100, inventoryResponse.getBody().getQuantity());
        assertEquals(0, inventoryResponse.getBody().getReservedQuantity());

        // Step 3: 创建出库单（预占库存）
        OutboundOrderDTO outboundRequest = new OutboundOrderDTO();
        outboundRequest.setOutboundOrderCd("OO_INT_001");
        outboundRequest.setCompanyCd("C001");
        outboundRequest.setProductCd("P_INT_001");
        outboundRequest.setQuantity(30);
        
        ResponseEntity<OutboundOrderDTO> outboundResponse = restTemplate.postForEntity(outboundBaseUrl, outboundRequest, OutboundOrderDTO.class);
        assertEquals(HttpStatus.CREATED, outboundResponse.getStatusCode());
        assertEquals("PENDING", outboundResponse.getBody().getStatus());

        // 验证库存：预占 30，可用库存 = 100 - 30 = 70
        ResponseEntity<InventoryDTO> inventoryAfterOutboundResponse = restTemplate.getForEntity(
            inventoryBaseUrl + "/C001/P_INT_001",
            InventoryDTO.class
        );
        assertEquals(HttpStatus.OK, inventoryAfterOutboundResponse.getStatusCode());
        assertEquals(100, inventoryAfterOutboundResponse.getBody().getQuantity());
        assertEquals(30, inventoryAfterOutboundResponse.getBody().getReservedQuantity());

        // Step 4: 发货出库→扣减库存（OutboundController.ship 使用 POST）
        ResponseEntity<OutboundOrderDTO> shipResponse = restTemplate.exchange(
            outboundBaseUrl + "/OO_INT_001/ship",
            HttpMethod.POST,
            null,
            OutboundOrderDTO.class
        );
        assertEquals(HttpStatus.OK, shipResponse.getStatusCode());
        assertEquals("SHIPPED", shipResponse.getBody().getStatus());

        // 验证库存：数量 = 100 - 30 = 70，预占 = 0
        ResponseEntity<InventoryDTO> inventoryAfterShipResponse = restTemplate.getForEntity(
            inventoryBaseUrl + "/C001/P_INT_001",
            InventoryDTO.class
        );
        assertEquals(HttpStatus.OK, inventoryAfterShipResponse.getStatusCode());
        assertEquals(70, inventoryAfterShipResponse.getBody().getQuantity());
        assertEquals(0, inventoryAfterShipResponse.getBody().getReservedQuantity());
    }

    @Test
    @DisplayName("VP-074: 拣货→出库状态联动测试")
    void testPickingToOutboundStatusLinkage() {
        // Step 1: 创建入库单并确认
        InboundOrderDTO inboundRequest = new InboundOrderDTO();
        inboundRequest.setInboundOrderCd("IO_INT_001");
        inboundRequest.setCompanyCd("C001");
        inboundRequest.setProductCd("P_INT_001");
        inboundRequest.setQuantity(100);
        restTemplate.postForEntity(inboundBaseUrl, inboundRequest, InboundOrderDTO.class);
        restTemplate.exchange(inboundBaseUrl + "/IO_INT_001/confirm", HttpMethod.PUT, null, InboundOrderDTO.class);

        // Step 2: 创建出库单
        OutboundOrderDTO outboundRequest = new OutboundOrderDTO();
        outboundRequest.setOutboundOrderCd("OO_INT_001");
        outboundRequest.setCompanyCd("C001");
        outboundRequest.setProductCd("P_INT_001");
        outboundRequest.setQuantity(50);
        ResponseEntity<OutboundOrderDTO> outboundResponse = restTemplate.postForEntity(outboundBaseUrl, outboundRequest, OutboundOrderDTO.class);
        assertEquals("PENDING", outboundResponse.getBody().getStatus());

        // Step 3: 创建拣货单
        PickingOrderDTO pickingRequest = new PickingOrderDTO();
        pickingRequest.setPickingOrderNo("PK_INT_001");
        pickingRequest.setOutboundOrderNo("OO_INT_001");
        pickingRequest.setProductCd("P_INT_001");
        pickingRequest.setQuantity(50);
        
        ResponseEntity<PickingOrderDTO> pickingResponse = restTemplate.postForEntity(pickingBaseUrl, pickingRequest, PickingOrderDTO.class);
        assertEquals(HttpStatus.CREATED, pickingResponse.getStatusCode());
        assertEquals("PENDING", pickingResponse.getBody().getStatus());

        // Step 4: 开始拣货（传递 pickedQuantity）
        PickingOrderDTO pickRequest = new PickingOrderDTO();
        pickRequest.setPickedQuantity(50);
        ResponseEntity<PickingOrderDTO> startPickResponse = restTemplate.exchange(
            pickingBaseUrl + "/PK_INT_001/pick",
            HttpMethod.PUT,
            new HttpEntity<>(pickRequest),
            PickingOrderDTO.class
        );
        assertEquals(HttpStatus.OK, startPickResponse.getStatusCode());
        assertEquals("PICKING", startPickResponse.getBody().getStatus());

        // Step 5: 完成拣货
        ResponseEntity<PickingOrderDTO> completePickResponse = restTemplate.exchange(
            pickingBaseUrl + "/PK_INT_001/complete",
            HttpMethod.PUT,
            null,
            PickingOrderDTO.class
        );
        assertEquals(HttpStatus.OK, completePickResponse.getStatusCode());
        assertEquals("COMPLETED", completePickResponse.getBody().getStatus());
        assertEquals(50, completePickResponse.getBody().getPickedQuantity());

        // Step 6: 验证出库单状态（拣货完成后出库单状态应为 PICKED）
        ResponseEntity<OutboundOrderDTO> outboundAfterPickingResponse = restTemplate.getForEntity(
            outboundBaseUrl + "/OO_INT_001",
            OutboundOrderDTO.class
        );
        assertEquals(HttpStatus.OK, outboundAfterPickingResponse.getStatusCode());
        assertEquals("PICKED", outboundAfterPickingResponse.getBody().getStatus());
    }

    @Test
    @DisplayName("VP-075: 调拨全流程测试（创建→审批→出库→入库）")
    void testTransferFullFlow() {
        // Step 1: 创建入库单并确认（C001 有库存）
        InboundOrderDTO inboundRequest = new InboundOrderDTO();
        inboundRequest.setInboundOrderCd("IO_INT_001");
        inboundRequest.setCompanyCd("C001");
        inboundRequest.setProductCd("P_INT_001");
        inboundRequest.setQuantity(100);
        restTemplate.postForEntity(inboundBaseUrl, inboundRequest, InboundOrderDTO.class);
        restTemplate.exchange(inboundBaseUrl + "/IO_INT_001/confirm", HttpMethod.PUT, null, InboundOrderDTO.class);

        // Step 2: 创建调拨单（从 C001 调拨到 C002）
        TransferOrderDTO transferRequest = new TransferOrderDTO();
        transferRequest.setTransferOrderNo("TR_INT_001");
        transferRequest.setFromCompanyCd("C001");
        transferRequest.setToCompanyCd("C002");
        transferRequest.setProductCd("P_INT_001");
        transferRequest.setQuantity(20);
        
        ResponseEntity<TransferOrderDTO> transferResponse = restTemplate.postForEntity(transferBaseUrl, transferRequest, TransferOrderDTO.class);
        assertEquals(HttpStatus.CREATED, transferResponse.getStatusCode());
        assertEquals("PENDING", transferResponse.getBody().getStatus());

        // Step 3: 审批调拨单
        ResponseEntity<TransferOrderDTO> approveResponse = restTemplate.exchange(
            transferBaseUrl + "/TR_INT_001/approve",
            HttpMethod.PUT,
            null,
            TransferOrderDTO.class
        );
        assertEquals(HttpStatus.OK, approveResponse.getStatusCode());
        assertEquals("APPROVED", approveResponse.getBody().getStatus());

        // Step 4: 调拨出库（TransferController.ship 使用 PUT）
        ResponseEntity<TransferOrderDTO> shipResponse = restTemplate.exchange(
            transferBaseUrl + "/TR_INT_001/ship",
            HttpMethod.PUT,
            null,
            TransferOrderDTO.class
        );
        assertEquals(HttpStatus.OK, shipResponse.getStatusCode());
        assertEquals("SHIPPED", shipResponse.getBody().getStatus());

        // Step 5: 调拨入库
        ResponseEntity<TransferOrderDTO> receiveResponse = restTemplate.exchange(
            transferBaseUrl + "/TR_INT_001/receive",
            HttpMethod.PUT,
            null,
            TransferOrderDTO.class
        );
        assertEquals(HttpStatus.OK, receiveResponse.getStatusCode());
        assertEquals("COMPLETED", receiveResponse.getBody().getStatus());
    }

    @Test
    @DisplayName("VP-076: 调拨公司代码一致性验证")
    void testTransferCompanyCodeConsistency() {
        // Step 1: 创建入库单并确认（C001 有库存）
        InboundOrderDTO inboundRequest = new InboundOrderDTO();
        inboundRequest.setInboundOrderCd("IO_INT_001");
        inboundRequest.setCompanyCd("C001");
        inboundRequest.setProductCd("P_INT_001");
        inboundRequest.setQuantity(100);
        restTemplate.postForEntity(inboundBaseUrl, inboundRequest, InboundOrderDTO.class);
        restTemplate.exchange(inboundBaseUrl + "/IO_INT_001/confirm", HttpMethod.PUT, null, InboundOrderDTO.class);

        // Step 2: 创建调拨单（C001 → C002）
        TransferOrderDTO transferRequest = new TransferOrderDTO();
        transferRequest.setTransferOrderNo("TR_INT_001");
        transferRequest.setFromCompanyCd("C001");
        transferRequest.setToCompanyCd("C002");
        transferRequest.setProductCd("P_INT_001");
        transferRequest.setQuantity(30);
        restTemplate.postForEntity(transferBaseUrl, transferRequest, TransferOrderDTO.class);

        // Step 3: 审批并出库（使用 PUT 方法）
        restTemplate.exchange(transferBaseUrl + "/TR_INT_001/approve", HttpMethod.PUT, null, TransferOrderDTO.class);
        restTemplate.exchange(transferBaseUrl + "/TR_INT_001/ship", HttpMethod.PUT, null, TransferOrderDTO.class);

        // Step 4: 验证 C001 库存减少
        ResponseEntity<InventoryDTO> fromInventoryResponse = restTemplate.getForEntity(
            inventoryBaseUrl + "/C001/P_INT_001",
            InventoryDTO.class
        );
        assertEquals(HttpStatus.OK, fromInventoryResponse.getStatusCode());
        assertEquals(70, fromInventoryResponse.getBody().getQuantity()); // 100 - 30 = 70

        // Step 5: 调拨入库
        restTemplate.exchange(transferBaseUrl + "/TR_INT_001/receive", HttpMethod.PUT, null, TransferOrderDTO.class);

        // Step 6: 验证 C002 库存增加
        ResponseEntity<InventoryDTO> toInventoryResponse = restTemplate.getForEntity(
            inventoryBaseUrl + "/C002/P_INT_001",
            InventoryDTO.class
        );
        assertEquals(HttpStatus.OK, toInventoryResponse.getStatusCode());
        assertEquals(30, toInventoryResponse.getBody().getQuantity());
    }

    @Test
    @DisplayName("VP-077: 库存不足时调拨创建失败")
    void testTransferShipFailsWhenInsufficientInventory() {
        // Step 1: 创建入库单并确认（只有 50 个库存）
        InboundOrderDTO inboundRequest = new InboundOrderDTO();
        inboundRequest.setInboundOrderCd("IO_INT_001");
        inboundRequest.setCompanyCd("C001");
        inboundRequest.setProductCd("P_INT_001");
        inboundRequest.setQuantity(50);
        restTemplate.postForEntity(inboundBaseUrl, inboundRequest, InboundOrderDTO.class);
        restTemplate.exchange(inboundBaseUrl + "/IO_INT_001/confirm", HttpMethod.PUT, null, InboundOrderDTO.class);

        // Step 2: 创建调拨单（尝试调拨 100 个，超过库存）- 应该失败
        TransferOrderDTO transferRequest = new TransferOrderDTO();
        transferRequest.setTransferOrderNo("TR_INT_001");
        transferRequest.setFromCompanyCd("C001");
        transferRequest.setToCompanyCd("C002");
        transferRequest.setProductCd("P_INT_001");
        transferRequest.setQuantity(100);
        
        // 创建调拨单时应该因库存不足而失败
        ResponseEntity<String> createResponse = restTemplate.postForEntity(transferBaseUrl, transferRequest, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, createResponse.getStatusCode());
    }
}
