package com.ajcarpinello.Pern_Merch_Website.config;

import com.ajcarpinello.Pern_Merch_Website.entity.Product;
import com.ajcarpinello.Pern_Merch_Website.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// creates sample products for development only

@Profile("dev")
@Component
@RequiredArgsConstructor
@Order(2)
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            productRepository.save(Product.builder()
                    .name("PERN Logo Tee")
                    .description("Classic black tee with the iconic PERN logo. 100% cotton.")
                    .price(new BigDecimal("29.99"))
                    .category("T-Shirts")
                    .size("M")
                    .imageUrl("https://via.placeholder.com/400x400/1a1a2e/e94560?text=PERN+Logo+Tee")
                    .stockQuantity(50)
                    .build());

            productRepository.save(Product.builder()
                    .name("PERN Tour 2026 Tee")
                    .description("Limited edition tour shirt. Cities on the back.")
                    .price(new BigDecimal("34.99"))
                    .category("T-Shirts")
                    .size("L")
                    .imageUrl("https://via.placeholder.com/400x400/16213e/e94560?text=Tour+2026")
                    .stockQuantity(30)
                    .build());

            productRepository.save(Product.builder()
                    .name("PERN Skull Hoodie")
                    .description("Heavyweight pullover hoodie with skull artwork.")
                    .price(new BigDecimal("59.99"))
                    .category("Hoodies")
                    .size("L")
                    .imageUrl("https://via.placeholder.com/400x400/0f3460/e94560?text=Skull+Hoodie")
                    .stockQuantity(25)
                    .build());

            productRepository.save(Product.builder()
                    .name("PERN Zip-Up Hoodie")
                    .description("Black zip-up hoodie with embroidered PERN patch.")
                    .price(new BigDecimal("64.99"))
                    .category("Hoodies")
                    .size("XL")
                    .imageUrl("https://via.placeholder.com/400x400/1a1a2e/53d8fb?text=Zip+Hoodie")
                    .stockQuantity(20)
                    .build());

            productRepository.save(Product.builder()
                    .name("Debut Album - Vinyl")
                    .description("\"Into the Stack\" debut album on 180g black vinyl.")
                    .price(new BigDecimal("24.99"))
                    .category("Vinyl")
                    .size("ONE_SIZE")
                    .imageUrl("https://via.placeholder.com/400x400/533483/e94560?text=Vinyl+LP")
                    .stockQuantity(40)
                    .build());

            productRepository.save(Product.builder()
                    .name("PERN Live Poster")
                    .description("18x24 concert poster from the Overflow Tour.")
                    .price(new BigDecimal("14.99"))
                    .category("Posters")
                    .size("ONE_SIZE")
                    .imageUrl("https://via.placeholder.com/400x400/e94560/1a1a2e?text=Live+Poster")
                    .stockQuantity(100)
                    .build());

            productRepository.save(Product.builder()
                    .name("PERN Enamel Pin Set")
                    .description("Set of 3 collectible enamel pins.")
                    .price(new BigDecimal("12.99"))
                    .category("Accessories")
                    .size("ONE_SIZE")
                    .imageUrl("https://via.placeholder.com/400x400/16213e/53d8fb?text=Pin+Set")
                    .stockQuantity(75)
                    .build());

            productRepository.save(Product.builder()
                    .name("PERN Beanie")
                    .description("Embroidered knit beanie. One size fits all.")
                    .price(new BigDecimal("19.99"))
                    .category("Accessories")
                    .size("ONE_SIZE")
                    .imageUrl("https://via.placeholder.com/400x400/0f3460/53d8fb?text=Beanie")
                    .stockQuantity(40)
                    .build());

            System.out.println(">>> Seeded 8 dummy products (dev profile)");
        }
    }
}
