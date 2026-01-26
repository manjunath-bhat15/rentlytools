package com.rentlytools.backend.core.listing;

import com.rentlytools.backend.core.user.UserRepository;
import com.rentlytools.backend.core.category.CategoryRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingRepository listingRepo;
    private final ListingPhotoRepository photoRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    public ListingController(
            ListingRepository listingRepo,
            ListingPhotoRepository photoRepo,
            UserRepository userRepo,
            CategoryRepository categoryRepo
    ) {
        this.listingRepo = listingRepo;
        this.photoRepo = photoRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> add(@RequestBody ListingCreateRequest req) {
        log.info("New listing request: Title='{}', OwnerID={}, CategoryID={}", 
                 req.title(), req.ownerId(), req.categoryId());

        // 1. Validate owner/admin
        var userOpt = userRepo.findById(req.ownerId());
        if (userOpt.isEmpty()) {
            log.warn("Listing failed: Owner ID {} not found", req.ownerId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "INVALID OWNER"));
        }
        
        var user = userOpt.get();
        boolean canCreate = Boolean.TRUE.equals(user.getRoleOwner()) || Boolean.TRUE.equals(user.getRoleAdmin());
        if (!canCreate) {
            log.warn("Listing failed: User {} is not authorized as owner/admin", req.ownerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "NOT AUTHORIZED"));
        }

        // 2. Validate category
        if (req.categoryId() == null) {
            log.warn("Listing failed: Category ID is null");
            return ResponseEntity.badRequest().body(Map.of("error", "CATEGORY ID REQUIRED"));
        }

        if (categoryRepo.findById(req.categoryId()).isEmpty()) {
            log.error("Listing failed: Category ID {} does not exist in 'categories' table", req.categoryId());
            return ResponseEntity.badRequest().body(Map.of("error", "INVALID CATEGORY"));
        }

        try {
            // Build and save listing
            Listing l = new Listing();
            l.setOwnerId(req.ownerId());
            l.setCategoryId(req.categoryId());
            l.setTitle(req.title());
            l.setDescription(req.description());
            l.setLat(req.lat());
            l.setLng(req.lng());
            l.setPricePerDay(req.pricePerDay());
            l.setDepositAmount(req.depositAmount());
            l.setAvgRating(0.0);
            l.setStatus("PENDING");

            listingRepo.save(l);
            log.info("Listing saved with ID: {}", l.getId());

            // Save photos
            List<String> photos = req.photos();
            if (photos != null && !photos.isEmpty()) {
                for (int i = 0; i < photos.size(); i++) {
                    String url = photos.get(i);
                    if (url == null || url.isBlank()) continue;

                    ListingPhoto p = new ListingPhoto();
                    p.setListingId(l.getId());
                    p.setUrl(url);
                    p.setOrderIndex(i);
                    photoRepo.save(p);
                }
                log.debug("Saved {} photos for listing {}", photos.size(), l.getId());
            }

            return ResponseEntity.ok(Map.of(
                    "message", "listing submitted for approval",
                    "listingId", l.getId(),
                    "status", l.getStatus()
            ));

        } catch (Exception e) {
            log.error("Internal Error creating listing: ", e);
            return ResponseEntity.status(500).body(Map.of("error", "Server Error: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public List<Listing> all() {
        log.info("Fetching all listings");
        return listingRepo.findAll();
    }

    @GetMapping("/details")
    public ResponseEntity<?> details(@RequestParam Long id) {
        log.info("Fetching details for listing ID: {}", id);

        var lOpt = listingRepo.findById(id);
        if (lOpt.isEmpty()) {
            log.warn("Listing ID {} not found", id);
            return ResponseEntity.notFound().build();
        }

        var l = lOpt.get();
        var ph = photoRepo.findByListingIdOrderByOrderIndex(id)
                .stream()
                .map(ListingPhoto::getUrl)
                .toList();

        return ResponseEntity.ok(new ListingDetailsResponse(
                l.getId(), l.getOwnerId(), l.getCategoryId(),
                l.getTitle(), l.getDescription(), l.getLat(), l.getLng(),
                l.getPricePerDay(), l.getDepositAmount(), l.getAvgRating(),
                l.getStatus(), ph
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteListing(@PathVariable Long id) {
        log.info("Request to delete listing ID: {}", id);
        return listingRepo.findById(id)
                .map(listing -> {
                    listingRepo.delete(listing);
                    log.info("Listing ID {} deleted successfully", id);
                    return ResponseEntity.ok().body(Map.of("message", "Deleted successfully"));
                })
                .orElseGet(() -> {
                    log.warn("Delete failed: Listing ID {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/owner")
public ResponseEntity<?> byOwner(@RequestParam("ownerId") Long ownerId) {
    log.info("GET request for owner listings. OwnerID: {}", ownerId);
    List<Listing> listings = listingRepo.findByOwnerId(ownerId);
    return ResponseEntity.ok(listings);
}
}