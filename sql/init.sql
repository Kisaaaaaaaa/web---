-- ============================================================
-- 校园图书借阅自助管理系统 - 数据库初始化脚本
-- 数据库版本：MySQL 8.0
-- 字符集：utf8mb4
-- 引擎：InnoDB
-- ============================================================

-- 创建数据库
DROP DATABASE IF EXISTS campus_book_borrow;
CREATE DATABASE campus_book_borrow
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE campus_book_borrow;

-- ============================================================
-- 1. 角色表 sys_role
-- 说明：存储系统角色信息（如管理员、学生）
-- ============================================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '角色ID，主键',
    role_name   VARCHAR(50)  NOT NULL                 COMMENT '角色名称（如：系统管理员、学生）',
    role_code   VARCHAR(50)  NOT NULL                 COMMENT '角色编码（如：ROLE_ADMIN, ROLE_STUDENT）',
    description VARCHAR(200) DEFAULT NULL             COMMENT '角色描述',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- ============================================================
-- 2. 用户表 sys_user
-- 说明：存储管理员和学生用户信息
-- ============================================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID，主键',
    username    VARCHAR(50)  NOT NULL                 COMMENT '用户名（登录账号）',
    password    VARCHAR(255) NOT NULL                 COMMENT '登录密码（BCrypt加密存储）',
    real_name   VARCHAR(50)  NOT NULL                 COMMENT '真实姓名',
    student_no  VARCHAR(30)  DEFAULT NULL             COMMENT '学号（学生角色必填，管理员可为空）',
    phone       VARCHAR(20)  DEFAULT NULL             COMMENT '手机号码',
    email       VARCHAR(100) DEFAULT NULL             COMMENT '电子邮箱',
    role_id     BIGINT       NOT NULL                 COMMENT '角色ID，关联 sys_role.id',
    status      TINYINT(1)   NOT NULL DEFAULT 1       COMMENT '账号状态：0-禁用，1-启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_role_id (role_id),
    KEY idx_real_name (real_name),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ============================================================
