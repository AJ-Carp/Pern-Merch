package com.ajcarpinello.Pern_Merch_Website.controller;

import com.ajcarpinello.Pern_Merch_Website.dto.ProductDTO;
import com.ajcarpinello.Pern_Merch_Website.dto.ProductVariantDTO;
import com.ajcarpinello.Pern_Merch_Website.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(productService.getProductsByCategory(category));
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.createProduct(dto));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long productId, @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(productId, dto));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/variants/{productId}")
    public ResponseEntity<ProductVariantDTO> addVariant(@PathVariable Long productId, @RequestBody ProductVariantDTO dto) {
        return ResponseEntity.ok(productService.addVariant(productId, dto));
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<ProductVariantDTO> updateVariant(@PathVariable Long variantId, @RequestBody ProductVariantDTO dto) {
        return ResponseEntity.ok(productService.updateVariant(variantId, dto));
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long variantId) {
        productService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }
}
