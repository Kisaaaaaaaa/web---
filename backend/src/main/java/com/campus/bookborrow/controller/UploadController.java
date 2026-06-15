package com.campus.bookborrow.controller;

import com.campus.bookborrow.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private Path uploadDir;

    @PostConstruct
    public void init() {
        // 相对路径 → 绝对路径（写入 frontend/public/ 下）
        uploadDir = Paths.get(System.getProperty("user.dir")).getParent().resolve("frontend").resolve("public").resolve("uploads");
        try { Files.createDirectories(uploadDir); } catch (Exception e) { e.printStackTrace(); }
    }

    @PostMapping
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(400, "文件为空");
        try {
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : ".png";
            String newName = UUID.randomUUID().toString() + ext;
            File dest = uploadDir.resolve(newName).toFile();
            file.transferTo(dest);
            return Result.ok(Map.of("url", "/uploads/" + newName));
        } catch (Exception e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }
}
