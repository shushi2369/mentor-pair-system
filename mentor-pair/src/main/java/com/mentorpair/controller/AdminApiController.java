package com.mentorpair.controller;

import com.mentorpair.common.BusinessException;
import com.mentorpair.common.PageVO;
import com.mentorpair.common.Result;
import com.mentorpair.dto.ProjectVO;
import com.mentorpair.dto.SelectionVO;
import com.mentorpair.dto.SubmissionVO;
import com.mentorpair.dto.GuidanceVO;
import com.mentorpair.dto.WindowVO;
import com.mentorpair.entity.User;
import com.mentorpair.service.GuidanceService;
import com.mentorpair.service.ProjectService;
import com.mentorpair.service.SelectionService;
import com.mentorpair.service.SelectionWindowService;
import com.mentorpair.service.StatsService;
import com.mentorpair.service.SubmissionService;
import com.mentorpair.service.UserService;
import com.mentorpair.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/admin/api")
public class AdminApiController {

    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private GuidanceService guidanceService;
    @Autowired
    private StatsService statsService;
    @Autowired
    private SelectionService selectionService;
    @Autowired
    private SelectionWindowService selectionWindowService;

    // ---------- 数据总览 ----------

    @GetMapping("/stats")
    public Result<java.util.Map<String, Long>> stats() {
        return Result.ok(statsService.stats());
    }

    @GetMapping("/recent")
    public Result<PageVO<SelectionVO>> recent() {
        return Result.ok(selectionService.voPage(1, 5, null, null, null));
    }

    // ---------- 双选时间窗 ----------

    @GetMapping("/window")
    public Result<WindowVO> window() {
        return Result.ok(selectionWindowService.status());
    }

    /** 保存时间窗：时间为 yyyy-MM-ddTHH:mm（datetime-local 格式），留空表示不限 */
    @PostMapping("/window")
    public Result<Void> saveWindow(@RequestParam(required = false) Integer enabled,
                                   @RequestParam(required = false) String startTime,
                                   @RequestParam(required = false) String endTime) {
        selectionWindowService.save(enabled, parseTime(startTime), parseTime(endTime));
        return Result.ok();
    }

    /** 解析 datetime-local 时间；关闭时间窗等场景允许留空或非法值（按未设置处理） */
    private LocalDateTime parseTime(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(s.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ---------- 账号管理（学员/导师） ----------

    @GetMapping("/users")
    public Result<PageVO<User>> users(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) Integer role,
                                      @RequestParam(required = false) String kw) {
        return Result.ok(userService.page(page, Math.min(size, 100), role, kw));
    }

    @PostMapping("/users")
    public Result<Void> createUser(@RequestParam String username, @RequestParam String password,
                                   @RequestParam String realName, @RequestParam Integer role,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam(required = false) Integer maxQuota,
                                   @RequestParam(required = false) String studentNo,
                                   @RequestParam(required = false) String major,
                                   @RequestParam(required = false) String className) {
        userService.create(username, password, realName, role, phone, maxQuota, studentNo, major, className);
        return Result.ok();
    }

    @PostMapping("/users/{id}/update")
    public Result<Void> updateUser(@PathVariable Long id,
                                   @RequestParam(required = false) String realName,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam(required = false) Integer maxQuota) {
        userService.update(id, realName, phone, maxQuota);
        return Result.ok();
    }

    @PostMapping("/users/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return Result.ok();
    }

    @PostMapping("/users/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.ok();
    }

    /** 批量导入学员账号：Excel 表头固定为 学号、姓名、密码、专业、班级（密码留空默认 123456） */
    @PostMapping("/users/import")
    public Result<String> importUsers(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择 Excel 文件");
        }
        if (!ExcelUtil.isExcelFileName(file.getOriginalFilename())) {
            throw new BusinessException("仅支持 .xls/.xlsx 文件");
        }
        ExcelUtil.StudentParseResult pr;
        try {
            pr = ExcelUtil.parseStudentExcel(file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("文件读取失败，请重试");
        }
        if (pr.rows.isEmpty()) {
            throw new BusinessException("未解析到有效学员行（首行表头需为：学号、姓名、密码、专业、班级）");
        }
        return Result.ok(userService.batchImportStudents(pr.rows));
    }

    // ---------- 项目信息管理 ----------

    @GetMapping("/projects")
    public Result<PageVO<ProjectVO>> projects(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String kw) {
        return Result.ok(projectService.voPage(page, Math.min(size, 100), null, null, kw));
    }

    @PostMapping("/projects/{id}/offline")
    public Result<Void> offlineProject(@PathVariable Long id) {
        projectService.offline(id, null);
        return Result.ok();
    }

    @DeleteMapping("/projects/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.delete(id);
        return Result.ok();
    }

    // ---------- 项目提交管理 ----------

    @GetMapping("/submissions")
    public Result<PageVO<SubmissionVO>> submissions(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return Result.ok(submissionService.voPage(page, Math.min(size, 100), null, null));
    }

    @DeleteMapping("/submissions/{id}")
    public Result<Void> deleteSubmission(@PathVariable Long id) {
        submissionService.delete(id);
        return Result.ok();
    }

    // ---------- 指导信息管理 ----------

    @GetMapping("/guidances")
    public Result<PageVO<GuidanceVO>> guidances(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(guidanceService.voPage(page, Math.min(size, 100), null, null));
    }

    @DeleteMapping("/guidances/{id}")
    public Result<Void> deleteGuidance(@PathVariable Long id) {
        guidanceService.delete(id);
        return Result.ok();
    }
}