-- 3. 图书分类表 book_category
-- 说明：支持层级分类（通过 parent_id 自关联实现）
-- ============================================================
DROP TABLE IF EXISTS book_category;
CREATE TABLE book_category (
    id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '分类ID，主键',
    category_name VARCHAR(50)  NOT NULL                 COMMENT '分类名称（如：计算机科学、文学小说）',
    parent_id     BIGINT       DEFAULT 0                COMMENT '父分类ID，0表示顶级分类',
    sort_order    INT          NOT NULL DEFAULT 0       COMMENT '排序序号（数值越小越靠前）',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted    TINYINT(1)   NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书分类表';

-- ============================================================
-- 4. 图书信息表 book_info
-- 说明：存储图书的详细信息及库存管理
-- 字段说明：
--   total_stock   - 图书总库存（馆藏数量）
--   current_stock - 当前可借数量（总库存 - 已借出 + 已归还）
-- 索引说明：
--   uk_isbn             - ISBN唯一索引，防止重复录入
--   idx_title_author    - 书名+作者复合索引，加速高频模糊搜索
--   idx_category_id     - 分类索引，加速按分类查询
-- ============================================================
DROP TABLE IF EXISTS book_info;
CREATE TABLE book_info (
    id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '图书ID，主键',
    isbn          VARCHAR(20)   NOT NULL                 COMMENT '国际标准书号（ISBN），唯一标识一本图书',
    title         VARCHAR(200)  NOT NULL                 COMMENT '图书标题/书名',
    author        VARCHAR(100)  NOT NULL                 COMMENT '作者',
    publisher     VARCHAR(100)  DEFAULT NULL             COMMENT '出版社',
    publish_date  DATE          DEFAULT NULL             COMMENT '出版日期',
    cover_url     VARCHAR(500)  DEFAULT NULL             COMMENT '封面图片URL地址',
    category_id   BIGINT        NOT NULL                 COMMENT '分类ID，关联 book_category.id',
    description   TEXT          DEFAULT NULL             COMMENT '图书简介/内容摘要',
    total_stock   INT           NOT NULL DEFAULT 0       COMMENT '总库存（馆藏数量）',
    current_stock INT           NOT NULL DEFAULT 0       COMMENT '当前可借数量',
    status        TINYINT(1)    NOT NULL DEFAULT 1       COMMENT '图书状态：0-已下架，1-在架可借',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted    TINYINT(1)    NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_isbn (isbn),
    KEY idx_title_author (title, author),
    KEY idx_category_id (category_id),
    KEY idx_current_stock (current_stock),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES book_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书信息表';

-- ============================================================
-- 5. 借阅记录表 borrow_record
-- 说明：记录每一次借阅操作，驱动核心业务状态机流转
-- 状态机说明：
--   0 - 借阅中（已借出未归还，且未超过应还日期）
--   1 - 已续借（在借阅中状态下申请续借成功）
--   2 - 已归还（图书已归还入库）
--   3 - 已逾期（超过应还日期仍未归还）
-- 状态流转规则：
--   借阅中(0) ──续借──▶ 已续借(1)
--   借阅中(0) ──归还──▶ 已归还(2)
--   借阅中(0) ──超期──▶ 已逾期(3)
--   已续借(1) ──归还──▶ 已归还(2)
--   已续借(1) ──超期──▶ 已逾期(3)
--   已逾期(3) ──归还──▶ 已归还(2)
-- ============================================================
DROP TABLE IF EXISTS borrow_record;
CREATE TABLE borrow_record (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '借阅记录ID，主键',
    user_id     BIGINT       NOT NULL                 COMMENT '借阅用户ID，关联 sys_user.id',
    book_id     BIGINT       NOT NULL                 COMMENT '所借图书ID，关联 book_info.id',
    borrow_time DATETIME     NOT NULL                 COMMENT '借阅时间（实际借出时间）',
    due_time    DATETIME     NOT NULL                 COMMENT '应还时间（借阅时间 + 借阅期限）',
    return_time DATETIME     DEFAULT NULL             COMMENT '实际归还时间（为空表示尚未归还）',
    renew_time  DATETIME     DEFAULT NULL             COMMENT '续借时间（最近一次续借操作的时间）',
    renew_count INT          NOT NULL DEFAULT 0       COMMENT '续借次数（每本书最多续借1次）',
    status      TINYINT(1)   NOT NULL DEFAULT 0       COMMENT '借阅状态：0-借阅中，1-已续借，2-已归还，3-已逾期',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    KEY idx_status (status),
    KEY idx_borrow_time (borrow_time),
    KEY idx_due_time (due_time),
    KEY idx_user_book_status (user_id, book_id, status),
    CONSTRAINT fk_borrow_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES book_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅记录表';

-- ============================================================
-- 初始化角色数据
-- ============================================================
INSERT INTO sys_role (role_name, role_code, description) VALUES
('系统管理员', 'ROLE_ADMIN',  '系统管理员，拥有全部操作权限'),
('学生',       'ROLE_STUDENT', '普通学生用户，可借阅、续借、归还图书');

-- ============================================================
-- 初始化管理员账号（密码为 admin123，使用BCrypt加密后的密文）
-- 实际开发中应在注册/添加用户时由后端加密存储，此处仅为演示数据
-- ============================================================
-- 管理员账号：admin / 密码：admin123（MD5加密）
INSERT INTO sys_user (username, password, real_name, student_no, phone, email, role_id, status) VALUES
('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', NULL, '13800000000', 'admin@campus.edu', 1, 1);

-- ============================================================
-- 初始化图书分类数据
-- ============================================================
INSERT INTO book_category (category_name, parent_id, sort_order) VALUES
('计算机科学',     0, 1),
('文学小说',       0, 2),
('社会科学',       0, 3),
('自然科学',       0, 4),
('哲学宗教',       0, 5),
('经济管理',       0, 6),
('编程语言',       1, 1),   -- 父分类：计算机科学
('数据库',         1, 2),   -- 父分类：计算机科学
('人工智能',       1, 3),   -- 父分类：计算机科学
('中国文学',       2, 1),   -- 父分类：文学小说
('外国文学',       2, 2);   -- 父分类：文学小说

-- ============================================================
-- 初始化图书数据（示例）
-- ============================================================
INSERT INTO book_info (isbn, title, author, publisher, publish_date, cover_url, category_id, description, total_stock, current_stock, status) VALUES
('978-7-111-67777-7', 'Java核心技术 卷I',       'Cay S. Horstmann', '机械工业出版社', '2022-06-01', '/covers/java-core.jpg',     7, '全面讲解Java语言核心概念与API，是Java开发者必读经典之作。', 10, 10, 1),
('978-7-121-38888-8', '高性能MySQL',             'Baron Schwartz',   '电子工业出版社', '2021-03-01', '/covers/mysql-hp.jpg',      8, '深入剖析MySQL内核原理、性能调优与高可用架构设计。',       5,  5,  1),
('978-7-302-55555-5', 'Spring实战',              'Craig Walls',      '清华大学出版社', '2020-09-01', '/covers/spring-action.jpg', 7, '全面介绍Spring框架核心特性及SpringBoot微服务开发。',       8,  8,  1),
('978-7-111-69999-9', '深入理解计算机系统',       'Randal E. Bryant', '机械工业出版社', '2021-11-01', '/covers/csapp.jpg',         7, '从程序员视角深入理解计算机系统底层原理的经典教材。',         6,  6,  1),
('978-7-02-16666-6',  '活着',                    '余华',             '北京十月文艺出版社', '2017-06-01', '/covers/huozhe.jpg',      10, '余华代表作，讲述了一个人一生的苦难与坚韧。',                15, 15, 1);
