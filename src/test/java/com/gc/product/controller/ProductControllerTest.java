package com.gc.product.controller;

import com.common.entity.Product;
import com.gc.product.dto.ProductDTO;
import com.gc.product.repository.ProductRepository;
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
class ProductControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/products";
        resetTestData();
    }

    private void resetTestData() {
        // 先清理测试用数据，确保每次运行环境一致
        productRepository.findByProductCd("P001").ifPresent(p -> productRepository.delete(p));
        productRepository.findByProductCd("P002").ifPresent(p -> productRepository.delete(p));
        productRepository.findByProductCd("P003").ifPresent(p -> productRepository.delete(p));
        productRepository.findByProductCd("P004").ifPresent(p -> productRepository.delete(p));
        productRepository.findByProductCd("P005").ifPresent(p -> productRepository.delete(p));

        Instant now = Instant.now();
        
        // 重新插入 P001
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

        // 重新插入 P002
        Product p2 = new Product();
        p2.setProductCd("P002");
        p2.setProductNmKanji("商品 B");
        p2.setProductNmKana("ショウヒンビー");
        p2.setUnitCd("BOX");
        p2.setVersion(0);
        p2.setDeletedFlag("0");
        p2.setCreatedUserCd("mock_user");
        p2.setCreatedTs(now);
        p2.setUpdatedUserCd("mock_user");
        p2.setUpdatedTs(now);
        productRepository.save(p2);
    }

    @Test
    @DisplayName("VP-001: 创建商品成功时返回 201")
    void shouldReturn201WhenCreateProductSucceeds() {
        ProductDTO request = new ProductDTO();
        request.setProductCd("P003");
        request.setProductNmKanji("商品 C");
        request.setProductNmKana("ショウヒンシー");
        request.setUnitCd("SET");

        ResponseEntity<ProductDTO> response = restTemplate.postForEntity(baseUrl, request, ProductDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("P003", response.getBody().getProductCd()),
            () -> assertEquals("商品 C", response.getBody().getProductNmKanji())
        );
    }

    @Test
    @DisplayName("VP-002: 创建商品字段完整性")
    void shouldReturnAllFieldsWhenCreateProduct() {
        ProductDTO request = new ProductDTO();
        request.setProductCd("P004");
        request.setProductNmKanji("商品 D");
        request.setProductNmKana("ショウヒンドィー");
        request.setUnitCd("PCS");

        ResponseEntity<ProductDTO> response = restTemplate.postForEntity(baseUrl, request, ProductDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("P004", response.getBody().getProductCd()),
            () -> assertEquals("商品 D", response.getBody().getProductNmKanji()),
            () -> assertEquals("ショウヒンドィー", response.getBody().getProductNmKana()),
            () -> assertEquals("PCS", response.getBody().getUnitCd())
        );
    }

    @Test
    @DisplayName("VP-003: 重复商品代码创建失败")
    void shouldFailWhenDuplicateProductCd() {
        ProductDTO request = new ProductDTO();
        request.setProductCd("P001");
        request.setProductNmKanji("重复商品");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertNotEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("VP-004: 按商品代码查询成功时返回 200")
    void shouldReturn200WhenProductExists() {
        ResponseEntity<ProductDTO> response = restTemplate.getForEntity(baseUrl + "/P001", ProductDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("P001", response.getBody().getProductCd()),
            () -> assertEquals("商品 A", response.getBody().getProductNmKanji())
        );
    }

    @Test
    @DisplayName("VP-005: 查询不存在的商品代码时返回 404")
    void shouldReturn404WhenProductNotFound() {
        ResponseEntity<ProductDTO> response = restTemplate.getForEntity(baseUrl + "/P999", ProductDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("VP-006: 更新商品成功时返回 200")
    void shouldReturn200WhenUpdateSucceeds() {
        ProductDTO request = new ProductDTO();
        request.setProductNmKanji("商品 A 改");
        request.setProductNmKana("ショウヒンエーカイ");

        HttpEntity<ProductDTO> entity = new HttpEntity<>(request);
        ResponseEntity<ProductDTO> response = restTemplate.exchange(
            baseUrl + "/P001", HttpMethod.PUT, entity, ProductDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("商品 A 改", response.getBody().getProductNmKanji()),
            () -> assertEquals("ショウヒンエーカイ", response.getBody().getProductNmKana())
        );
    }

    @Test
    @DisplayName("VP-007: 逻辑删除商品后再次查询返回 404")
    void shouldReturn404AfterLogicalDelete() {
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            baseUrl + "/P001", HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        ResponseEntity<ProductDTO> getResponse = restTemplate.getForEntity(baseUrl + "/P001", ProductDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    @DisplayName("VP-007b: 删除不存在的商品时返回 404")
    void shouldReturn404WhenDeleteProductNotFound() {
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/P999", HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("VP-008: 商品代码为空时创建失败")
    void shouldReturn500WhenProductCdIsEmpty() {
        ProductDTO request = new ProductDTO();
        request.setProductCd("");
        request.setProductNmKanji("空代码商品");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertNotEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("VP-009: 商品代码超限时创建失败")
    void shouldReturn500WhenProductCdTooLong() {
        ProductDTO request = new ProductDTO();
        request.setProductCd("P".repeat(21));
        request.setProductNmKanji("超长代码商品");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertNotEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("VP-010: 商品名称（汉字）为空时创建失败")
    void shouldReturn500WhenProductNmKanjiIsEmpty() {
        ProductDTO request = new ProductDTO();
        request.setProductCd("P005");
        request.setProductNmKanji("");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertNotEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
