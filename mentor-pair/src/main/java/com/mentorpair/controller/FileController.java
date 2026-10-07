package com.mentorpair.controller;

import com.mentorpair.common.BusinessException;
import com.mentorpair.dto.LoginUser;
import com.mentorpair.entity.Selection;
import com.mentorpair.entity.Submission;
import com.mentorpair.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** 项目文件下载：学员下载本人提交，导师下载其指导学员的提交，管理员可下载全部 */
@RestController
public class FileController {

    @Autowired
    private SubmissionService submissionService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @GetMapping("/api/files/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id, HttpSession session) throws Exception {
        LoginUser u = (LoginUser) session.getAttribute(LoginUser.SESSION_KEY);
        Submission sub = submissionService.getById(id);
        if (sub == null) {
            throw new BusinessException("提交记录不存在");
        }
        Selection sel = submissionService.getSelectionOfSubmission(id);
        boolean allowed = u.isAdmin()
                || (u.isStudent() && sel.getStudentId().equals(u.getId()))
                || (u.isMentor() && sel.getMentorId().equals(u.getId()));
        if (!allowed) {
            throw new BusinessException("没有权限下载该文件");
        }
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path file = base.resolve(sub.getFilePath()).normalize();
        if (!file.startsWith(base) || !Files.exists(file)) {
            throw new BusinessException("文件不存在或已被清理");
        }
        String encodedName = URLEncoder.encode(sub.getFileName(), "UTF-8").replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }
}
