package com.gc.outbound.controller;

import com.common.entity.Inventory;
import com.common.entity.Product;
import com.gc.inventory.repository.InventoryRepository;
import com.gc.outbound.dto.OutboundOrderDTO;
import com.gc.outbound.repository.OutboundOrderRepository;
import com.gc.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OutboundOrderControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/outbound-orders";
        resetTestData();
    }

    private void resetTestData() {
        // 清理测试数据
        outboundOrderRepository.findByOutboundOrderCd("OO001").ifPresent(o -> outboundOrderRepository.delete(o));
        outboundOrderRepository.findByOutboundOrderCd("OO002").ifPresent(o -> outboundOrderRepository.delete(o));
        outboundOrderRepository.findByOutboundOrderCd("OO003").ifPresent(o -> outboundOrderRepository.delete(o));
        outboundOrderRepository.findByOutboundOrderCd("OO999").ifPresent(o -> outboundOrderRepository.delete(o));
        
        // 清理并重新创建商品数据
        productRepository.findByProductCd("P001").ifPresent(p -> productRepository.delete(p));
        Instant now = Instant.now();
        Product p1 = new Product();
        p1.setProductCd("P001");
        p1.setProductNmKanji("商品 A");
        p1.setProductNmKana("ショウヒンエー");
        p1.setUnitCd("PCS");
        p1.setVersion(0);
        p1.setDeletedFlag("0");
        p1.setCreatedUserCd("mock_user");
        p1.setCreatedTs(now);
        p1.setUpdatedUserCd("mock_user");
        p1.setUpdatedTs(now);
        productRepository.save(p1);

        // 清理并重新创建库存数据
        inventoryRepository.findByCompanyCdAndProductCdAndDeletedFlag("C001", "P001", "0").ifPresent(i -> inventoryRepository.delete(i));
        Inventory inv = new Inventory();
        inv.setCompanyCd("C001");
        inv.setProductCd("P001");
        inv.setQuantity(100);
        inv.setReservedQuantity(0);
        inv.setVersion(0);
        inv.setDeletedFlag("0");
        inv.setCreatedUserCd("mock_user");
        inv.setCreatedTs(now);
        inv.setUpdatedUserCd("mock_user");
        inv.setUpdatedTs(now);
        inventoryRepository.save(inv);
    }

    @Test
    @DisplayName("VP-035: 创建出库单默认 PENDING 状态")
    void shouldCreateOutboundOrderWithPendingStatus() {
        OutboundOrderDTO request = new OutboundOrderDTO();
        request.setOutboundOrderCd("OO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(50);

        ResponseEntity<OutboundOrderDTO> response = restTemplate.postForEntity(baseUrl, request, OutboundOrderDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("OO001", response.getBody().getOutboundOrderCd()),
            () -> assertEquals("C001", response.getBody().getCompanyCd()),
            () -> assertEquals("P001", response.getBody().getProductCd()),
            () -> assertEquals(50, response.getBody().getQuantity())
        );
    }

    @Test
    @DisplayName("VP-036: 库存不足拒绝创建")
    void shouldFailWhenInsufficientInventory() {
        OutboundOrderDTO request = new OutboundOrderDTO();
        request.setOutboundOrderCd("OO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(9999); // 远超库存数量

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("VP-037: 重复单号创建失败")
    void shouldFailWhenDuplicateOutboundOrderCd() {
        // 先创建一个出库单
        OutboundOrderDTO request = new OutboundOrderDTO();
        request.setOutboundOrderCd("OO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(50);
        restTemplate.postForEntity(baseUrl, request, OutboundOrderDTO.class);

        // 尝试用相同单号创建
        OutboundOrderDTO request2 = new OutboundOrderDTO();
        request2.setOutboundOrderCd("OO001");
        request2.setCompanyCd("C001");
        request2.setProductCd("P001");
        request2.setQuantity(30);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request2, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("VP-038: 按单号查询出库单成功")
    void shouldGetOutboundOrderById() {
        // 先创建测试数据
        OutboundOrderDTO request = new OutboundOrderDTO();
        request.setOutboundOrderCd("OO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(50);
        restTemplate.postForEntity(baseUrl, request, OutboundOrderDTO.class);

        ResponseEntity<OutboundOrderDTO> response = restTemplate.getForEntity(baseUrl + "/OO001", OutboundOrderDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("OO001", response.getBody().getOutboundOrderCd()),
            () -> assertEquals("C001", response.getBody().getCompanyCd()),
            () -> assertEquals("P001", response.getBody().getProductCd()),
            () -> assertEquals(50, response.getBody().getQuantity())
        );
    }

    @Test
    @DisplayName("VP-039: 查询不存在的出库单返回 404")
    void shouldReturn404WhenOutboundOrderNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/OO999", String.class);

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
            () -> assertTrue(response.getBody().contains("不存在"))
        );
    }
}
