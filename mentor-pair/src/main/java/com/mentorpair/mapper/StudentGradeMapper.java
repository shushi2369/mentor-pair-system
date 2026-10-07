package com.mentorpair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mentorpair.dto.GradeVO;
import com.mentorpair.entity.StudentGrade;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentGradeMapper extends BaseMapper<StudentGrade> {

    @Select("SELECT g.id, g.course_id AS courseId, c.course_name AS courseName,"
            + " g.student_no AS studentNo, g.student_name AS studentName, g.score"
            + " FROM student_grade g JOIN course c ON c.id = g.course_id"
            + " WHERE g.course_id = #{courseId}"
            + " ORDER BY g.student_no")
    List<GradeVO> selectByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT g.id, g.course_id AS courseId, c.course_name AS courseName,"
            + " g.student_no AS studentNo, g.student_name AS studentName, g.score"
            + " FROM student_grade g JOIN course c ON c.id = g.course_id"
            + " WHERE g.student_no = #{studentNo}"
            + " ORDER BY c.course_name")
    List<GradeVO> selectByStudentNo(@Param("studentNo") String studentNo);
}
