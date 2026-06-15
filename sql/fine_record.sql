-- ============================================================
-- 罚金记录表 fine_record
-- 说明：记录每次借阅逾期产生的罚金及缴费状态
-- ============================================================
DROP TABLE IF EXISTS fine_record;
CREATE TABLE fine_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '罚金记录ID，主键',
    borrow_id       BIGINT          NOT NULL                 COMMENT '关联借阅记录ID',
    user_id         BIGINT          NOT NULL                 COMMENT '用户ID',
    book_id         BIGINT          NOT NULL                 COMMENT '图书ID',
    overdue_days    INT             NOT NULL DEFAULT 0       COMMENT '逾期天数',
    fine_amount     DECIMAL(10, 2)  NOT NULL DEFAULT 0.00    COMMENT '罚金金额（元）',
    payment_status  TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '缴费状态：0-未缴，1-已缴',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_borrow_id (borrow_id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    KEY idx_payment_status (payment_status),
    CONSTRAINT fk_fine_borrow FOREIGN KEY (borrow_id) REFERENCES borrow_record(id),
    CONSTRAINT fk_fine_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_fine_book FOREIGN KEY (book_id) REFERENCES book_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='罚金记录表';
