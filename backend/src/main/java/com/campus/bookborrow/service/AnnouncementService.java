package com.campus.bookborrow.service;

import com.campus.bookborrow.entity.Announcement;
import java.util.List;

public interface AnnouncementService {
    List<Announcement> getVisible();
    List<Announcement> getAll();
    void add(Announcement a);
    void update(Announcement a);
    void delete(Long id);
}
