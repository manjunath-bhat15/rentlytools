package com.rentlytools.backend.core.wallet;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletRepository walletRepo;
    private final WalletTransactionRepository txnRepo;

    public WalletController(WalletRepository walletRepo, WalletTransactionRepository txnRepo) {
        this.walletRepo = walletRepo;
        this.txnRepo = txnRepo;
    }
    @GetMapping("/balance")
    public Object getBalance(@RequestParam Long userId) {
        var walletOpt = walletRepo.findByUserId(userId);
        if (walletOpt.isEmpty()) {
            return Map.of("error", "Wallet not found for this user");
        }

        var wallet = walletOpt.get();
        return Map.of(
                "userId", wallet.getUserId(),
                "balance", wallet.getBalance(),
                "walletId", wallet.getId()
        );
    }

    @PostMapping("/credit")
    public Object credit(@RequestParam Long userId, @RequestParam Double amount) {
        var walletOpt = walletRepo.findByUserId(userId);
        if (walletOpt.isEmpty()) return error("Wallet not found");

        var wallet = walletOpt.get();
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepo.save(wallet);

        WalletTransaction t = new WalletTransaction();
        t.setUserId(userId);
        t.setType(WalletTransaction.Type.CREDIT);
        t.setAmount(amount);
        t.setMeta("{\"note\":\"Manual credit\"}");
        txnRepo.save(t);

        return success("Wallet credited", wallet.getBalance());
    }

    @PostMapping("/debit")
    public Object debit(@RequestParam Long userId, @RequestParam Double amount) {
        var walletOpt = walletRepo.findByUserId(userId);
        if (walletOpt.isEmpty()) return error("Wallet not found");

        var wallet = walletOpt.get();
        if (wallet.getBalance() < amount) return error("Insufficient balance");

        wallet.setBalance(wallet.getBalance() - amount);
        walletRepo.save(wallet);

        WalletTransaction t = new WalletTransaction();
        t.setUserId(userId);
        t.setType(WalletTransaction.Type.DEBIT);
        t.setAmount(amount);
        t.setMeta("{\"note\":\"Manual debit\"}");
        txnRepo.save(t);

        return success("Wallet debited", wallet.getBalance());
    }

    @GetMapping("/transactions")
    public Object transactions(@RequestParam Long userId) {
        return txnRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private Map<String, Object> success(String msg, Double balance) {
        Map<String, Object> m = new HashMap<>();
        m.put("message", msg);
        m.put("balance", balance);
        return m;
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", msg);
        return m;
    }

}