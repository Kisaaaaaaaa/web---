package com.campus.bookborrow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class BookReview {
    private Long id;
    private Long userId;
    private Long bookId;
    private String content;
    private Integer rating;
    private LocalDateTime createTime;

    // 以下为联查字段
    private String username;
    private String realName;
}
