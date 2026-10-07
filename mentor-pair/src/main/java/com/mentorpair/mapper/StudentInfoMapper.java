package com.mentorpair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mentorpair.entity.StudentInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StudentInfoMapper extends BaseMapper<StudentInfo> {

    /** 事务内行锁学员记录：串行化同一学员的申请与被接受操作 */
    @Select("SELECT * FROM student_info WHERE user_id = #{userId} FOR UPDATE")
    StudentInfo lockByUserId(@Param("userId") Long userId);
}
