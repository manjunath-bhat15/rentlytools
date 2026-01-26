package com.rentlytools.backend.core.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    // Find listings owned by a specific owner
    List<Listing> findByOwnerId(Long ownerId);

    // Find listings within latitude/longitude range
    List<Listing> findByLatBetweenAndLngBetween(double minLat, double maxLat, double minLng, double maxLng);

    // Optional: find active listings only (if you use status)
    List<Listing> findByStatus(String status);
}