package com.rentlytools.backend.dashboard;

import com.rentlytools.backend.core.booking.BookingRepository;
import com.rentlytools.backend.core.booking.Booking;
import com.rentlytools.backend.core.listing.ListingRepository;
import com.rentlytools.backend.core.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BookingRepository bookingRepo;
    private final ListingRepository listingRepo;
    private final WalletRepository walletRepo;

    // 1) Active bookings for a user
    public long getActiveBookings(Long userId) {
        return bookingRepo.findByRenterId(userId)
                .stream()
                .filter(b -> b.getStatus() == Booking.Status.ACTIVE)
                .count();
    }

    // 2) Pending booking requests for OWNER
    public long getPendingRequests(Long ownerId) {
        var ownerListings = listingRepo.findByOwnerId(ownerId);

        if (ownerListings.isEmpty()) return 0;

        var listingIds = ownerListings.stream()
                .map(l -> l.getId())
                .toList();

        return bookingRepo.findByListingIdIn(listingIds)
                .stream()
                .filter(b -> b.getStatus() == Booking.Status.REQ)
                .count();
    }

    // 3) Wallet Balance
    public double getWalletBalance(Long userId) {
        return walletRepo.findByUserId(userId)
                .map(w -> w.getBalance())
                .orElse(0.0);
    }

    // 4) Placeholder unread messages count
    // (You can replace with real chat later)
    public long getUnreadMessages(Long userId) {
        return 0L;  // always 0 now
    }

    // --- Combined Stats ---
    public DashboardStats getDashboardStats(Long userId) {

        long active = getActiveBookings(userId);
        long pending = getPendingRequests(userId);
        double wallet = getWalletBalance(userId);
        long unread = getUnreadMessages(userId);

        return new DashboardStats(active, wallet, pending, unread);
    }
}