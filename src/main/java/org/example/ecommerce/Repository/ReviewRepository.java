package org.example.ecommerce.Repository;

import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Override
    Optional<Review> findById(Long aLong);

    Page<Review> findByProduct(Product product, Pageable pageable);
}
