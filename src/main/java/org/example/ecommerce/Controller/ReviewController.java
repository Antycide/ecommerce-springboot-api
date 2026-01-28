package org.example.ecommerce.Controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.AddReviewDto;
import org.example.ecommerce.DTO.ShowReviewDto;
import org.example.ecommerce.Service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ShowReviewDto> addReview(@PathVariable Long productId, @RequestBody AddReviewDto reviewDto) {
        ShowReviewDto showReviewDto = reviewService.addReview(productId, reviewDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(showReviewDto.id())
                .toUri();

        return ResponseEntity.created(location).body(showReviewDto);
    }

    @GetMapping
    public ResponseEntity<Page<ShowReviewDto>> getAllReviewsOfProduct(@PathVariable Long productId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAllReviews(productId, pageable));
    }


}
