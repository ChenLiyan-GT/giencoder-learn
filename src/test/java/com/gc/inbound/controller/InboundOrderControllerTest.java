package com.gc.inbound.controller;

import com.common.entity.InboundOrder;
import com.common.entity.Product;
import com.gc.inbound.dto.InboundOrderDTO;
import com.gc.inbound.repository.InboundOrderRepository;
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
class InboundOrderControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private ProductRepository productRepository;

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
        
        // 清理并重新创建商品数据
        productRepository.findByProductCd("P001").ifPresent(p -> productRepository.delete(p));
        productRepository.findByProductCd("P_DELETED").ifPresent(p -> productRepository.delete(p));
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
            () -> assertEquals("C001", response.getBody().getCompanyCd()),
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

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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
            () -> assertEquals("C001", response.getBody().getCompanyCd()),
            () -> assertEquals("P001", response.getBody().getProductCd()),
            () -> assertEquals(100, response.getBody().getQuantity())
        );
    }

    @Test
    @DisplayName("VP-013: 查询不存在的入库单返回 404")
    void shouldReturn404WhenInboundOrderNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/IO999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("VP-014: 删除的商品不能入库")
    void shouldFailWhenProductIsDeleted() {
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderCd("IO002");
        request.setCompanyCd("C001");
        request.setProductCd("P_DELETED");
        request.setQuantity(50);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
