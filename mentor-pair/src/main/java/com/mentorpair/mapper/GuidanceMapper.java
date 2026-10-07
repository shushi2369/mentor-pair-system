package com.mentorpair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mentorpair.dto.GuidanceVO;
import com.mentorpair.entity.Guidance;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface GuidanceMapper extends BaseMapper<Guidance> {

    @Select("<script>"
            + "SELECT g.id, g.submission_id AS submissionId, sub.title AS submissionTitle,"
            + " g.content, g.guidance_time AS guidanceTime,"
            + " mu.real_name AS mentorName, su.real_name AS studentName, si.student_no AS studentNo"
            + " FROM guidance g"
            + " JOIN submission sub ON sub.id = g.submission_id"
            + " JOIN selection s ON s.id = sub.selection_id"
            + " LEFT JOIN sys_user mu ON mu.id = g.mentor_id"
            + " LEFT JOIN sys_user su ON su.id = s.student_id"
            + " LEFT JOIN student_info si ON si.user_id = s.student_id"
            + " <where>"
            + " <if test='mentorId != null'> AND g.mentor_id = #{mentorId}</if>"
            + " <if test='studentId != null'> AND s.student_id = #{studentId}</if>"
            + " </where>"
            + " ORDER BY g.guidance_time DESC, g.id DESC"
            + "</script>")
    IPage<GuidanceVO> selectVOPage(IPage<GuidanceVO> page, @Param("mentorId") Long mentorId,
                                   @Param("studentId") Long studentId);
}
