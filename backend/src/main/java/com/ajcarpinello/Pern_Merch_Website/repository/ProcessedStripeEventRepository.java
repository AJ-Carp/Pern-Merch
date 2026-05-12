package com.ajcarpinello.Pern_Merch_Website.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ajcarpinello.Pern_Merch_Website.entity.ProcessedStripeEvent;

public interface ProcessedStripeEventRepository extends JpaRepository<ProcessedStripeEvent, String> {
    
}