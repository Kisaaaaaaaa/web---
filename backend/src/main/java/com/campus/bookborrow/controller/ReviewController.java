package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import com.campus.bookborrow.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/book/{bookId}")
    public Result<?> list(@PathVariable Long bookId) {
        return Result.ok(reviewService.getReviews(bookId));
    }

    @GetMapping("/stats/{bookId}")
    public Result<?> stats(@PathVariable Long bookId) {
        return Result.ok(reviewService.getReviewStats(bookId));
    }

    @PostMapping
    public Result<?> add(@RequestAttribute("userId") Long userId,
                         @RequestBody Map<String,Object> body) {
        Long bookId = Long.valueOf(body.get("bookId").toString());
        String content = (String) body.get("content");
        Integer rating = Integer.valueOf(body.get("rating").toString());
        reviewService.addReview(userId, bookId, content, rating);
        return Result.ok("评论成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@RequestAttribute("userId") Long userId,
                            @PathVariable Long id) {
        reviewService.deleteReview(id, userId);
        return Result.ok("删除成功");
    }
}
