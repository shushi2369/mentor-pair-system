package com.mentorpair.service;

import com.mentorpair.common.PageVO;
import com.mentorpair.dto.CourseVO;
import com.mentorpair.dto.GradeVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseService {

    PageVO<CourseVO> page(Long mentorId, int page, int size);

    List<GradeVO> gradesOfCourse(Long mentorId, Long courseId);

    List<GradeVO> gradesByStudentNo(String studentNo);

    String importCourse(Long mentorId, String courseName, MultipartFile file);

    void delete(Long mentorId, Long courseId);
}
