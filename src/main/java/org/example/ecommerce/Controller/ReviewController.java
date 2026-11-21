package org.example.ecommerce.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.AddReviewDto;
import org.example.ecommerce.DTO.ShowReviewDto;
import org.example.ecommerce.Service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ShowReviewDto> addReview(@PathVariable Long productId, @Valid @RequestBody AddReviewDto reviewDto) {
        ShowReviewDto showReviewDto = reviewService.addReview(productId, reviewDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(showReviewDto);
    }

    @GetMapping
    public ResponseEntity<Page<ShowReviewDto>> getAllReviewsOfProduct(@PathVariable Long productId, @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAllReviews(productId, pageable));
    }


}
