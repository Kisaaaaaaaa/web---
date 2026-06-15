package com.campus.bookborrow.service.impl;

import com.campus.bookborrow.entity.Announcement;
import com.campus.bookborrow.mapper.AnnouncementMapper;
import com.campus.bookborrow.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service @RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {
    private final AnnouncementMapper mapper;

    @Override public List<Announcement> getVisible() { return mapper.selectVisible(); }
    @Override public List<Announcement> getAll() { return mapper.selectAll(); }
    @Override public void add(Announcement a) { mapper.insert(a); }
    @Override public void update(Announcement a) { mapper.update(a); }
    @Override public void delete(Long id) { mapper.deleteById(id); }
}
