package org.example.ecommerce.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "wishlist")
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(mappedBy = "wishlist", orphanRemoval = true)
    private User user;

    @OneToMany(mappedBy = "wishlist", orphanRemoval = true)
    private Set<WishlistItem> wishlistItems = new LinkedHashSet<>();

}