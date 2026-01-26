package com.rentlytools.backend.core.booking;

import com.rentlytools.backend.core.listing.ListingRepository;
import com.rentlytools.backend.core.user.UserRepository;
import com.rentlytools.backend.core.wallet.WalletService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepo;
    private final ListingRepository listingRepo;
    private final UserRepository userRepo;
    private final WalletService walletService;

    public BookingController(BookingRepository bookingRepo,
                             ListingRepository listingRepo,
                             UserRepository userRepo,
                             WalletService walletService) {
        this.bookingRepo = bookingRepo;
        this.listingRepo = listingRepo;
        this.userRepo = userRepo;
        this.walletService = walletService;
    }

    // --------------------------- REQUEST BOOKING ---------------------------
    @PostMapping("/request")
    public Object request(@RequestBody BookingRequest req) {

        var listingOpt = listingRepo.findById(req.listingId());
        if (listingOpt.isEmpty()) return error("LISTING NOT FOUND");

        var renterOpt = userRepo.findById(req.renterId());
        if (renterOpt.isEmpty()) return error("RENTER NOT FOUND");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime start = LocalDateTime.parse(req.startAt(), fmt);
        LocalDateTime end = LocalDateTime.parse(req.endAt(), fmt);

        Booking b = new Booking();
        b.setListingId(req.listingId());
        b.setRenterId(req.renterId());
        b.setStartAt(start);   // ✅ fixed
        b.setEndAt(end);       // ✅ fixed
        b.setStatus(Booking.Status.REQ);

        bookingRepo.save(b);
        return success("booking request submitted", b.getId());
    }

    // --------------------------- OWNER DECISION ---------------------------
    @PostMapping("/owner/decide")
    public Object ownerDecision(
            @RequestParam Long ownerId,
            @RequestParam Long bookingId,
            @RequestParam String action
    ) {
        var bOpt = bookingRepo.findById(bookingId);
        if (bOpt.isEmpty()) return error("BOOKING NOT FOUND");

        var b = bOpt.get();
        var listingOpt = listingRepo.findById(b.getListingId());
        if (listingOpt.isEmpty()) return error("LISTING NOT FOUND");

        var listing = listingOpt.get();

        if (!listing.getOwnerId().equals(ownerId))
            return error("NOT OWNER OF THIS LISTING");

        if (!b.getStatus().equals(Booking.Status.REQ))
            return error("BOOKING NOT IN REQUEST STATUS");

        if (action.equalsIgnoreCase("APPROVE")) {
            try {
                Long holdTxnId = walletService.holdAmount(b.getRenterId(), listing.getDepositAmount(), b.getId());
                b.setDepositHoldTxnId(holdTxnId); // ✅ fixed
                b.setStatus(Booking.Status.ACC);
                bookingRepo.save(b);
                return success("APPROVED ✅ Deposit held.", bookingId);
            } catch (Exception e) {
                return error("Wallet error: " + e.getMessage());
            }
        }

        if (action.equalsIgnoreCase("REJECT")) {
            b.setStatus(Booking.Status.DECL);
            bookingRepo.save(b);
            return success("REJECTED ❌", bookingId);
        }

        return error("INVALID ACTION");
    }

    // --------------------------- BOOKING STATUS ---------------------------
    @GetMapping("/status")
    public Object status(@RequestParam Long bookingId) {
        var bOpt = bookingRepo.findById(bookingId);
        if (bOpt.isEmpty()) return error("BOOKING NOT FOUND");

        var b = bOpt.get();
        Map<String, Object> res = new HashMap<>();
        res.put("bookingId", b.getId());
        res.put("status", b.getStatus());
        res.put("startAt", b.getStartAt());   // ✅ fixed
        res.put("endAt", b.getEndAt());       // ✅ fixed

        return res;
    }

    // --------------------------- RENTER CANCEL ---------------------------
    @PostMapping("/renter/cancel")
    public Object renterCancel(
            @RequestParam Long renterId,
            @RequestParam Long bookingId
    ) {
        var bOpt = bookingRepo.findById(bookingId);
        if (bOpt.isEmpty()) return error("BOOKING NOT FOUND");

        var b = bOpt.get();

        if (!b.getRenterId().equals(renterId))
            return error("NOT YOUR BOOKING");

        if (b.getStatus() != Booking.Status.REQ && b.getStatus() != Booking.Status.ACC)
            return error("CANNOT CANCEL AT THIS STAGE");

        b.setStatus(Booking.Status.CAN);
        bookingRepo.save(b);

        return success("BOOKING CANCELLED 🚫", b.getId());
    }

    // --------------------------- COMPLETE BOOKING ---------------------------
    // Inside BookingController.java
// Inside BookingController.java
@PostMapping("/complete")
public Object completeBooking(@RequestParam Long bookingId) {
    var b = bookingRepo.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
            
    var listing = listingRepo.findById(b.getListingId())
            .orElseThrow(() -> new RuntimeException("Listing not found"));

    try {
        // 1. Release Deposit to Renter
        walletService.releaseAmount(b.getRenterId(), listing.getDepositAmount(), b.getId(), "Deposit Released");

        // 2. Pay Rental Fee to Owner
        // You can calculate this based on days, or use a total_price field
        Double fee = b.getTotalPrice() != null ? b.getTotalPrice() : 0.0; 
        walletService.payOwner(b.getRenterId(), listing.getOwnerId(), fee, b.getId());

        // 3. Finalize
        b.setStatus(Booking.Status.COMP);
        bookingRepo.save(b);

        return success("Success! Deposit released and Owner credited.", b.getId());
    } catch (Exception e) {
        return error("Completion failed: " + e.getMessage());
    }
}

    // --------------------------- OWNER LISTINGS ---------------------------

    @GetMapping("/owner/pending")
    public Object getPending(@RequestParam Long ownerId) {
        return bookingRepo.findPendingByOwnerId(ownerId);
    }

    @GetMapping("/owner/active")
    public Object getActive(@RequestParam Long ownerId) {
        return bookingRepo.findActiveForOwner(ownerId);
    }

    @GetMapping("/owner/completed")
    public Object getCompleted(@RequestParam Long ownerId) {
        return bookingRepo.findCompletedForOwner(ownerId);
    }

    // --------------------------- HELPERS ---------------------------
    private Map<String, Object> error(String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", msg);
        return m;
    }

    private Map<String, Object> success(String msg, Long id) {
        Map<String, Object> m = new HashMap<>();
        m.put("message", msg);
        m.put("bookingId", id);
        return m;
    }
}