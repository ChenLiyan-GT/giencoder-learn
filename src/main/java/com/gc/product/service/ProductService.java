package com.gc.product.service;

import com.common.entity.Product;
import com.gc.product.dto.ProductDTO;
import com.gc.product.repository.ProductRepository;
import io.micrometer.common.util.StringUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private static final int PRODUCT_CD_MAX_LENGTH = 20;
    private static final int PRODUCT_NM_KANJI_MAX_LENGTH = 40;
    private static final int PRODUCT_NM_KANA_MAX_LENGTH = 40;
    private static final int UNIT_CD_MAX_LENGTH = 10;

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductByProductCd(String productCd) {
        Optional<Product> productOptional = productRepository.findByProductCdAndDeletedFlag(productCd, "0");

        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            return convertToDTO(product);
        }

        return null;
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        validateProductDTO(productDTO, true);
        
        // 重複チェック
        if (productRepository.findByProductCd(productDTO.getProductCd()).isPresent()) {
            throw new IllegalStateException("指定された商品コードは既に存在します");
        }
        
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        Instant now = Instant.now();
        product.setCreatedTs(now);
        product.setCreatedUserCd("system");
        product.setUpdatedTs(now);
        product.setUpdatedUserCd("system");
        product.setVersion(0);
        if (product.getDeletedFlag() == null) {
            product.setDeletedFlag("0");
        }
        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }

    public ProductDTO updateProduct(String productCd, ProductDTO productDTO) {
        Optional<Product> productOptional = productRepository.findByProductCdAndDeletedFlag(productCd, "0");
        if (productOptional.isEmpty()) {
            return null;
        }
        
        validateProductDTO(productDTO, false);
        
        Product product = productOptional.get();
        BeanUtils.copyProperties(productDTO, product, "productId", "productCd", "createdTs", "createdUserCd", "createdProgram", "version", "deletedFlag");
        product.setUpdatedTs(Instant.now());
        product.setUpdatedUserCd("system");
        Product updated = productRepository.save(product);
        return convertToDTO(updated);
    }

    public boolean deleteProduct(String productCd) {
        Optional<Product> productOptional = productRepository.findByProductCdAndDeletedFlag(productCd, "0");
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setDeletedFlag("1");
            product.setUpdatedTs(Instant.now());
            product.setUpdatedUserCd("system");
            productRepository.save(product);
            return true;
        }
        return false;
    }

    private void validateProductDTO(ProductDTO productDTO, boolean isNew) {
        if (isNew && StringUtils.isEmpty(productDTO.getProductCd())) {
            throw new IllegalArgumentException("商品コードは必須です");
        }
        
        if (StringUtils.isEmpty(productDTO.getProductNmKanji())) {
            throw new IllegalArgumentException("商品名（漢字）は必須です");
        }
        
        if (productDTO.getProductCd() != null && productDTO.getProductCd().length() > PRODUCT_CD_MAX_LENGTH) {
            throw new IllegalArgumentException("商品コードは" + PRODUCT_CD_MAX_LENGTH + "文字以内で入力してください");
        }
        
        if (productDTO.getProductNmKanji() != null && productDTO.getProductNmKanji().length() > PRODUCT_NM_KANJI_MAX_LENGTH) {
            throw new IllegalArgumentException("商品名（漢字）は" + PRODUCT_NM_KANJI_MAX_LENGTH + "文字以内で入力してください");
        }
        
        if (productDTO.getProductNmKana() != null && productDTO.getProductNmKana().length() > PRODUCT_NM_KANA_MAX_LENGTH) {
            throw new IllegalArgumentException("商品名（カナ）は" + PRODUCT_NM_KANA_MAX_LENGTH + "文字以内で入力してください");
        }
        
        if (productDTO.getUnitCd() != null && productDTO.getUnitCd().length() > UNIT_CD_MAX_LENGTH) {
            throw new IllegalArgumentException("単位コードは" + UNIT_CD_MAX_LENGTH + "文字以内で入力してください");
        }
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }
}
