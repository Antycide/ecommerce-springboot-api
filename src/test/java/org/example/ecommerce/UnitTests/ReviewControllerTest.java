package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Controller.ReviewController;
import org.example.ecommerce.DTO.ShowReviewDto;
import org.example.ecommerce.Exception.GlobalExceptionHandler;
import org.example.ecommerce.Service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void addReview_returnsCreated() throws Exception {
        ShowReviewDto dto = new ShowReviewDto(1L, "Great", "test123");
        when(reviewService.addReview(any(Long.class), any())).thenReturn(dto);

        mockMvc.perform(post("/api/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "reviewText": "Great"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/products/1/reviews/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reviewText").value("Great"));
    }

    @Test
    void getAllReviews_returnsPage() throws Exception {
        ShowReviewDto dto = new ShowReviewDto(1L, "Great", "test123");
        when(reviewService.getAllReviews(1L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/products/1/reviews?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].reviewText").value("Great"));
    }
}
