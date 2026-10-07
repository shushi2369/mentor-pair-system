package com.mentorpair.controller;

import com.mentorpair.common.BusinessException;
import com.mentorpair.common.PageVO;
import com.mentorpair.common.Result;
import com.mentorpair.dto.CourseVO;
import com.mentorpair.dto.GradeVO;
import com.mentorpair.dto.LoginUser;
import com.mentorpair.dto.ProjectVO;
import com.mentorpair.dto.SelectionVO;
import com.mentorpair.dto.SubmissionVO;
import com.mentorpair.entity.MentorInfo;
import com.mentorpair.entity.Selection;
import com.mentorpair.entity.StudentInfo;
import com.mentorpair.service.CourseService;
import com.mentorpair.service.GuidanceService;
import com.mentorpair.service.MentorInfoService;
import com.mentorpair.service.ProjectService;
import com.mentorpair.service.SelectionService;
import com.mentorpair.service.StudentInfoService;
import com.mentorpair.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/mentor/api")
public class MentorApiController {

    @Autowired
    private MentorInfoService mentorInfoService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SelectionService selectionService;
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private GuidanceService guidanceService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private StudentInfoService studentInfoService;

    private static LoginUser me(HttpSession session) {
        return (LoginUser) session.getAttribute(LoginUser.SESSION_KEY);
    }

    // ---------- 导师资料 ----------

    @GetMapping("/profile")
    public Result<MentorInfo> profile(HttpSession session) {
        return Result.ok(mentorInfoService.getByUserId(me(session).getId()));
    }

    @PostMapping("/profile")
    public Result<Void> saveProfile(HttpSession session,
                                    @RequestParam(required = false) String jobTitle,
                                    @RequestParam(required = false) String department,
                                    @RequestParam(required = false) String researchArea,
                                    @RequestParam(required = false) Integer maxQuota,
                                    @RequestParam(required = false) String intro) {
        LoginUser u = me(session);
        mentorInfoService.saveProfile(u.getId(), jobTitle, department, researchArea, maxQuota, intro);
        return Result.ok();
    }

    // ---------- 项目管理 ----------

    @GetMapping("/projects")
    public Result<PageVO<ProjectVO>> projects(HttpSession session,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(projectService.voPage(page, Math.min(size, 100), me(session).getId(), null, null));
    }

    @PostMapping("/projects")
    public Result<Void> createProject(HttpSession session, @RequestParam String title,
                                      @RequestParam(required = false) String description) {
        projectService.create(me(session).getId(), title, description);
        return Result.ok();
    }

    @PostMapping("/projects/{id}/update")
    public Result<Void> updateProject(HttpSession session, @PathVariable Long id,
                                      @RequestParam String title,
                                      @RequestParam(required = false) String description) {
        projectService.update(id, me(session).getId(), title, description);
        return Result.ok();
    }

    @PostMapping("/projects/{id}/offline")
    public Result<Void> offlineProject(HttpSession session, @PathVariable Long id) {
        projectService.offline(id, me(session).getId());
        return Result.ok();
    }

    // ---------- 导师选择信息（双选申请处理） ----------

    @GetMapping("/selections")
    public Result<PageVO<SelectionVO>> selections(HttpSession session,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) Integer status) {
        return Result.ok(selectionService.voPage(page, Math.min(size, 100), me(session).getId(), null, status));
    }

    /** 导出已接收学员名单（xlsx） */
    @GetMapping("/selections/export")
    public ResponseEntity<byte[]> exportAccepted(HttpSession session) throws Exception {
        Long mentorId = me(session).getId();
        PageVO<SelectionVO> page = selectionService.voPage(1, -1, mentorId, null, Selection.STATUS_ACCEPTED);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("已接收学员");
            String[] heads = {"学号", "姓名", "意向项目", "申请时间", "接受时间"};
            Row head = sheet.createRow(0);
            for (int i = 0; i < heads.length; i++) {
                head.createCell(i).setCellValue(heads[i]);
            }
            int r = 1;
            for (SelectionVO s : page.getRecords()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(s.getStudentNo() == null ? "" : s.getStudentNo());
                row.createCell(1).setCellValue(s.getStudentName() == null ? "" : s.getStudentName());
                row.createCell(2).setCellValue(s.getProjectTitle() == null ? "未指定项目" : s.getProjectTitle());
                row.createCell(3).setCellValue(s.getApplyTime() == null ? "" : s.getApplyTime().format(fmt));
                row.createCell(4).setCellValue(s.getHandleTime() == null ? "" : s.getHandleTime().format(fmt));
            }
            for (int i = 0; i < heads.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 1024, 12000));
            }
            wb.write(bos);
            String name = URLEncoder.encode("已接收学员名单.xlsx", "UTF-8").replaceAll("\\+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + name)
                    .header(HttpHeaders.CONTENT_TYPE,
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(bos.toByteArray());
        }
    }

    @PostMapping("/selections/{id}/accept")
    public Result<Void> accept(HttpSession session, @PathVariable Long id) {
        selectionService.accept(me(session).getId(), id);
        return Result.ok();
    }

    @PostMapping("/selections/{id}/reject")
    public Result<Void> reject(HttpSession session, @PathVariable Long id,
                               @RequestParam(required = false) String remark) {
        selectionService.reject(me(session).getId(), id, remark);
        return Result.ok();
    }

    /** 查看申请学员（按学号匹配）已导入的成绩 */
    @GetMapping("/selections/{id}/grades")
    public Result<List<GradeVO>> applicantGrades(HttpSession session, @PathVariable Long id) {
        LoginUser u = me(session);
        Selection sel = selectionService.getById(id);
        if (sel == null || !sel.getMentorId().equals(u.getId())) {
            throw new BusinessException("申请不存在");
        }
        StudentInfo si = studentInfoService.getByUserId(sel.getStudentId());
        if (si == null) {
            return Result.ok(Collections.emptyList());
        }
        return Result.ok(courseService.gradesByStudentNo(si.getStudentNo()));
    }

    // ---------- 项目提交与指导 ----------

    @GetMapping("/submissions")
    public Result<PageVO<SubmissionVO>> submissions(HttpSession session,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return Result.ok(submissionService.voPage(page, Math.min(size, 100), me(session).getId(), null));
    }

    @PostMapping("/submissions/{id}/guidance")
    public Result<Void> guidance(HttpSession session, @PathVariable Long id,
                                 @RequestParam String content) {
        guidanceService.add(me(session).getId(), id, content);
        return Result.ok();
    }

    // ---------- 成绩单导入（Excel） ----------

    @GetMapping("/courses")
    public Result<PageVO<CourseVO>> courses(HttpSession session,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(courseService.page(me(session).getId(), page, Math.min(size, 100)));
    }

    @PostMapping("/courses")
    public Result<String> importCourse(HttpSession session,
                                       @RequestParam String courseName,
                                       @RequestParam("file") MultipartFile file) {
        return Result.ok(courseService.importCourse(me(session).getId(), courseName, file));
    }

    @GetMapping("/courses/{id}/grades")
    public Result<List<GradeVO>> courseGrades(HttpSession session, @PathVariable Long id) {
        return Result.ok(courseService.gradesOfCourse(me(session).getId(), id));
    }

    @DeleteMapping("/courses/{id}")
    public Result<Void> deleteCourse(HttpSession session, @PathVariable Long id) {
        courseService.delete(me(session).getId(), id);
        return Result.ok();
    }
}
