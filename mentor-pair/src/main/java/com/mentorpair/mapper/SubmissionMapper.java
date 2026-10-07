package com.mentorpair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mentorpair.dto.SubmissionVO;
import com.mentorpair.entity.Submission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SubmissionMapper extends BaseMapper<Submission> {

    @Select("<script>"
            + "SELECT sub.id, sub.selection_id AS selectionId, sub.title, sub.description,"
            + " sub.file_name AS fileName, sub.file_size AS fileSize, sub.submit_time AS submitTime,"
            + " s.student_id AS studentId, s.mentor_id AS mentorId,"
            + " su.real_name AS studentName, si.student_no AS studentNo,"
            + " mu.real_name AS mentorName, p.title AS projectTitle"
            + " FROM submission sub"
            + " JOIN selection s ON s.id = sub.selection_id"
            + " LEFT JOIN sys_user su ON su.id = s.student_id"
            + " LEFT JOIN student_info si ON si.user_id = s.student_id"
            + " LEFT JOIN sys_user mu ON mu.id = s.mentor_id"
            + " LEFT JOIN project p ON p.id = s.project_id"
            + " <where>"
            + " <if test='mentorId != null'> AND s.mentor_id = #{mentorId}</if>"
            + " <if test='studentId != null'> AND s.student_id = #{studentId}</if>"
            + " </where>"
            + " ORDER BY sub.submit_time DESC, sub.id DESC"
            + "</script>")
    IPage<SubmissionVO> selectVOPage(IPage<SubmissionVO> page, @Param("mentorId") Long mentorId,
                                     @Param("studentId") Long studentId);
}
