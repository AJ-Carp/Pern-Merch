package com.ajcarpinello.Pern_Merch_Website.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_stripe_events")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ProcessedStripeEvent {
    
    @Id
    private String eventId;
    private String eventType;
    private LocalDateTime processedAt;
}