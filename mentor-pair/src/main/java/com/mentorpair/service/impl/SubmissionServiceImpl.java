package com.mentorpair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentorpair.common.BusinessException;
import com.mentorpair.common.PageVO;
import com.mentorpair.dto.SubmissionVO;
import com.mentorpair.entity.Selection;
import com.mentorpair.entity.Submission;
import com.mentorpair.mapper.GuidanceMapper;
import com.mentorpair.mapper.SelectionMapper;
import com.mentorpair.mapper.SubmissionMapper;
import com.mentorpair.service.SubmissionService;
import com.mentorpair.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final Set<String> ALLOWED_EXT = new HashSet<>(
            Arrays.asList("rar", "zip", "doc", "docx", "pdf", "ppt", "pptx"));

    @Autowired
    private SubmissionMapper submissionMapper;
    @Autowired
    private SelectionMapper selectionMapper;
    @Autowired
    private GuidanceMapper guidanceMapper;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public PageVO<SubmissionVO> voPage(int page, int size, Long mentorId, Long studentId) {
        IPage<SubmissionVO> p = submissionMapper.selectVOPage(new Page<>(page, size), mentorId, studentId);
        return PageVO.of(p);
    }

    @Override
    public Submission getById(Long id) {
        return submissionMapper.selectById(id);
    }

    @Override
    public Selection getSelectionOfSubmission(Long submissionId) {
        Submission sub = getById(submissionId);
        if (sub == null) {
            throw new BusinessException("提交记录不存在");
        }
        Selection s = selectionMapper.selectById(sub.getSelectionId());
        if (s == null) {
            throw new BusinessException("关联的双选记录不存在");
        }
        return s;
    }

    @Override
    @Transactional
    public void upload(Long studentId, Long selectionId, String title, String description, MultipartFile file) {
        if (TextUtil.isBlank(title)) {
            throw new BusinessException("请填写提交标题");
        }
        TextUtil.ensureLen("提交标题", title, 100);
        TextUtil.ensureLen("补充说明", description, 500);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        String original = file.getOriginalFilename();
        int dot = original == null ? -1 : original.lastIndexOf('.');
        if (dot < 0) {
            throw new BusinessException("文件缺少扩展名，仅支持 rar/zip/doc/docx/pdf/ppt/pptx");
        }
        String ext = original.substring(dot + 1).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("不支持的文件类型 ." + ext + "，仅支持 rar/zip/doc/docx/pdf/ppt/pptx");
        }
        Selection s = selectionMapper.selectById(selectionId);
        if (s == null || !s.getStudentId().equals(studentId)) {
            throw new BusinessException("双选记录不存在");
        }
        if (s.getStatus() == null || s.getStatus() != Selection.STATUS_ACCEPTED) {
            throw new BusinessException("双选申请被接受后才能提交项目文件");
        }
        String folder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String stored = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = Paths.get(uploadDir, folder).toAbsolutePath().normalize().resolve(stored);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败，请稍后重试");
        }
        Submission sub = new Submission();
        sub.setSelectionId(selectionId);
        sub.setTitle(title.trim());
        sub.setDescription(TextUtil.blankToNull(description));
        sub.setFileName(original);
        sub.setFilePath(folder + "/" + stored);
        sub.setFileSize(file.getSize());
        sub.setSubmitTime(LocalDateTime.now());
        submissionMapper.insert(sub);
        log.info("学员 {} 提交项目文件：{}（{} 字节）", studentId, original, file.getSize());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Submission sub = getById(id);
        if (sub == null) {
            throw new BusinessException("提交记录不存在");
        }
        guidanceMapper.delete(new LambdaQueryWrapper<com.mentorpair.entity.Guidance>()
                .eq(com.mentorpair.entity.Guidance::getSubmissionId, id));
        submissionMapper.deleteById(id);
        try {
            Path file = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(sub.getFilePath()).normalize();
            if (file.startsWith(Paths.get(uploadDir).toAbsolutePath().normalize())) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            log.warn("删除物理文件失败：{}", sub.getFilePath());
        }
        log.info("提交记录 {} 删除", id);
    }
}
