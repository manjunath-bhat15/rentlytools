package com.rentlytools.backend.verification.owner;

import com.rentlytools.backend.core.booking.Booking;
import com.rentlytools.backend.core.booking.BookingRepository;
import com.rentlytools.backend.core.listing.ListingRepository;
import com.rentlytools.backend.core.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/owner/bookings")
@RequiredArgsConstructor
public class OwnerBookingController {

    private final BookingRepository bookingRepo;
    private final ListingRepository listingRepo;
    private final UserRepository userRepo;

    // ⭐ PENDING BOOKINGS FOR OWNER
    @GetMapping("/pending")
    public List<Map<String, Object>> pending(@RequestParam Long ownerId) {

        var bookings = bookingRepo.findPendingByOwnerId(ownerId);
        List<Map<String, Object>> output = new ArrayList<>();

        for (Booking b : bookings) {

            var listing = listingRepo.findById(b.getListingId()).orElse(null);
            var renter = userRepo.findById(b.getRenterId()).orElse(null);

            Map<String, Object> m = new HashMap<>();
            m.put("bookingId", b.getId());
            m.put("status", b.getStatus());
            m.put("startAt", b.getStartAt());
            m.put("endAt", b.getEndAt());

            if (listing != null) {
                m.put("listingTitle", listing.getTitle());
                m.put("pricePerDay", listing.getPricePerDay());
                m.put("deposit", listing.getDepositAmount());
            }

            if (renter != null) {
                m.put("renterName", renter.getName());
                m.put("renterMobile", renter.getMobile());
                m.put("renterEmail", renter.getEmail());
            }

            output.add(m);
        }

        return output;
    }
    // ⭐ ACTIVE BOOKINGS
@GetMapping("/active")
public List<Map<String, Object>> active(@RequestParam Long ownerId) {
    var bookings = bookingRepo.findActiveForOwner(ownerId);
    List<Map<String, Object>> out = new ArrayList<>();

    for (Booking b : bookings) {
        var listing = listingRepo.findById(b.getListingId()).orElse(null);
        var renter = userRepo.findById(b.getRenterId()).orElse(null);

        Map<String, Object> m = new HashMap<>();
        m.put("bookingId", b.getId());
        m.put("status", b.getStatus());
        m.put("startAt", b.getStartAt());
        m.put("endAt", b.getEndAt());

        if (listing != null) {
            m.put("listingTitle", listing.getTitle());
            m.put("pricePerDay", listing.getPricePerDay());
        }

        if (renter != null) {
            m.put("renterName", renter.getName());
            m.put("renterMobile", renter.getMobile());
            m.put("renterEmail", renter.getEmail());
        }

        out.add(m);
    }

    return out;
}


// ⭐ COMPLETED BOOKINGS
@GetMapping("/completed")
public List<Map<String, Object>> completed(@RequestParam Long ownerId) {
    var bookings = bookingRepo.findCompletedForOwner(ownerId);
    List<Map<String, Object>> out = new ArrayList<>();

    for (Booking b : bookings) {
        var listing = listingRepo.findById(b.getListingId()).orElse(null);

        Map<String, Object> m = new HashMap<>();
        m.put("bookingId", b.getId());
        m.put("status", b.getStatus());
        m.put("startAt", b.getStartAt());
        m.put("endAt", b.getEndAt());
        m.put("listingTitle", listing != null ? listing.getTitle() : "Unknown");

        out.add(m);
    }

    return out;
}
}