package org.example.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 255 = length of the review_text column
public record AddReviewDto(@NotBlank(message = "Review text cannot be empty")
                           @Size(max = 255, message = "Review text cannot be longer than 255 characters") String reviewText) {
}
