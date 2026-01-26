package com.rentlytools.backend.core.listing;

import com.rentlytools.backend.core.user.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listings/admin")
public class ListingAdminController {

    private final ListingRepository listingRepo;
    private final UserRepository userRepo;

    public ListingAdminController(
            ListingRepository listingRepo,
            UserRepository userRepo
    ) {
        this.listingRepo = listingRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/approve")
public String approve(@RequestParam Long adminId, @RequestParam Long listingId) {

    var user = userRepo.findById(adminId).orElse(null);
    if (user == null || !Boolean.TRUE.equals(user.getRoleAdmin()))
        return "NOT ADMIN";

    var listing = listingRepo.findById(listingId).orElse(null);
    if (listing == null) return "LISTING NOT FOUND";

    listing.setStatus("ACTIVE");   // << change here
    listingRepo.save(listing);

    return "Listing is now ACTIVE ✅";
}

    @PostMapping("/reject")
    public String reject(@RequestParam Long adminId, @RequestParam Long listingId) {

        var user = userRepo.findById(adminId).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getRoleAdmin()))
            return "NOT ADMIN";

        var listing = listingRepo.findById(listingId).orElse(null);
        if (listing == null) return "LISTING NOT FOUND";

        listing.setStatus("REJECTED");
        listingRepo.save(listing);

        return "Listing rejected ❌";
    }
}
