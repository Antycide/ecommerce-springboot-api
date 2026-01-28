package org.example.ecommerce.Repository;

import org.example.ecommerce.Model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    @Override
    Optional<WishlistItem> findById(Long wishlistItemId);

    @Override
    void deleteById(Long wishlistItemId);
}
