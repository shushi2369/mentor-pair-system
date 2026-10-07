package com.mentorpair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mentorpair.dto.SelectionVO;
import com.mentorpair.entity.Selection;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SelectionMapper extends BaseMapper<Selection> {

    @Select("<script>"
            + "SELECT s.id, s.student_id AS studentId, s.mentor_id AS mentorId,"
            + " s.project_id AS projectId, p.title AS projectTitle, s.status, s.remark,"
            + " s.apply_time AS applyTime, s.handle_time AS handleTime,"
            + " su.real_name AS studentName, si.student_no AS studentNo, mu.real_name AS mentorName"
            + " FROM selection s"
            + " LEFT JOIN sys_user su ON su.id = s.student_id"
            + " LEFT JOIN student_info si ON si.user_id = s.student_id"
            + " LEFT JOIN sys_user mu ON mu.id = s.mentor_id"
            + " LEFT JOIN project p ON p.id = s.project_id"
            + " <where>"
            + " <if test='mentorId != null'> AND s.mentor_id = #{mentorId}</if>"
            + " <if test='studentId != null'> AND s.student_id = #{studentId}</if>"
            + " <if test='status != null'> AND s.status = #{status}</if>"
            + " </where>"
            + " ORDER BY s.apply_time DESC, s.id DESC"
            + "</script>")
    IPage<SelectionVO> selectVOPage(IPage<SelectionVO> page, @Param("mentorId") Long mentorId,
                                    @Param("studentId") Long studentId, @Param("status") Integer status);
}
