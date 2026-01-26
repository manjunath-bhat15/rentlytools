package com.rentlytools.backend.core.booking;

import com.rentlytools.backend.core.booking.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT b
        FROM Booking b
        JOIN Listing l ON b.listingId = l.id
        WHERE l.ownerId = :ownerId
        ORDER BY b.createdAt DESC
    """)
    List<Booking> findAllByOwnerId(Long ownerId);

    @Query("""
        SELECT b
        FROM Booking b
        JOIN Listing l ON b.listingId = l.id
        WHERE l.ownerId = :ownerId
        AND b.status = 'REQ'
        ORDER BY b.createdAt DESC
    """)
    List<Booking> findPendingByOwnerId(Long ownerId);

    // Scheduler
    List<Booking> findByEndAtBeforeAndStatus(LocalDateTime endTime, Booking.Status status);

    // Dashboard (renter side)
    List<Booking> findByRenterId(Long renterId);

    // Stats
    List<Booking> findByListingIdIn(List<Long> listingIds);

    long countByRenterId(Long renterId);

    // ACTIVE BOOKINGS
    @Query("""
        SELECT b FROM Booking b
        JOIN Listing l ON b.listingId = l.id
        WHERE l.ownerId = :ownerId
        AND b.status IN ('ACC','ACTIVE')
        ORDER BY b.startAt ASC
    """)
    List<Booking> findActiveForOwner(Long ownerId);

    // COMPLETED BOOKINGS
    @Query("""
        SELECT b FROM Booking b
        JOIN Listing l ON b.listingId = l.id
        WHERE l.ownerId = :ownerId
        AND b.status = 'COMP'
        ORDER BY b.endAt DESC
    """)
    List<Booking> findCompletedForOwner(Long ownerId);
}