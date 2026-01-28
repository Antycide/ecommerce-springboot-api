package org.example.ecommerce.UnitTests;

import org.example.ecommerce.DTO.AddReviewDto;
import org.example.ecommerce.DTO.ShowReviewDto;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Mappers.ReviewMapper;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.Review;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.ReviewRepository;
import org.example.ecommerce.Service.ReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test123");

        var auth = new UsernamePasswordAuthenticationToken(user, "test123");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addReview_whenProductMissing_throwsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> reviewService.addReview(1L, new AddReviewDto("ok")));
    }

    @Test
    void addReview_whenValid_savesAndReturnsDto() {
        Product product = new Product();
        product.setId(1L);
        Review review = new Review();
        ShowReviewDto dto = new ShowReviewDto(1L, "ok", "test123");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewMapper.reviewDtoToReview(any(AddReviewDto.class))).thenReturn(review);
        when(reviewMapper.reviewToShowReviewDto(review)).thenReturn(dto);

        ShowReviewDto result = reviewService.addReview(1L, new AddReviewDto("ok"));

        assertThat(result).isEqualTo(dto);
        assertThat(review.getUser()).isEqualTo(user);
        assertThat(review.getProduct()).isEqualTo(product);
        verify(reviewRepository).save(review);
    }

    @Test
    void getAllReviews_whenProductMissing_throwsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> reviewService.getAllReviews(1L, PageRequest.of(0, 10)));
    }

    @Test
    void getAllReviews_whenProductExists_returnsMappedPage() {
        Product product = new Product();
        product.setId(1L);
        Review review = new Review();
        ShowReviewDto dto = new ShowReviewDto(1L, "ok", "test123");

        Page<Review> page = new PageImpl<>(List.of(review), PageRequest.of(0, 10), 1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProduct(product, PageRequest.of(0, 10))).thenReturn(page);
        when(reviewMapper.reviewToShowReviewDto(review)).thenReturn(dto);

        Page<ShowReviewDto> result = reviewService.getAllReviews(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(dto);
    }
}
