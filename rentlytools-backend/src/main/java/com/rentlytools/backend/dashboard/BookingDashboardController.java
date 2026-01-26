package com.rentlytools.backend.dashboard;

import com.rentlytools.backend.core.booking.Booking;
import com.rentlytools.backend.core.booking.BookingRepository;
import com.rentlytools.backend.core.listing.Listing;
import com.rentlytools.backend.core.listing.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class BookingDashboardController {

    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;

    @GetMapping("/recent-bookings")
    public List<Map<String, Object>> getRecentBookings(@RequestParam Long userId) {

        List<Booking> bookings = bookingRepository
                .findByRenterId(userId)
                .stream()
                .filter(b -> !b.getStatus().name().equals("CAN"))
                .filter(b -> !b.getStatus().name().equals("DECL"))
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .limit(5)
                .toList();

        List<Map<String, Object>> response = new ArrayList<>();

        for (Booking b : bookings) {

            Listing listing = listingRepository.findById(b.getListingId()).orElse(null);

            Map<String, Object> obj = new HashMap<>();
            obj.put("id", b.getId());
            obj.put("title", listing != null ? listing.getTitle() : "Unknown");
            obj.put("price", b.getTotalPrice() != null ? b.getTotalPrice() : 0);
            obj.put("startAt", b.getStartAt());
            obj.put("endAt", b.getEndAt());
            obj.put("status", b.getStatus().name());
            obj.put("createdAt", b.getCreatedAt());

            response.add(obj);
        }

        return response;
    }
}