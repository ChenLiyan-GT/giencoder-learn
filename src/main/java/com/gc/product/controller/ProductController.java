package com.gc.product.controller;

import com.gc.product.dto.ProductDTO;
import com.gc.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productCd}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable String productCd) {
        ProductDTO productDTO = productService.getProductByProductCd(productCd);

        if (productDTO != null) {
            return ResponseEntity.ok(productDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{productCd}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable String productCd, @RequestBody ProductDTO productDTO) {
        ProductDTO updated = productService.updateProduct(productCd, productDTO);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{productCd}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productCd) {
        boolean deleted = productService.deleteProduct(productCd);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
