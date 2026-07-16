package com.gc.transfer.controller;

import com.common.entity.Product;
import com.common.entity.Inventory;
import com.gc.product.repository.ProductRepository;
import com.gc.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
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
 * 仓库调拨机能 API 测试
 * 覆盖验证用例：VP-055 ~ VP-072
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransferOrderControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/transfer-orders";
        setupTestData();
    }

    private void setupTestData() {
        // 清理已存在的测试数据
        inventoryRepository.findByCompanyCdAndProductCd("C001", "P001").ifPresent(inventoryRepository::delete);
        inventoryRepository.findByCompanyCdAndProductCd("C002", "P001").ifPresent(inventoryRepository::delete);
        productRepository.findByProductCd("P001").ifPresent(productRepository::delete);
        productRepository.findByProductCd("P002").ifPresent(productRepository::delete);

        // 创建商品
        Product product = new Product();
        product.setProductCd("P001");
        product.setProductNmKanji("商品 A");
        product.setProductNmKana("ショウヒン A");
        product.setUnitCd("PCS");
        product.setDeletedFlag("0");
        product.setCreatedUserCd("test_user");
        product.setCreatedTs(Instant.now());
        product.setUpdatedUserCd("test_user");
        product.setUpdatedTs(Instant.now());
        product.setVersion(0);
        productRepository.save(product);

        // 创建调出方库存（C001/P001，数量 100）
        Inventory inventory1 = new Inventory();
        inventory1.setCompanyCd("C001");
        inventory1.setProductCd("P001");
        inventory1.setQuantity(100);
        inventory1.setReservedQuantity(0);
        inventory1.setDeletedFlag("0");
        inventory1.setCreatedUserCd("test_user");
        inventory1.setCreatedTs(Instant.now());
        inventory1.setUpdatedUserCd("test_user");
        inventory1.setUpdatedTs(Instant.now());
        inventory1.setVersion(0);
        inventoryRepository.save(inventory1);

        // 创建调入方库存（C002/P001，数量 50）
        Inventory inventory2 = new Inventory();
        inventory2.setCompanyCd("C002");
        inventory2.setProductCd("P001");
        inventory2.setQuantity(50);
        inventory2.setReservedQuantity(0);
        inventory2.setDeletedFlag("0");
        inventory2.setCreatedUserCd("test_user");
        inventory2.setCreatedTs(Instant.now());
        inventory2.setUpdatedUserCd("test_user");
        inventory2.setUpdatedTs(Instant.now());
        inventory2.setVersion(0);
        inventoryRepository.save(inventory2);
    }

    private Map<String, Object> createTransferOrderDTO(String transferOrderNo, String fromCompanyCd, 
                                                        String toCompanyCd, String productCd, int quantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("transferOrderNo", transferOrderNo);
        request.put("fromCompanyCd", fromCompanyCd);
        request.put("toCompanyCd", toCompanyCd);
        request.put("productCd", productCd);
        request.put("quantity", quantity);
        return request;
    }

    // VP-055: 创建调拨单成功，默认状态 PENDING
    @Test
    void testCreateTransferOrder_Success() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF001", "C001", "C002", "P001", 20);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, request, Map.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TF001", response.getBody().get("transferOrderNo"));
        assertEquals("C001", response.getBody().get("fromCompanyCd"));
        assertEquals("C002", response.getBody().get("toCompanyCd"));
        assertEquals("P001", response.getBody().get("productCd"));
        assertEquals(20, (Integer) response.getBody().get("quantity"));
        assertEquals("PENDING", response.getBody().get("status"));
    }

    // VP-056: 调出方=调入方，创建失败
    @Test
    void testCreateTransferOrder_SameCompany_Failure() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF002", "C001", "C001", "P001", 20);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // VP-057: 调出方可用库存不足，创建失败
    @Test
    void testCreateTransferOrder_InsufficientStock_Failure() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF003", "C001", "C002", "P001", 200);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // VP-058: 重复调拨单号，创建失败
    @Test
    void testCreateTransferOrder_DuplicateNo_Failure() {
        // Given
        Map<String, Object> request1 = createTransferOrderDTO("TF004", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request1, Map.class);

        // When
        Map<String, Object> request2 = createTransferOrderDTO("TF004", "C001", "C002", "P001", 30);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request2, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // VP-059: 按单号查询调拨单成功
    @Test
    void testGetTransferOrder_Success() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF005", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + "/TF005", Map.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TF005", response.getBody().get("transferOrderNo"));
        assertEquals("C001", response.getBody().get("fromCompanyCd"));
        assertEquals("C002", response.getBody().get("toCompanyCd"));
        assertEquals("PENDING", response.getBody().get("status"));
    }

    // VP-060: 查询不存在的调拨单，返回 404
    @Test
    void testGetTransferOrder_NotFound() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/TF999", String.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // VP-061: 审批调拨→APPROVED + 调出方预占
    @Test
    void testApproveTransferOrder_Success() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF006", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/TF006/approve",
            HttpMethod.PUT,
            null,
            Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("APPROVED", response.getBody().get("status"));

        // 验证调出方库存预占
        ResponseEntity<Map> inventoryResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/inventory/C001/P001", Map.class);
        assertEquals(100, (Integer) inventoryResponse.getBody().get("quantity"));
        assertEquals(20, (Integer) inventoryResponse.getBody().get("reservedQuantity"));
    }

    // VP-062: 非 PENDING 状态不可审批
    @Test
    void testApproveTransferOrder_InvalidStatus_Failure() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF007", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);
        restTemplate.exchange(baseUrl + "/TF007/approve", HttpMethod.PUT, null, Map.class);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/TF007/approve",
            HttpMethod.PUT,
            null,
            String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // VP-063: 调拨出库→SHIPPED + 调出方扣减
    @Test
    void testShipTransferOrder_Success() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF008", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);
        restTemplate.exchange(baseUrl + "/TF008/approve", HttpMethod.PUT, null, Map.class);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/TF008/ship",
            HttpMethod.PUT,
            null,
            Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SHIPPED", response.getBody().get("status"));

        // 验证调出方库存扣减
        ResponseEntity<Map> inventoryResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/inventory/C001/P001", Map.class);
        assertEquals(80, (Integer) inventoryResponse.getBody().get("quantity"));
        assertEquals(0, (Integer) inventoryResponse.getBody().get("reservedQuantity"));
    }

    // VP-064: 非 APPROVED 状态不可出库
    @Test
    void testShipTransferOrder_InvalidStatus_Failure() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF009", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/TF009/ship",
            HttpMethod.PUT,
            null,
            String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // VP-065: 调拨入库→COMPLETED + 调入方增加
    @Test
    void testReceiveTransferOrder_Success() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF010", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);
        restTemplate.exchange(baseUrl + "/TF010/approve", HttpMethod.PUT, null, Map.class);
        restTemplate.exchange(baseUrl + "/TF010/ship", HttpMethod.PUT, null, Map.class);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/TF010/receive",
            HttpMethod.PUT,
            null,
            Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("COMPLETED", response.getBody().get("status"));

        // 验证调入方库存增加
        ResponseEntity<Map> inventoryResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/inventory/C002/P001", Map.class);
        assertEquals(70, (Integer) inventoryResponse.getBody().get("quantity"));
    }

    // VP-066: 非 SHIPPED 状态不可入库
    @Test
    void testReceiveTransferOrder_InvalidStatus_Failure() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF011", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/TF011/receive",
            HttpMethod.PUT,
            null,
            String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // VP-067: PENDING→CANCELLED
    @Test
    void testCancelTransferOrder_Pending_Success() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF012", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/TF012/cancel",
            HttpMethod.PUT,
            null,
            Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CANCELLED", response.getBody().get("status"));
    }

    // VP-068: APPROVED→CANCELLED + 恢复预占
    @Test
    void testCancelTransferOrder_Approved_Success() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF013", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);
        restTemplate.exchange(baseUrl + "/TF013/approve", HttpMethod.PUT, null, Map.class);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/TF013/cancel",
            HttpMethod.PUT,
            null,
            Map.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CANCELLED", response.getBody().get("status"));

        // 验证调出方库存预占恢复
        ResponseEntity<Map> inventoryResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/inventory/C001/P001", Map.class);
        assertEquals(100, (Integer) inventoryResponse.getBody().get("quantity"));
        assertEquals(0, (Integer) inventoryResponse.getBody().get("reservedQuantity"));
    }

    // VP-069: SHIPPED 状态不可取消
    @Test
    void testCancelTransferOrder_Shipped_Failure() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF014", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);
        restTemplate.exchange(baseUrl + "/TF014/approve", HttpMethod.PUT, null, Map.class);
        restTemplate.exchange(baseUrl + "/TF014/ship", HttpMethod.PUT, null, Map.class);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/TF014/cancel",
            HttpMethod.PUT,
            null,
            String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // VP-070: COMPLETED 状态不可取消
    @Test
    void testCancelTransferOrder_Completed_Failure() {
        // Given
        Map<String, Object> request = createTransferOrderDTO("TF015", "C001", "C002", "P001", 20);
        restTemplate.postForEntity(baseUrl, request, Map.class);
        restTemplate.exchange(baseUrl + "/TF015/approve", HttpMethod.PUT, null, Map.class);
        restTemplate.exchange(baseUrl + "/TF015/ship", HttpMethod.PUT, null, Map.class);
        restTemplate.exchange(baseUrl + "/TF015/receive", HttpMethod.PUT, null, Map.class);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/TF015/cancel",
            HttpMethod.PUT,
            null,
            String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
