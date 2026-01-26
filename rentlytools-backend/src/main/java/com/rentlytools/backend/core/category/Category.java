package com.rentlytools.backend.core.category;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="categories")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String icon; // example: 🌿 or 🔧

    @Column(name="active", columnDefinition="boolean default true")
    private boolean active = true;
}
