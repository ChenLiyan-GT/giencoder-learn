package com.example.demo.controller;

import com.example.demo.dto.CompanyDTO;
import com.example.demo.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 公司 Controller
 */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * 根据公司代码查询公司信息
     * GET /api/companies/{companyCd}
     * 
     * @param companyCd 公司代码
     * @return 公司信息
     */
    @GetMapping("/{companyCd}")
    public ResponseEntity<CompanyDTO> getCompany(@PathVariable String companyCd) {
        CompanyDTO companyDTO = companyService.getCompanyByCompanyCd(companyCd);
        
        if (companyDTO != null) {
            return ResponseEntity.ok(companyDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<CompanyDTO> createCompany(@RequestBody CompanyDTO companyDTO) {
        CompanyDTO created = companyService.createCompany(companyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{companyCd}")
    public ResponseEntity<CompanyDTO> updateCompany(@PathVariable String companyCd, @RequestBody CompanyDTO companyDTO) {
        CompanyDTO updated = companyService.updateCompany(companyCd, companyDTO);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{companyCd}")
    public ResponseEntity<Void> deleteCompany(@PathVariable String companyCd) {
        boolean deleted = companyService.deleteCompany(companyCd);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
