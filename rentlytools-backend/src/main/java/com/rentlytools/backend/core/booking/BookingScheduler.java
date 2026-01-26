package com.rentlytools.backend.core.booking;

import com.rentlytools.backend.core.listing.Listing;
import com.rentlytools.backend.core.listing.ListingRepository;
import com.rentlytools.backend.core.wallet.WalletService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingScheduler {

    private final BookingRepository bookingRepo;
    private final ListingRepository listingRepo;
    private final WalletService walletService;

    public BookingScheduler(BookingRepository bookingRepo, ListingRepository listingRepo, WalletService walletService) {
        this.bookingRepo = bookingRepo;
        this.listingRepo = listingRepo;
        this.walletService = walletService;
    }

    /**
     * Automatically completes expired bookings every 5 minutes
     * and releases held deposits from renters' wallets.
     */
    @Scheduled(fixedRate = 300000) // every 5 minutes (300,000 ms)
    public void autoCompleteExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[AUTO] Scheduler triggered at: " + now);

        // Find bookings that have ended but are still ACTIVE
        List<Booking> expiredBookings = bookingRepo.findByEndAtBeforeAndStatus(now, Booking.Status.ACTIVE);

        if (expiredBookings.isEmpty()) {
            System.out.println("[AUTO] No expired active bookings found ✅");
            return;
        }

        for (Booking booking : expiredBookings) {
            try {
                // Fetch the listing to get deposit amount
                Listing listing = listingRepo.findById(booking.getListingId()).orElse(null);
                if (listing == null) {
                    System.err.println("[AUTO] Listing not found for booking ID " + booking.getId());
                    continue;
                }

                // Mark as completed
                booking.setStatus(Booking.Status.COMP);
                bookingRepo.save(booking);

                // If deposit exists, release amount
                if (booking.getDepositHoldTxnId() != null) {
                    walletService.releaseAmount(
                            booking.getRenterId(),
                            listing.getDepositAmount(),
                            booking.getId(),
                            "Auto-release: booking completed"
                    );
                    System.out.println("[AUTO] Released deposit for booking ID " + booking.getId());
                }

                System.out.println("[AUTO] Booking ID " + booking.getId() + " marked as COMPLETED ✅");
            } catch (Exception e) {
                System.err.println("[AUTO][ERROR] Booking ID " + booking.getId() + ": " + e.getMessage());
            }
        }
    }
}
