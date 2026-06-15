package com.campus.bookborrow.service;

import com.campus.bookborrow.entity.BookReview;
import java.util.List;
import java.util.Map;

public interface ReviewService {
    void addReview(Long userId, Long bookId, String content, Integer rating);
    List<BookReview> getReviews(Long bookId);
    void deleteReview(Long id, Long userId);
    Map<String,Object> getReviewStats(Long bookId);
}
