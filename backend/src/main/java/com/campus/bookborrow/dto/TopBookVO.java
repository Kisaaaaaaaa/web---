package com.campus.bookborrow.dto;

import lombok.Data;

@Data
public class TopBookVO {
    private Long bookId;
    private String title;
    private String author;
    private String coverUrl;
    private Long borrowCount;
}
