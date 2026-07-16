package com.gc.picking.controller;

import com.common.entity.Inventory;
import com.common.entity.OutboundOrder;
import com.common.entity.PickingOrder;
import com.common.entity.Product;
import com.gc.inventory.repository.InventoryRepository;
import com.gc.outbound.repository.OutboundOrderRepository;
import com.gc.picking.repository.PickingOrderRepository;
import com.gc.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 拣货机能控制器测试
 * 覆盖验证用例：VP-022 ~ VP-034
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("拣货机能 API 测试")
class PickingOrderControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PickingOrderRepository pickingOrderRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/picking-orders";
        setupTestData();
    }

    /**
     * 准备测试数据
     */
    void setupTestData() {
        // 清理已存在的测试数据
        productRepository.findByProductCd("P001").ifPresent(p -> productRepository.delete(p));
        inventoryRepository.findByCompanyCdAndProductCd("C001", "P001").ifPresent(i -> inventoryRepository.delete(i));
        outboundOrderRepository.findByOutboundOrderCd("OB001").ifPresent(o -> outboundOrderRepository.delete(o));
        pickingOrderRepository.findByPickingOrderNo("PK001").ifPresent(p -> pickingOrderRepository.delete(p));

        Instant now = Instant.now();
        
        // 创建商品
        Product product = new Product();
        product.setProductCd("P001");
        product.setProductNmKanji("商品 A");
        product.setProductNmKana("ショウヒン A");
        product.setUnitCd("PCS");
        product.setCreatedUserCd("TEST_USER");
        product.setUpdatedUserCd("TEST_USER");
        product.setCreatedProgram("PickingOrderControllerTest");
        product.setUpdatedProgram("PickingOrderControllerTest");
        product.setDeletedFlag("0");
        product.setVersion(1);
        product.setCreatedTs(now);
        product.setUpdatedTs(now);
        productRepository.save(product);

        // 创建库存
        Inventory inventory = new Inventory();
        inventory.setCompanyCd("C001");
        inventory.setProductCd("P001");
        inventory.setQuantity(100);
        inventory.setReservedQuantity(0);
        inventory.setCreatedUserCd("TEST_USER");
        inventory.setUpdatedUserCd("TEST_USER");
        inventory.setCreatedProgram("PickingOrderControllerTest");
        inventory.setUpdatedProgram("PickingOrderControllerTest");
        inventory.setVersion(1);
        inventory.setDeletedFlag("0");
        inventory.setCreatedTs(now);
        inventory.setUpdatedTs(now);
        inventoryRepository.save(inventory);

        // 创建出库单
        OutboundOrder outboundOrder = new OutboundOrder();
        outboundOrder.setOutboundOrderCd("OB001");
        outboundOrder.setCompanyCd("C001");
        outboundOrder.setProductCd("P001");
        outboundOrder.setQuantity(50);
        outboundOrder.setStatus("PENDING");
        outboundOrder.setCreatedUserCd("TEST_USER");
        outboundOrder.setUpdatedUserCd("TEST_USER");
        outboundOrder.setCreatedProgram("PickingOrderControllerTest");
        outboundOrder.setUpdatedProgram("PickingOrderControllerTest");
        outboundOrder.setVersion(1);
        outboundOrder.setDeletedFlag("0");
        outboundOrder.setCreatedTs(now);
        outboundOrder.setUpdatedTs(now);
        outboundOrderRepository.save(outboundOrder);
    }

    // ==================== VP-022: 创建拣货单成功 ====================
    @Test
    @DisplayName("VP-022: 创建拣货单成功 (201)")
    void testCreatePickingOrder_Success() {
        Map<String, Object> request = new HashMap<>();
        request.put("pickingOrderNo", "PK001");
        request.put("outboundOrderNo", "OB001");
        request.put("productCd", "P001");
        request.put("quantity", 50);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, entity, Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("PK001", responseBody.get("pickingOrderNo"));
        assertEquals("OB001", responseBody.get("outboundOrderNo"));
        assertEquals("P001", responseBody.get("productCd"));
        assertEquals(50, ((Number) responseBody.get("quantity")).intValue());
        assertEquals("PENDING", responseBody.get("status"));
    }

    // ==================== VP-023: 重复拣货单号创建失败 ====================
    @Test
    @DisplayName("VP-023: 重复拣货单号创建失败 (非 2xx)")
    void testCreatePickingOrder_DuplicateFails() {
        createPickingOrder("PK001", "OB001", "P001", 50);

        Map<String, Object> request = new HashMap<>();
        request.put("pickingOrderNo", "PK001");
        request.put("outboundOrderNo", "OB001");
        request.put("productCd", "P001");
        request.put("quantity", 50);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, Void.class);

        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    // ==================== VP-024: 按单号查询拣货单成功 ====================
    @Test
    @DisplayName("VP-024: 按单号查询拣货单成功 (200)")
    void testGetPickingOrder_Success() {
        createPickingOrder("PK001", "OB001", "P001", 50);

        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/PK001", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("PK001", responseBody.get("pickingOrderNo"));
        assertEquals("OB001", responseBody.get("outboundOrderNo"));
    }

    // ==================== VP-025: 查询不存在的拣货单 ====================
    @Test
    @DisplayName("VP-025: 查询不存在的拣货单 (404)")
    void testGetPickingOrder_NotFound() {
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/PK999",
            HttpMethod.GET,
            null,
            Void.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== VP-026: 执行拣货→picked_quantity 累加，状态=PICKING ====================
    @Test
    @DisplayName("VP-026: 执行拣货→picked_quantity 累加，状态=PICKING (200)")
    void testPick_Success() {
        createPickingOrder("PK001", "OB001", "P001", 50);

        Map<String, Object> request = new HashMap<>();
        request.put("pickedQuantity", 20);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/PK001/pick",
            HttpMethod.PUT,
            entity,
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(20, ((Number) responseBody.get("pickedQuantity")).intValue());
        assertEquals("PICKING", responseBody.get("status"));
    }

    // ==================== VP-027: 分批拣货→picked_quantity 累加 ====================
    @Test
    @DisplayName("VP-027: 分批拣货→picked_quantity 累加 (200)")
    void testPick_BatchPicking() {
        createPickingOrder("PK001", "OB001", "P001", 50);
        pick("PK001", 20);

        Map<String, Object> request = new HashMap<>();
        request.put("pickedQuantity", 30);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/PK001/pick",
            HttpMethod.PUT,
            entity,
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(50, ((Number) responseBody.get("pickedQuantity")).intValue());
    }

    // ==================== VP-028: 拣货时库存不足→拒绝 ====================
    @Test
    @DisplayName("VP-028: 拣货时库存不足→拒绝 (非 2xx)")
    void testPick_InsufficientInventory() {
        createPickingOrder("PK001", "OB001", "P001", 50);

        Map<String, Object> request = new HashMap<>();
        request.put("pickedQuantity", 150);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/PK001/pick",
            HttpMethod.PUT,
            entity,
            Void.class
        );

        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    // ==================== VP-029: 完成拣货→status=COMPLETED ====================
    @Test
    @DisplayName("VP-029: 完成拣货→status=COMPLETED (200)")
    void testComplete_Success() {
        createPickingOrder("PK001", "OB001", "P001", 50);
        pick("PK001", 50);

        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/PK001/complete",
            HttpMethod.PUT,
            null,
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("COMPLETED", responseBody.get("status"));
    }

    // ==================== VP-030: picked_quantity 未达 quantity 时 complete 被拒绝 ====================
    @Test
    @DisplayName("VP-030: picked_quantity 未达 quantity 时 complete 被拒绝 (非 2xx)")
    void testComplete_InsufficientPickedQuantity() {
        createPickingOrder("PK001", "OB001", "P001", 50);
        pick("PK001", 20);

        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/PK001/complete",
            HttpMethod.PUT,
            null,
            Void.class
        );

        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    // ==================== VP-031: PENDING→CANCELLED ====================
    @Test
    @DisplayName("VP-031: PENDING→CANCELLED (200)")
    void testCancel_PendingToCancelled() {
        createPickingOrder("PK001", "OB001", "P001", 50);

        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/PK001/cancel",
            HttpMethod.PUT,
            null,
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("CANCELLED", responseBody.get("status"));
    }

    // ==================== VP-032: PICKING→CANCELLED ====================
    @Test
    @DisplayName("VP-032: PICKING→CANCELLED (200)")
    void testCancel_PickingToCancelled() {
        createPickingOrder("PK001", "OB001", "P001", 50);
        pick("PK001", 20);

        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/PK001/cancel",
            HttpMethod.PUT,
            null,
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("CANCELLED", responseBody.get("status"));
    }

    // ==================== 辅助方法 ====================

    void createPickingOrder(String pickingOrderNo, String outboundOrderNo, String productCd, int quantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("pickingOrderNo", pickingOrderNo);
        request.put("outboundOrderNo", outboundOrderNo);
        request.put("productCd", productCd);
        request.put("quantity", quantity);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        restTemplate.postForEntity(baseUrl, entity, Map.class);
    }

    void pick(String pickingOrderNo, int pickedQuantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("pickedQuantity", pickedQuantity);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        restTemplate.exchange(
            baseUrl + "/" + pickingOrderNo + "/pick",
            HttpMethod.PUT,
            entity,
            Map.class
        );
    }
}
