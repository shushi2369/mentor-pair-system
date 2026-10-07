package com.mentorpair.controller;

import com.mentorpair.common.PageVO;
import com.mentorpair.common.Result;
import com.mentorpair.dto.GuidanceVO;
import com.mentorpair.dto.LoginUser;
import com.mentorpair.dto.MentorCardVO;
import com.mentorpair.dto.ProjectVO;
import com.mentorpair.dto.SelectionVO;
import com.mentorpair.dto.SubmissionVO;
import com.mentorpair.dto.WindowVO;
import com.mentorpair.service.CourseService;
import com.mentorpair.service.GuidanceService;
import com.mentorpair.service.MentorInfoService;
import com.mentorpair.service.ProjectService;
import com.mentorpair.service.SelectionService;
import com.mentorpair.service.SelectionWindowService;
import com.mentorpair.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/student/api")
public class StudentApiController {

    @Autowired
    private MentorInfoService mentorInfoService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SelectionService selectionService;
    @Autowired
    private SelectionWindowService selectionWindowService;
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private GuidanceService guidanceService;

    private static LoginUser me(HttpSession session) {
        return (LoginUser) session.getAttribute(LoginUser.SESSION_KEY);
    }

    // ---------- 双选时间窗 ----------

    @GetMapping("/window")
    public Result<WindowVO> window() {
        return Result.ok(selectionWindowService.status());
    }

    // ---------- 选择导师 / 查看项目 ----------

    @GetMapping("/mentors")
    public Result<PageVO<MentorCardVO>> mentors(HttpSession session,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String kw) {
        return Result.ok(mentorInfoService.pageMentorCards(page, Math.min(size, 100), kw));
    }

    @GetMapping("/projects")
    public Result<PageVO<ProjectVO>> projects(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) Long mentorId,
                                              @RequestParam(required = false) String kw) {
        return Result.ok(projectService.voPage(page, Math.min(size, 100), mentorId, 1, kw));
    }

    @PostMapping("/selections")
    public Result<Void> apply(HttpSession session, @RequestParam Long mentorId,
                              @RequestParam(required = false) Long projectId) {
        selectionService.apply(me(session).getId(), mentorId, projectId);
        return Result.ok();
    }

    @GetMapping("/selections")
    public Result<PageVO<SelectionVO>> selections(HttpSession session,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) Integer status) {
        return Result.ok(selectionService.voPage(page, Math.min(size, 100), null, me(session).getId(), status));
    }

    // ---------- 提交项目文件 ----------

    @GetMapping("/submissions")
    public Result<PageVO<SubmissionVO>> submissions(HttpSession session,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return Result.ok(submissionService.voPage(page, Math.min(size, 100), null, me(session).getId()));
    }

    @PostMapping("/submissions")
    public Result<Void> upload(HttpSession session, @RequestParam Long selectionId,
                               @RequestParam String title,
                               @RequestParam(required = false) String description,
                               @RequestParam("file") MultipartFile file) {
        submissionService.upload(me(session).getId(), selectionId, title, description, file);
        return Result.ok();
    }

    // ---------- 查看指导信息 ----------

    @GetMapping("/guidances")
    public Result<PageVO<GuidanceVO>> guidances(HttpSession session,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(guidanceService.voPage(page, Math.min(size, 100), null, me(session).getId()));
    }
}
