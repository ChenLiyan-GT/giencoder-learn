package com.example.demo.controller;

import com.example.demo.dto.CompanyDTO;
import com.example.demo.entity.Company;
import com.example.demo.repository.CompanyRepository;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompanyControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompanyRepository companyRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/companies";
        resetTestData();
    }

    private void resetTestData() {
        // 先清理测试用数据，确保每次运行环境一致
        companyRepository.findByCompanyCd("C001").ifPresent(c -> companyRepository.delete(c));
        companyRepository.findByCompanyCd("C002").ifPresent(c -> companyRepository.delete(c));
        companyRepository.findByCompanyCd("C003").ifPresent(c -> companyRepository.delete(c));
        companyRepository.findByCompanyCd("C004").ifPresent(c -> companyRepository.delete(c));

        // 重新插入 C001
        Company c1 = new Company();
        c1.setCompanyCd("C001");
        c1.setCompanyNmKanji("テスト株式会社");
        c1.setCompanyNmKana("テストカブシキガイシャ");
        c1.setCompanyAbbreviation("テスト");
        c1.setPostalCd("1000001");
        c1.setAreaCd("001");
        c1.setAddress("東京都千代田区1-1-1");
        c1.setPhoneNo("03-1111-1111");
        c1.setFaxNo("03-1111-1112");
        c1.setVersion(0);
        c1.setDeletedFlag("0");
        c1.setCreatedUserCd("mock_user");
        companyRepository.save(c1);

        // 重新插入 C002
        Company c2 = new Company();
        c2.setCompanyCd("C002");
        c2.setCompanyNmKanji("サンプル株式会社");
        c2.setCompanyNmKana("サンプルカブシキガイシャ");
        c2.setCompanyAbbreviation("サンプル");
        c2.setPostalCd("1000002");
        c2.setAreaCd("002");
        c2.setAddress("東京都港区2-2-2");
        c2.setPhoneNo("03-2222-2221");
        c2.setFaxNo("03-2222-2222");
        c2.setVersion(0);
        c2.setDeletedFlag("0");
        c2.setCreatedUserCd("mock_user");
        companyRepository.save(c2);
    }

    @Test
    @DisplayName("getCompany: 公司存在时返回200和公司DTO")
    void shouldReturn200AndCompanyDTOWhenCompanyExists() {
        ResponseEntity<CompanyDTO> response = restTemplate.getForEntity(baseUrl + "/C001", CompanyDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("C001", response.getBody().getCompanyCd()),
            () -> assertEquals("テスト株式会社", response.getBody().getCompanyNmKanji())
        );
    }

    @Test
    @DisplayName("getCompany: 公司不存在时返回404")
    void shouldReturn404WhenCompanyNotFound() {
        ResponseEntity<CompanyDTO> response = restTemplate.getForEntity(baseUrl + "/NOT_EXIST", CompanyDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("createCompany: 创建公司成功时返回201和公司DTO")
    void shouldReturn201AndCreatedDTO() {
        CompanyDTO request = new CompanyDTO();
        request.setCompanyCd("C003");
        request.setCompanyNmKanji("新規株式会社");
        request.setCompanyNmKana("シンキカブシキガイシャ");
        request.setDeletedFlag("0");
        request.setVersion(0);

        ResponseEntity<CompanyDTO> response = restTemplate.postForEntity(baseUrl, request, CompanyDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("C003", response.getBody().getCompanyCd()),
            () -> assertEquals("新規株式会社", response.getBody().getCompanyNmKanji())
        );
    }

    @Test
    @DisplayName("createCompany: 不传deletedFlag和version时使用默认值")
    void shouldReturn201WhenCreateWithDefaultValues() {
        CompanyDTO request = new CompanyDTO();
        request.setCompanyCd("C004");
        request.setCompanyNmKanji("デフォルト株式会社");
        request.setCompanyNmKana("デフォルトカブシキガイシャ");

        ResponseEntity<CompanyDTO> response = restTemplate.postForEntity(baseUrl, request, CompanyDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("C004", response.getBody().getCompanyCd()),
            () -> assertEquals("0", response.getBody().getDeletedFlag()),
            () -> assertEquals(0, response.getBody().getVersion())
        );
    }

    @Test
    @DisplayName("updateCompany: 更新公司成功时返回200")
    void shouldReturn200WhenUpdateSucceeds() {
        CompanyDTO request = new CompanyDTO();
        request.setCompanyNmKanji("更新株式会社");

        HttpEntity<CompanyDTO> entity = new HttpEntity<>(request);
        ResponseEntity<CompanyDTO> response = restTemplate.exchange(
            baseUrl + "/C001", HttpMethod.PUT, entity, CompanyDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("更新株式会社", response.getBody().getCompanyNmKanji())
        );
    }

    @Test
    @DisplayName("updateCompany: 更新不存在的公司时返回404")
    void shouldReturn404WhenUpdateCompanyNotFound() {
        CompanyDTO request = new CompanyDTO();
        request.setCompanyNmKanji("更新株式会社");

        HttpEntity<CompanyDTO> entity = new HttpEntity<>(request);
        ResponseEntity<CompanyDTO> response = restTemplate.exchange(
            baseUrl + "/NOT_EXIST", HttpMethod.PUT, entity, CompanyDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("deleteCompany: 删除公司成功时返回204")
    void shouldReturn204WhenDeleteSucceeds() {
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/C001", HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        assertFalse(companyRepository.findByCompanyCd("C001").isPresent());
    }

    @Test
    @DisplayName("deleteCompany: 删除不存在的公司时返回404")
    void shouldReturn404WhenDeleteCompanyNotFound() {
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/NOT_EXIST", HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}