package com.rentlytools.backend.core.listing;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "listing_photos")
public class ListingPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id")
    private Long listingId;

    private String url;

    @Column(name = "order_index")
    private Integer orderIndex;
}