package com.campus.bookborrow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFavorite {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime createTime;
}
