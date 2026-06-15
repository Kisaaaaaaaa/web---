package com.campus.bookborrow.mapper;

import com.campus.bookborrow.entity.BookReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BookReviewMapper {
    int insert(BookReview review);
    List<BookReview> selectByBookId(@Param("bookId") Long bookId);
    int countByBookId(@Param("bookId") Long bookId);
    Double avgRating(@Param("bookId") Long bookId);
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}
