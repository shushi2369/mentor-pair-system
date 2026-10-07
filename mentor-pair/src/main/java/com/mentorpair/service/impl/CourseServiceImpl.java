package com.mentorpair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentorpair.common.BusinessException;
import com.mentorpair.common.PageVO;
import com.mentorpair.dto.CourseVO;
import com.mentorpair.dto.GradeVO;
import com.mentorpair.entity.Course;
import com.mentorpair.entity.StudentGrade;
import com.mentorpair.mapper.CourseMapper;
import com.mentorpair.mapper.StudentGradeMapper;
import com.mentorpair.service.CourseService;
import com.mentorpair.util.ExcelUtil;
import com.mentorpair.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private StudentGradeMapper studentGradeMapper;

    @Override
    public PageVO<CourseVO> page(Long mentorId, int page, int size) {
        IPage<CourseVO> p = courseMapper.selectVOPage(new Page<>(page, size), mentorId);
        return PageVO.of(p);
    }

    @Override
    public List<GradeVO> gradesOfCourse(Long mentorId, Long courseId) {
        Course c = requireCourse(mentorId, courseId);
        return studentGradeMapper.selectByCourseId(c.getId());
    }

    @Override
    public List<GradeVO> gradesByStudentNo(String studentNo) {
        if (TextUtil.isBlank(studentNo)) {
            return new java.util.ArrayList<>();
        }
        return studentGradeMapper.selectByStudentNo(studentNo.trim());
    }

    @Override
    @Transactional
    public String importCourse(Long mentorId, String courseName, MultipartFile file) {
        if (TextUtil.isBlank(courseName)) {
            throw new BusinessException("请填写课程名称");
        }
        courseName = courseName.trim();
        if (courseName.length() > 100) {
            throw new BusinessException("课程名称不能超过 100 字");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择 Excel 文件");
        }
        if (!ExcelUtil.isExcelFileName(file.getOriginalFilename())) {
            throw new BusinessException("仅支持 .xls/.xlsx 文件");
        }
        if (courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getMentorId, mentorId).eq(Course::getCourseName, courseName)) > 0) {
            throw new BusinessException("课程“" + courseName + "”已导入过，请更换课程名");
        }
        ExcelUtil.ParseResult pr;
        try {
            pr = ExcelUtil.parseGradeExcel(file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("文件读取失败，请重试");
        }
        if (pr.rows.isEmpty()) {
            throw new BusinessException("未解析到有效成绩行（首行表头需为：学号、姓名、成绩）");
        }
        Course c = new Course();
        c.setMentorId(mentorId);
        c.setCourseName(courseName);
        courseMapper.insert(c);

        Set<String> seen = new HashSet<>();
        int parseFail = pr.errors.size();
        int insertFail = 0;
        for (ExcelUtil.GradeRow g : pr.rows) {
            if (!seen.add(g.studentNo)) {
                pr.errors.add("第" + g.line + "行：学号 " + g.studentNo + " 在文件中重复");
                insertFail++;
                continue;
            }
            StudentGrade sg = new StudentGrade();
            sg.setCourseId(c.getId());
            sg.setStudentNo(g.studentNo);
            sg.setStudentName(g.studentName);
            sg.setScore(g.score);
            try {
                studentGradeMapper.insert(sg);
            } catch (Exception e) {
                pr.errors.add("第" + g.line + "行：学号 " + g.studentNo + " 写入失败");
                insertFail++;
            }
        }
        int success = pr.rows.size() - insertFail;
        int fail = parseFail + insertFail;
        if (success == 0) {
            throw new BusinessException("导入失败，未写入任何成绩。" + String.join("；", pr.errors));
        }
        log.info("导师 {} 导入课程“{}”：成功 {} 条，失败 {} 条", mentorId, courseName, success, fail);
        StringBuilder msg = new StringBuilder();
        msg.append("导入完成：成功 ").append(success).append(" 条，失败 ").append(fail).append(" 条");
        if (!pr.errors.isEmpty()) {
            msg.append("（").append(String.join("；", pr.errors)).append("）");
        }
        return msg.toString();
    }

    @Override
    @Transactional
    public void delete(Long mentorId, Long courseId) {
        Course c = requireCourse(mentorId, courseId);
        studentGradeMapper.delete(new LambdaQueryWrapper<StudentGrade>()
                .eq(StudentGrade::getCourseId, courseId));
        courseMapper.deleteById(courseId);
        log.info("课程 {}（{}）删除", courseId, c.getCourseName());
    }

    private Course requireCourse(Long mentorId, Long courseId) {
        Course c = courseMapper.selectById(courseId);
        if (c == null || !c.getMentorId().equals(mentorId)) {
            throw new BusinessException("课程不存在");
        }
        return c;
    }
}
