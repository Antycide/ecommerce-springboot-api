package org.example.ecommerce.Repository;

import org.example.ecommerce.Model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductName(String productName);

    @Override
    Optional<Product> findById(Long productId);

    boolean existsByProductName(String productName);

    @Override
    Page<Product> findAll(Pageable pageable);

    @Override
    void deleteById(Long productId);

    @Override
    boolean existsById(Long productId);

    @Modifying
    @Query("UPDATE Product p SET p.availableQuantity = p.availableQuantity - :qty WHERE p.id = :id AND p.availableQuantity >= :qty")
    int decreaseAvailableQuantity(Long id, int qty);
}
