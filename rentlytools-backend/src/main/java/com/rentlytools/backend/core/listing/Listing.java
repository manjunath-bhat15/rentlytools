package com.rentlytools.backend.core.listing;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "category_id")
    private Long categoryId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double lat;
    private Double lng;

    @Column(name = "price_per_day")
    private Double pricePerDay;

    @Column(name = "deposit_amount")
    private Double depositAmount;

    @Column(name = "avg_rating")
    private Double avgRating;

    private String status;
}