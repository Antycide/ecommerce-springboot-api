package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.AddReviewDto;
import org.example.ecommerce.Mappers.ReviewMapper;
import org.example.ecommerce.DTO.ShowReviewDto;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.Review;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    //TODO refactor these methods
    // Create a method that gets single review by id from a product

    @Transactional
    public ShowReviewDto addReview(Long productId, AddReviewDto reviewDto) {

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException("Product with id " + productId + " does not exist"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Review review = reviewMapper.reviewDtoToReview(reviewDto);
        review.setUser(user);
        review.setProduct(product);
        reviewRepository.save(review);

        product.getReviews().add(review);

        return reviewMapper.reviewToShowReviewDto(review);
    }

    @Transactional(readOnly = true)
    public Page<ShowReviewDto> getAllReviews(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException("Product with id " + productId + " does not exist"));

        Page<Review> reviews = reviewRepository.findByProduct(product, pageable);

        return reviews.map(reviewMapper::reviewToShowReviewDto);
    }
}
