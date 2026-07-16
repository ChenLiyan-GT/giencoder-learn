package com.gc.inbound.controller;

import com.common.entity.InboundOrder;
import com.common.entity.Product;
import com.gc.inbound.dto.InboundOrderDTO;
import com.gc.inbound.repository.InboundOrderRepository;
import com.gc.inventory.dto.InventoryDTO;
import com.gc.inventory.repository.InventoryRepository;
import com.gc.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InboundOrderControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/inbound-orders";
        resetTestData();
    }

    private void resetTestData() {
        // 清理测试数据
        inboundOrderRepository.findByInboundOrderCd("IO001").ifPresent(i -> inboundOrderRepository.delete(i));
        inboundOrderRepository.findByInboundOrderCd("IO002").ifPresent(i -> inboundOrderRepository.delete(i));
        inboundOrderRepository.findByInboundOrderCd("IO003").ifPresent(i -> inboundOrderRepository.delete(i));
        
        // 清理已删除商品测试数据
        productRepository.findByProductCd("P_DELETED").ifPresent(p -> productRepository.delete(p));
        
        // 清理库存测试数据
        inventoryRepository.findByCompanyCdAndProductCdAndDeletedFlag("C001", "P001", "0").ifPresent(i -> inventoryRepository.delete(i));
        
        // 确保有商品数据
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
    }

    @Test
    @DisplayName("VP-010: 创建入库单默认 RECEIVED 状态")
    void shouldCreateInboundOrderWithReceivedStatus() {
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);

        ResponseEntity<InboundOrderDTO> response = restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("IO001", response.getBody().getInboundOrderCd()),
            () -> assertEquals("P001", response.getBody().getProductCd()),
            () -> assertEquals(100, response.getBody().getQuantity())
        );
    }

    @Test
    @DisplayName("VP-011: 重复单号创建失败")
    void shouldFailWhenDuplicateInboundOrderCd() {
        // 先创建一个入库单
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);
        restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        // 尝试用相同单号创建
        InboundOrderDTO request2 = new InboundOrderDTO();
        request2.setInboundOrderCd("IO001");
        request2.setCompanyCd("C001");
        request2.setProductCd("P001");
        request2.setQuantity(50);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request2, String.class);

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
            () -> assertTrue(response.getBody().contains("入库单号已存在"))
        );
    }

    @Test
    @DisplayName("VP-012: 按单号查询入库单成功")
    void shouldGetInboundOrderById() {
        // 先创建测试数据
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);
        restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        ResponseEntity<InboundOrderDTO> response = restTemplate.getForEntity(baseUrl + "/IO001", InboundOrderDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("IO001", response.getBody().getInboundOrderCd()),
            () -> assertEquals("P001", response.getBody().getProductCd()),
            () -> assertEquals(100, response.getBody().getQuantity())
        );
    }

    @Test
    @DisplayName("VP-013: 查询不存在的入库单返回 404")
    void shouldReturn404WhenInboundOrderNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/IO999", String.class);

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
            () -> assertNull(response.getBody())
        );
    }

    @Test
    @DisplayName("VP-014: 删除的商品不能入库")
    void shouldFailWhenProductIsDeleted() {
        // 创建已删除商品
        Instant now = Instant.now();
        Product pDeleted = new Product();
        pDeleted.setProductCd("P_DELETED");
        pDeleted.setProductNmKanji("删除商品");
        pDeleted.setProductNmKana("サクジョショウヒン");
        pDeleted.setUnitCd("PCS");
        pDeleted.setVersion(0);
        pDeleted.setDeletedFlag("1");
        pDeleted.setCreatedUserCd("mock_user");
        pDeleted.setCreatedTs(now);
        pDeleted.setUpdatedUserCd("mock_user");
        pDeleted.setUpdatedTs(now);
        productRepository.save(pDeleted);

        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO002");
        request.setCompanyCd("C001");
        request.setProductCd("P_DELETED");
        request.setQuantity(50);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("VP-015: 确认入库→库存增加")
    void shouldIncreaseQuantityAfterConfirm() {
        // 创建入库单
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);
        restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        // 确认入库
        ResponseEntity<InboundOrderDTO> confirmResponse = restTemplate.exchange(
            baseUrl + "/IO001/confirm",
            HttpMethod.PUT,
            null,
            InboundOrderDTO.class
        );

        assertEquals(HttpStatus.OK, confirmResponse.getStatusCode());
        assertEquals("CONFIRMED", confirmResponse.getBody().getStatus());

        // 验证库存增加
        ResponseEntity<InventoryDTO> inventoryResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/inventory/C001/P001",
            InventoryDTO.class
        );

        assertEquals(HttpStatus.OK, inventoryResponse.getStatusCode());
        assertEquals(100, inventoryResponse.getBody().getQuantity());
    }

    @Test
    @DisplayName("VP-016: 确认入库后状态变更为 CONFIRMED")
    void shouldChangeStatusToConfirmedAfterConfirm() {
        // 创建入库单
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);
        restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        // 确认入库
        ResponseEntity<InboundOrderDTO> confirmResponse = restTemplate.exchange(
            baseUrl + "/IO001/confirm",
            HttpMethod.PUT,
            null,
            InboundOrderDTO.class
        );

        assertAll(
            () -> assertEquals(HttpStatus.OK, confirmResponse.getStatusCode()),
            () -> assertEquals("CONFIRMED", confirmResponse.getBody().getStatus())
        );
    }

    @Test
    @DisplayName("VP-017: 拒绝入库→库存不变")
    void shouldNotChangeQuantityAfterReject() {
        // 创建入库单
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);
        restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        // 拒绝入库
        ResponseEntity<InboundOrderDTO> rejectResponse = restTemplate.exchange(
            baseUrl + "/IO001/reject",
            HttpMethod.PUT,
            null,
            InboundOrderDTO.class
        );

        assertEquals(HttpStatus.OK, rejectResponse.getStatusCode());
        assertEquals("REJECTED", rejectResponse.getBody().getStatus());

        // 验证库存不存在（因为没有确认）
        ResponseEntity<String> inventoryResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/inventory/C001/P001",
            String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, inventoryResponse.getStatusCode());
    }

    @Test
    @DisplayName("VP-018: 已确认的入库单不可再次确认")
    void shouldFailToConfirmAlreadyConfirmedOrder() {
        // 创建并确认入库单
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);
        restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        restTemplate.exchange(baseUrl + "/IO001/confirm", HttpMethod.PUT, null, InboundOrderDTO.class);

        // 再次确认
        ResponseEntity<String> secondConfirmResponse = restTemplate.exchange(
            baseUrl + "/IO001/confirm",
            HttpMethod.PUT,
            null,
            String.class
        );

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST, secondConfirmResponse.getStatusCode()),
            () -> assertTrue(secondConfirmResponse.getBody().contains("只有 RECEIVED 状态的入库单才能确认"))
        );
    }

    @Test
    @DisplayName("VP-019: 已拒绝的入库单不可确认")
    void shouldFailToConfirmRejectedOrder() {
        // 创建并拒绝入库单
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);
        restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        restTemplate.exchange(baseUrl + "/IO001/reject", HttpMethod.PUT, null, InboundOrderDTO.class);

        // 尝试确认
        ResponseEntity<String> confirmResponse = restTemplate.exchange(
            baseUrl + "/IO001/confirm",
            HttpMethod.PUT,
            null,
            String.class
        );

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST, confirmResponse.getStatusCode()),
            () -> assertTrue(confirmResponse.getBody().contains("只有 RECEIVED 状态的入库单才能确认"))
        );
    }

    @Test
    @DisplayName("VP-020: 已确认的入库单不可拒绝")
    void shouldFailToRejectConfirmedOrder() {
        // 创建并确认入库单
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);
        restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        restTemplate.exchange(baseUrl + "/IO001/confirm", HttpMethod.PUT, null, InboundOrderDTO.class);

        // 尝试拒绝
        ResponseEntity<String> rejectResponse = restTemplate.exchange(
            baseUrl + "/IO001/reject",
            HttpMethod.PUT,
            null,
            String.class
        );

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST, rejectResponse.getStatusCode()),
            () -> assertTrue(rejectResponse.getBody().contains("只有 RECEIVED 状态的入库单才能拒绝"))
        );
    }

    @Test
    @DisplayName("VP-021: 拒绝已拒绝的入库单失败")
    void shouldFailToRejectAlreadyRejectedOrder() {
        // 创建并拒绝入库单
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);
        restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        restTemplate.exchange(baseUrl + "/IO001/reject", HttpMethod.PUT, null, InboundOrderDTO.class);

        // 再次拒绝
        ResponseEntity<String> secondRejectResponse = restTemplate.exchange(
            baseUrl + "/IO001/reject",
            HttpMethod.PUT,
            null,
            String.class
        );

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST, secondRejectResponse.getStatusCode()),
            () -> assertTrue(secondRejectResponse.getBody().contains("只有 RECEIVED 状态的入库单才能拒绝"))
        );
    }
}
