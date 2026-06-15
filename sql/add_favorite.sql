-- 收藏表建表SQL（追加到 seed_books.sql 开头或单独执行）
CREATE TABLE IF NOT EXISTS user_favorite (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏ID，主键',
    user_id     BIGINT   NOT NULL                COMMENT '用户ID，关联 sys_user.id',
    book_id     BIGINT   NOT NULL                COMMENT '图书ID，关联 book_info.id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_book (user_id, book_id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_fav_book FOREIGN KEY (book_id) REFERENCES book_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';
