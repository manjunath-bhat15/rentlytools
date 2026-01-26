package com.rentlytools.backend.core.wallet;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "wallet_accounts")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private Double balance = 0.0;
}