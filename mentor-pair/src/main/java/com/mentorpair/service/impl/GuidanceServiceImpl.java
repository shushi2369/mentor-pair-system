package com.mentorpair.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentorpair.common.BusinessException;
import com.mentorpair.common.PageVO;
import com.mentorpair.dto.GuidanceVO;
import com.mentorpair.entity.Guidance;
import com.mentorpair.entity.Selection;
import com.mentorpair.entity.Submission;
import com.mentorpair.mapper.GuidanceMapper;
import com.mentorpair.mapper.SelectionMapper;
import com.mentorpair.mapper.SubmissionMapper;
import com.mentorpair.service.GuidanceService;
import com.mentorpair.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class GuidanceServiceImpl implements GuidanceService {

    @Autowired
    private GuidanceMapper guidanceMapper;
    @Autowired
    private SubmissionMapper submissionMapper;
    @Autowired
    private SelectionMapper selectionMapper;

    @Override
    public PageVO<GuidanceVO> voPage(int page, int size, Long mentorId, Long studentId) {
        IPage<GuidanceVO> p = guidanceMapper.selectVOPage(new Page<>(page, size), mentorId, studentId);
        return PageVO.of(p);
    }

    @Override
    public void add(Long mentorId, Long submissionId, String content) {
        if (TextUtil.isBlank(content)) {
            throw new BusinessException("请填写指导意见");
        }
        Submission sub = submissionMapper.selectById(submissionId);
        if (sub == null) {
            throw new BusinessException("提交记录不存在");
        }
        Selection s = selectionMapper.selectById(sub.getSelectionId());
        if (s == null || !s.getMentorId().equals(mentorId)) {
            throw new BusinessException("提交记录不存在");
        }
        Guidance g = new Guidance();
        g.setSubmissionId(submissionId);
        g.setMentorId(mentorId);
        g.setContent(content.trim());
        g.setGuidanceTime(LocalDateTime.now());
        guidanceMapper.insert(g);
        log.info("导师 {} 对提交 {} 写入指导", mentorId, submissionId);
    }

    @Override
    public void delete(Long id) {
        if (guidanceMapper.selectById(id) == null) {
            throw new BusinessException("指导记录不存在");
        }
        guidanceMapper.deleteById(id);
        log.info("指导记录 {} 删除", id);
    }
}
