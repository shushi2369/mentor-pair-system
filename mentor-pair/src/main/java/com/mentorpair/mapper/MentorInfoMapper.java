package com.mentorpair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mentorpair.dto.MentorCardVO;
import com.mentorpair.entity.MentorInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface MentorInfoMapper extends BaseMapper<MentorInfo> {

    /** 事务内行锁导师名额记录，防止并发超收 */
    @Select("SELECT * FROM mentor_info WHERE user_id = #{userId} FOR UPDATE")
    MentorInfo lockByUserId(@Param("userId") Long userId);

    @Select("<script>"
            + "SELECT u.id AS mentorId, u.real_name AS mentorName, mi.job_title AS jobTitle,"
            + " mi.department AS department, mi.research_area AS researchArea,"
            + " mi.max_quota AS maxQuota, mi.intro AS intro,"
            + " (SELECT COUNT(*) FROM selection s WHERE s.mentor_id = u.id AND s.status = 1) AS acceptedCount"
            + " FROM sys_user u JOIN mentor_info mi ON mi.user_id = u.id"
            + " WHERE u.role = 2 AND u.status = 1"
            + " <if test='kw != null and kw != \"\"'>"
            + " AND (u.real_name LIKE CONCAT('%',#{kw},'%')"
            + " OR mi.research_area LIKE CONCAT('%',#{kw},'%')"
            + " OR mi.department LIKE CONCAT('%',#{kw},'%'))"
            + " </if>"
            + " ORDER BY u.id"
            + "</script>")
    IPage<MentorCardVO> selectMentorCards(IPage<MentorCardVO> page, @Param("kw") String kw);
}
