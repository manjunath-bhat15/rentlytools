package com.rentlytools.backend.core.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, Long> {
    List<ListingPhoto> findByListingIdOrderByOrderIndex(Long listingId);
}