package com.mentorpair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mentorpair.dto.ProjectVO;
import com.mentorpair.entity.Project;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ProjectMapper extends BaseMapper<Project> {

    @Select("<script>"
            + "SELECT p.id, p.mentor_id AS mentorId, u.real_name AS mentorName, p.title,"
            + " p.description, p.status, p.create_time AS createTime"
            + " FROM project p JOIN sys_user u ON u.id = p.mentor_id"
            + " <where>"
            + " <if test='mentorId != null'> AND p.mentor_id = #{mentorId}</if>"
            + " <if test='status != null'> AND p.status = #{status}</if>"
            + " <if test='kw != null and kw != \"\"'>"
            + " AND (p.title LIKE CONCAT('%',#{kw},'%') OR u.real_name LIKE CONCAT('%',#{kw},'%'))"
            + " </if>"
            + " </where>"
            + " ORDER BY p.create_time DESC, p.id DESC"
            + "</script>")
    IPage<ProjectVO> selectVOPage(IPage<ProjectVO> page, @Param("mentorId") Long mentorId,
                                  @Param("status") Integer status, @Param("kw") String kw);
}
