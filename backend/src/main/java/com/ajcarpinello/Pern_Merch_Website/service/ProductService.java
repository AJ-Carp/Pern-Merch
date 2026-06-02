package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.dto.ProductDTO;
import com.ajcarpinello.Pern_Merch_Website.dto.ProductVariantDTO;
import com.ajcarpinello.Pern_Merch_Website.entity.Product;
import com.ajcarpinello.Pern_Merch_Website.entity.ProductVariant;
import com.ajcarpinello.Pern_Merch_Website.exception.AppException;
import com.ajcarpinello.Pern_Merch_Website.repository.CartItemRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.OrderItemRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProductRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return toProductDTOList(products);
    }

    public List<ProductVariantDTO> getAllVariants() {
        List<ProductVariant> variants = variantRepository.findAll();
        return toVariantDTOList(variants);
    }

    public List<ProductDTO> getProductsByCategory(String category) {
        List<Product> products = productRepository.findByCategory(category);
        return toProductDTOList(products);
    }

    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Product not found"));
        return toProductDTO(product);
    }

    public ProductVariantDTO getVariant(Long id) {
        ProductVariant variant = variantRepository.findById(id).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "variant not found"));
        return toVariantDTO(variant);
    }

    public ProductDTO createProduct(ProductDTO dto) {
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl()).build();
        return toProductDTO(productRepository.save(product));
    }

    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Product not found"));
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        return toProductDTO(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public ProductVariantDTO addVariant(Long productId, ProductVariantDTO variantDTO) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Product not found"));
        for (ProductVariant existing : product.getVariants()) {
            if (existing.getSize().equalsIgnoreCase(variantDTO.getSize())) {
                throw new AppException(HttpStatus.CONFLICT, "A '" + variantDTO.getSize() + "' variant already exists for this product");
            }
        }
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .size(variantDTO.getSize())
                .stockQuantity(variantDTO.getStockQuantity()).build();
        product.getVariants().add(variant);
        productRepository.save(product);
        return toVariantDTO(variant);
    }

    public ProductVariantDTO updateVariant(Long variantId, ProductVariantDTO dto) {
        ProductVariant variant = variantRepository.findById(variantId).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Variant not found"));
        variant.setSize(dto.getSize());
        variant.setStockQuantity(dto.getStockQuantity());
        // maybe update product. I feel like this is not necessary
        variantRepository.save(variant);
        return toVariantDTO(variant);
    }

    public void deleteVariant(Long variantId) {
        if (orderItemRepository.existsByVariantId(variantId)) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot delete a variant that is part of an existing order");
        }
        if (cartItemRepository.existsByVariantId(variantId)) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot delete a variant that is currently in a customer's cart");
        }
        variantRepository.deleteById(variantId);
    }

    private List<ProductDTO> toProductDTOList(List<Product> products) {
        List<ProductDTO> productDTOS = new ArrayList<>();
        for (Product product : products) {
            ProductDTO productDTO = toProductDTO(product);
            productDTOS.add(productDTO);
        }
        return productDTOS;
    }

    private ProductDTO toProductDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .variants(toVariantDTOList(product.getVariants())).build();
    }

    private List<ProductVariantDTO> toVariantDTOList(List<ProductVariant> variants) {
        List<ProductVariantDTO> variantDTOS = new ArrayList<>();
        if (variants == null) return variantDTOS;
        for (ProductVariant variant : variants) {
            ProductVariantDTO variantDTO = toVariantDTO(variant);
            variantDTOS.add(variantDTO);
        }
        return variantDTOS;
    }

    private ProductVariantDTO toVariantDTO(ProductVariant variant) {
        return ProductVariantDTO.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .size(variant.getSize())
                .stockQuantity(variant.getStockQuantity()).build();
    }
}
