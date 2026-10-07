package com.mentorpair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentorpair.common.BusinessException;
import com.mentorpair.common.PageVO;
import com.mentorpair.dto.MentorCardVO;
import com.mentorpair.entity.MentorInfo;
import com.mentorpair.mapper.MentorInfoMapper;
import com.mentorpair.service.MentorInfoService;
import com.mentorpair.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MentorInfoServiceImpl implements MentorInfoService {

    @Autowired
    private MentorInfoMapper mentorInfoMapper;

    @Override
    public MentorInfo getByUserId(Long userId) {
        return mentorInfoMapper.selectOne(
                new LambdaQueryWrapper<MentorInfo>().eq(MentorInfo::getUserId, userId));
    }

    @Override
    public PageVO<MentorCardVO> pageMentorCards(int page, int size, String kw) {
        IPage<MentorCardVO> p = mentorInfoMapper.selectMentorCards(new Page<>(page, size), kw);
        return PageVO.of(p);
    }

    @Override
    public void saveProfile(Long userId, String jobTitle, String department, String researchArea,
                            Integer maxQuota, String intro) {
        MentorInfo mi = getByUserId(userId);
        if (mi == null) {
            mi = new MentorInfo();
            mi.setUserId(userId);
            mi.setMaxQuota(0);
        }
        if (maxQuota != null) {
            if (maxQuota < 0 || maxQuota > 999) {
                throw new BusinessException("名额需在 0-999 之间");
            }
            mi.setMaxQuota(maxQuota);
        }
        TextUtil.ensureLen("职称", jobTitle, 50);
        TextUtil.ensureLen("院系", department, 100);
        TextUtil.ensureLen("研究方向", researchArea, 200);
        TextUtil.ensureLen("个人简介", intro, 1000);
        mi.setJobTitle(jobTitle);
        mi.setDepartment(department);
        mi.setResearchArea(researchArea);
        mi.setIntro(intro);
        if (mi.getId() == null) {
            mentorInfoMapper.insert(mi);
        } else {
            mentorInfoMapper.updateById(mi);
        }
        log.info("导师 {} 保存资料", userId);
    }
}
