package com.rentlytools.backend.dashboard;

import com.rentlytools.backend.core.booking.BookingRepository;
import com.rentlytools.backend.core.wallet.WalletRepository;
import com.rentlytools.backend.core.listing.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private final BookingRepository bookingRepo;
    private final WalletRepository walletRepo;
    private final ListingRepository listingRepo;

    public DashboardStatsResponse getStats(Long userId) {

        // 🔵 Active bookings count
        long activeBookings = bookingRepo.countByRenterId(userId);

        // 🟢 Wallet Balance
        double walletBalance = walletRepo.findByUserId(userId)
                .map(w -> w.getBalance())
                .orElse(0.0);

        // 🟡 Pending requests (owner side)
        long pendingRequests = listingRepo.findByOwnerId(userId)
                .stream()
                .flatMap(listing -> bookingRepo
                        .findByListingIdIn(
                                listingRepo.findByOwnerId(userId)
                                        .stream()
                                        .map(l -> l.getId())
                                        .toList()
                        )
                        .stream()
                )
                .filter(b -> b.getStatus().name().equals("REQ"))
                .count();

        // 🔴 Unread messages (0 for now)
        long unreadMessages = 0;

        return new DashboardStatsResponse(
                activeBookings,
                walletBalance,
                pendingRequests,
                unreadMessages
        );
    }
}