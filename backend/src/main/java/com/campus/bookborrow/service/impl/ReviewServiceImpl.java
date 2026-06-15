package com.campus.bookborrow.service.impl;

import com.campus.bookborrow.entity.BookReview;
import com.campus.bookborrow.exception.BusinessException;
import com.campus.bookborrow.mapper.BookReviewMapper;
import com.campus.bookborrow.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final BookReviewMapper reviewMapper;

    @Override @Transactional(rollbackFor = Exception.class)
    public void addReview(Long userId, Long bookId, String content, Integer rating) {
        if (content == null || content.trim().isEmpty()) throw new BusinessException(400, "评论内容不能为空");
        if (rating == null || rating < 1 || rating > 5) throw new BusinessException(400, "评分须为1-5");
        BookReview r = new BookReview();
        r.setUserId(userId); r.setBookId(bookId);
        r.setContent(content.trim()); r.setRating(rating);
        reviewMapper.insert(r);
    }

    @Override
    public List<BookReview> getReviews(Long bookId) {
        return reviewMapper.selectByBookId(bookId);
    }

    @Override @Transactional
    public void deleteReview(Long id, Long userId) {
        if (reviewMapper.deleteById(id, userId) == 0)
            throw new BusinessException(400, "无权删除该评论");
    }

    @Override
    public Map<String,Object> getReviewStats(Long bookId) {
        Map<String,Object> m = new HashMap<>();
        m.put("count", reviewMapper.countByBookId(bookId));
        m.put("avgRating", reviewMapper.avgRating(bookId));
        return m;
    }
}
