package com.rentlytools.backend.core.wallet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class WalletService {

    private final WalletRepository walletRepo;
    private final WalletTransactionRepository txnRepo;

    public WalletService(WalletRepository walletRepo, WalletTransactionRepository txnRepo) {
        this.walletRepo = walletRepo;
        this.txnRepo = txnRepo;
    }

    /**
     * 🪙 Hold deposit
     * Subtracts amount from user balance and logs a HOLD transaction.
     */
    @Transactional
    public Long holdAmount(Long userId, Double amount, Long bookingId) {
        // 1. Retrieve the wallet from 'wallet_accounts'
        var walletOpt = walletRepo.findByUserId(userId);
        if (walletOpt.isEmpty()) {
            throw new RuntimeException("Wallet not found for user: " + userId);
        }
        
        var wallet = walletOpt.get();

        // 2. Strict Balance Check
        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance to hold deposit of ₹" + amount);
        }

        // 3. Update Balance
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepo.save(wallet);

        // 4. Log Transaction in 'wallet_transactions'
        WalletTransaction txn = new WalletTransaction();
        txn.setUserId(userId);
        txn.setType(WalletTransaction.Type.HOLD);
        txn.setAmount(amount);
        txn.setBookingId(bookingId);
        txn.setMeta("{\"action\":\"Deposit Hold\", \"bookingId\":" + bookingId + "}");
        txn.setCreatedAt(LocalDateTime.now()); // Ensure timestamp is set
        
        txnRepo.save(txn);

        return txn.getId();
    }

    /**
     * 💸 Release deposit
     * Adds amount back to user balance and logs a RELEASE transaction.
     */
    @Transactional
    public void releaseAmount(Long userId, Double amount, Long bookingId, String reason) {
        var walletOpt = walletRepo.findByUserId(userId);
        if (walletOpt.isEmpty()) {
            throw new RuntimeException("Wallet not found for user: " + userId);
        }
        
        var wallet = walletOpt.get();

        // 1. Add balance back
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepo.save(wallet);

        // 2. Log Release Transaction
        WalletTransaction txn = new WalletTransaction();
        txn.setUserId(userId);
        txn.setType(WalletTransaction.Type.RELEASE);
        txn.setAmount(amount);
        txn.setBookingId(bookingId);
        txn.setMeta("{\"action\":\"Deposit Release\", \"reason\":\"" + reason + "\"}");
        txn.setCreatedAt(LocalDateTime.now()); // Set timestamp
        
        txnRepo.save(txn);
    }

    // Inside WalletService.java
@Transactional
public void payOwner(Long renterId, Long ownerId, Double amount, Long bookingId) {
    // 1. Deduct from Renter
    var renterWallet = walletRepo.findByUserId(renterId)
            .orElseThrow(() -> new RuntimeException("Renter wallet not found"));
    
    if (renterWallet.getBalance() < amount) {
        throw new RuntimeException("Renter has insufficient balance for final payment");
    }
    renterWallet.setBalance(renterWallet.getBalance() - amount);
    walletRepo.save(renterWallet);

    // 2. Credit to Owner
    var ownerWallet = walletRepo.findByUserId(ownerId)
            .orElseThrow(() -> new RuntimeException("Owner wallet not found"));
    ownerWallet.setBalance(ownerWallet.getBalance() + amount);
    walletRepo.save(ownerWallet);

    // 3. Log Transactions
    WalletTransaction debit = new WalletTransaction();
    debit.setUserId(renterId);
    debit.setType(WalletTransaction.Type.DEBIT);
    debit.setAmount(amount);
    debit.setBookingId(bookingId);
    debit.setCreatedAt(LocalDateTime.now());
    txnRepo.save(debit);

    WalletTransaction credit = new WalletTransaction();
    credit.setUserId(ownerId);
    credit.setType(WalletTransaction.Type.CREDIT);
    credit.setAmount(amount);
    credit.setBookingId(bookingId);
    credit.setCreatedAt(LocalDateTime.now());
    txnRepo.save(credit);
}


}