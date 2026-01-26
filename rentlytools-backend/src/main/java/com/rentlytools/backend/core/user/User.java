package com.rentlytools.backend.core.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Added unique = true and nullable = false to prevent duplicates/orphans
    @Column(unique = true, nullable = false)
    private String email;

    private String mobile;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role_user")
    private Boolean roleUser = true;

    @Column(name = "role_owner")
    private Boolean roleOwner = false;

    @Column(name = "role_admin")
    private Boolean roleAdmin = false;

    @Column(name = "default_lat")
    private Double lat;

    @Column(name = "default_lng")
    private Double lng;

    @Column(name = "address")
    private String address;
}