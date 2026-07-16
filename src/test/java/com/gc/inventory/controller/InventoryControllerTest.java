package com.gc.inventory.controller;

import com.common.entity.Inventory;
import com.gc.inventory.dto.InventoryDTO;
import com.gc.inventory.repository.InventoryRepository;
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
class InventoryControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/inventory";
        resetTestData();
    }

    private void resetTestData() {
        // 清理测试用数据
        inventoryRepository.findByCompanyCdAndProductCd("C001", "P001").ifPresent(i -> inventoryRepository.delete(i));
        inventoryRepository.findByCompanyCdAndProductCd("C001", "P002").ifPresent(i -> inventoryRepository.delete(i));
        inventoryRepository.findByCompanyCdAndProductCd("C002", "P001").ifPresent(i -> inventoryRepository.delete(i));

        Instant now = Instant.now();

        // 创建测试数据：C001 公司 P001 商品库存 100 个
        Inventory inv1 = new Inventory();
        inv1.setCompanyCd("C001");
        inv1.setProductCd("P001");
        inv1.setQuantity(100);
        inv1.setReservedQuantity(10);
        inv1.setVersion(0);
        inv1.setDeletedFlag("0");
        inv1.setCreatedUserCd("mock_user");
        inv1.setCreatedTs(now);
        inv1.setUpdatedTs(now);
        inv1.setUpdatedUserCd("mock_user");
        inventoryRepository.save(inv1);

        // 创建测试数据：C001 公司 P002 商品库存 50 个
        Inventory inv2 = new Inventory();
        inv2.setCompanyCd("C001");
        inv2.setProductCd("P002");
        inv2.setQuantity(50);
        inv2.setReservedQuantity(5);
        inv2.setVersion(0);
        inv2.setDeletedFlag("0");
        inv2.setCreatedUserCd("mock_user");
        inv2.setCreatedTs(now);
        inv2.setUpdatedTs(now);
        inv2.setUpdatedUserCd("mock_user");
        inventoryRepository.save(inv2);

        // 创建测试数据：C002 公司 P001 商品库存 200 个
        Inventory inv3 = new Inventory();
        inv3.setCompanyCd("C002");
        inv3.setProductCd("P001");
        inv3.setQuantity(200);
        inv3.setReservedQuantity(20);
        inv3.setVersion(0);
        inv3.setDeletedFlag("0");
        inv3.setCreatedUserCd("mock_user");
        inv3.setCreatedTs(now);
        inv3.setUpdatedTs(now);
        inv3.setUpdatedUserCd("mock_user");
        inventoryRepository.save(inv3);
    }

    // ==================== VP-047: 分页查询库存列表 ====================
    @Test
    @DisplayName("VP-047: getList - 分页查询库存列表成功")
    void shouldReturn200AndInventoryListWhenGetList() {
        ResponseEntity<InventoryDTO[]> response = restTemplate.getForEntity(baseUrl + "?page=0&size=10", InventoryDTO[].class);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertTrue(response.getBody().length >= 3)
        );
    }

    // ==================== VP-048: 按公司 + 商品查询库存成功 ====================
    @Test
    @DisplayName("VP-048: get - 按公司 + 商品查询库存成功返回 200")
    void shouldReturn200AndInventoryDTOWhenInventoryExists() {
        ResponseEntity<InventoryDTO> response = restTemplate.getForEntity(baseUrl + "/C001/P001", InventoryDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("C001", response.getBody().getCompanyCd()),
            () -> assertEquals("P001", response.getBody().getProductCd()),
            () -> assertEquals(100, response.getBody().getQuantity()),
            () -> assertEquals(10, response.getBody().getReservedQuantity())
        );
    }

    // ==================== VP-049: 查询不存在的库存返回 404 ====================
    @Test
    @DisplayName("VP-049: get - 查询不存在的库存返回 404")
    void shouldReturn404WhenInventoryNotFound() {
        ResponseEntity<InventoryDTO> response = restTemplate.getForEntity(baseUrl + "/NOT_EXIST/NOT_EXIST", InventoryDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ==================== VP-050: 库存调整成功 + 记录原因 ====================
    @Test
    @DisplayName("VP-050: adjust - 库存调整成功返回 200 并记录原因")
    void shouldReturn200WhenAdjustSucceeds() {
        InventoryDTO request = new InventoryDTO();
        request.setQuantity(150);
        request.setAdjustmentReason("库存盘点调整");

        HttpEntity<InventoryDTO> entity = new HttpEntity<>(request);
        ResponseEntity<InventoryDTO> response = restTemplate.exchange(
            baseUrl + "/C001/P001/adjust", HttpMethod.PUT, entity, InventoryDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals(150, response.getBody().getQuantity())
        );
    }

    // ==================== VP-051: 库存数量不可为负 ====================
    @Test
    @DisplayName("VP-051: adjust - 库存数量不可为负返回 400")
    void shouldReturn400WhenAdjustToNegativeQuantity() {
        InventoryDTO request = new InventoryDTO();
        request.setQuantity(-10);
        request.setAdjustmentReason("非法调整");

        HttpEntity<InventoryDTO> entity = new HttpEntity<>(request);
        ResponseEntity<InventoryDTO> response = restTemplate.exchange(
            baseUrl + "/C001/P001/adjust", HttpMethod.PUT, entity, InventoryDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== VP-052: 可用库存计算验证 ====================
    @Test
    @DisplayName("VP-052: get - 可用库存计算正确 (quantity - reservedQuantity)")
    void shouldCalculateAvailableQuantityCorrectly() {
        ResponseEntity<InventoryDTO> response = restTemplate.getForEntity(baseUrl + "/C001/P001", InventoryDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals(100, response.getBody().getQuantity()),
            () -> assertEquals(10, response.getBody().getReservedQuantity()),
            () -> assertEquals(90, response.getBody().getAvailableQuantity())
        );
    }
}
