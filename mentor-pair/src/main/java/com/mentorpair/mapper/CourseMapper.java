package com.mentorpair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mentorpair.dto.CourseVO;
import com.mentorpair.entity.Course;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT c.id, c.course_name AS courseName,"
            + " (SELECT COUNT(*) FROM student_grade g WHERE g.course_id = c.id) AS gradeCount,"
            + " c.create_time AS createTime"
            + " FROM course c WHERE c.mentor_id = #{mentorId}"
            + " ORDER BY c.create_time DESC, c.id DESC")
    IPage<CourseVO> selectVOPage(IPage<CourseVO> page, @Param("mentorId") Long mentorId);
}
