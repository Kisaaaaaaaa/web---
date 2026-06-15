package com.campus.bookborrow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园图书借阅自助管理系统 - 启动类
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@SpringBootApplication
public class CampusBookBorrowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusBookBorrowApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  校园图书借阅自助管理系统 启动成功！");
        System.out.println("  后端端口：8088");
        System.out.println("=================================================");
    }
}
