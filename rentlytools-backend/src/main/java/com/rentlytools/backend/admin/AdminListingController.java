package com.rentlytools.backend.admin;

import com.rentlytools.backend.core.listing.Listing;
import com.rentlytools.backend.core.listing.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/listings")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AdminListingController {

    private final ListingRepository listingRepo;

    // APPROVE LISTING
    @PostMapping("/approve")
    public ResponseEntity<?> approve(@RequestParam Long id) {
        var opt = listingRepo.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("error", "Listing not found"));

        Listing l = opt.get();
        l.setStatus("APPROVED");
        listingRepo.save(l);

        return ResponseEntity.ok(Map.of(
                "message", "Listing approved",
                "id", id
        ));
    }

    // REJECT LISTING
    @PostMapping("/reject")
    public ResponseEntity<?> reject(@RequestParam Long id) {
        var opt = listingRepo.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("error", "Listing not found"));

        Listing l = opt.get();
        l.setStatus("REJECTED");
        listingRepo.save(l);

        return ResponseEntity.ok(Map.of(
                "message", "Listing rejected",
                "id", id
        ));
    }

    // GET ALL PENDING
    @GetMapping("/pending")
    public Object pending() {
        return listingRepo.findByStatus("PENDING");
    }
}