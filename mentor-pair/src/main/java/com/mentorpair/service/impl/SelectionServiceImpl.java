package com.mentorpair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentorpair.common.BusinessException;
import com.mentorpair.common.PageVO;
import com.mentorpair.dto.SelectionVO;
import com.mentorpair.entity.MentorInfo;
import com.mentorpair.entity.Project;
import com.mentorpair.entity.Selection;
import com.mentorpair.entity.User;
import com.mentorpair.mapper.MentorInfoMapper;
import com.mentorpair.mapper.ProjectMapper;
import com.mentorpair.mapper.SelectionMapper;
import com.mentorpair.mapper.StudentInfoMapper;
import com.mentorpair.mapper.UserMapper;
import com.mentorpair.service.SelectionService;
import com.mentorpair.service.SelectionWindowService;
import com.mentorpair.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Service
public class SelectionServiceImpl implements SelectionService {

    @Autowired
    private SelectionMapper selectionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MentorInfoMapper mentorInfoMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private SelectionWindowService selectionWindowService;
    @Autowired
    private StudentInfoMapper studentInfoMapper;

    @Override
    public PageVO<SelectionVO> voPage(int page, int size, Long mentorId, Long studentId, Integer status) {
        IPage<SelectionVO> p = selectionMapper.selectVOPage(new Page<>(page, size), mentorId, studentId, status);
        return PageVO.of(p);
    }

    @Override
    public Selection getById(Long id) {
        return selectionMapper.selectById(id);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void apply(Long studentId, Long mentorId, Long projectId) {
        // 双选时间窗：未开放时拒绝申请（导师处理不受限制）
        String reason = selectionWindowService.closedReason(LocalDateTime.now());
        if (reason != null) {
            throw new BusinessException(reason);
        }
        if (mentorId == null) {
            throw new BusinessException("请选择导师");
        }
        // 锁学员行：串行化同一学员的并发申请，保证"同时只有一条待处理/已接受申请"
        studentInfoMapper.lockByUserId(studentId);
        User mentor = userMapper.selectById(mentorId);
        if (mentor == null || mentor.getRole() == null || mentor.getRole() != 2
                || mentor.getStatus() == null || mentor.getStatus() != 1) {
            throw new BusinessException("该导师不存在或已停用");
        }
        MentorInfo mi = mentorInfoMapper.selectOne(
                new LambdaQueryWrapper<MentorInfo>().eq(MentorInfo::getUserId, mentorId));
        if (mi == null) {
            throw new BusinessException("该导师尚未完善信息，暂不可申请");
        }
        if (projectId != null) {
            Project p = projectMapper.selectById(projectId);
            if (p == null || !p.getMentorId().equals(mentorId) || p.getStatus() == null || p.getStatus() != 1) {
                throw new BusinessException("所选项目不存在或已下架");
            }
        }
        Long active = selectionMapper.selectCount(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getStudentId, studentId)
                .in(Selection::getStatus, Arrays.asList(Selection.STATUS_PENDING, Selection.STATUS_ACCEPTED)));
        if (active > 0) {
            throw new BusinessException("您已有待处理或已接受的双选申请，请等待处理结果");
        }
        Selection s = new Selection();
        s.setStudentId(studentId);
        s.setMentorId(mentorId);
        s.setProjectId(projectId);
        s.setStatus(Selection.STATUS_PENDING);
        s.setApplyTime(LocalDateTime.now());
        selectionMapper.insert(s);
        log.info("学员 {} 向导师 {} 提交双选申请", studentId, mentorId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void accept(Long mentorId, Long selectionId) {
        // 锁导师名额行，事务内完成"统计已接受 + 名额比较 + 写入状态"，防止并发超收
        MentorInfo mi = mentorInfoMapper.lockByUserId(mentorId);
        if (mi == null) {
            throw new BusinessException("导师信息不存在");
        }
        Selection s = selectionMapper.selectById(selectionId);
        if (s == null || !s.getMentorId().equals(mentorId)) {
            throw new BusinessException("申请不存在");
        }
        if (s.getStatus() != Selection.STATUS_PENDING) {
            throw new BusinessException("该申请已处理，请勿重复操作");
        }
        // 再锁学员行：防止两个导师并发接受同一学员
        studentInfoMapper.lockByUserId(s.getStudentId());
        Long acceptedOther = selectionMapper.selectCount(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getStudentId, s.getStudentId())
                .eq(Selection::getStatus, Selection.STATUS_ACCEPTED)
                .ne(Selection::getId, s.getId()));
        if (acceptedOther > 0) {
            throw new BusinessException("该学员已被其他导师接收");
        }
        long accepted = selectionMapper.selectCount(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getMentorId, mentorId)
                .eq(Selection::getStatus, Selection.STATUS_ACCEPTED));
        int quota = mi.getMaxQuota() == null ? 0 : mi.getMaxQuota();
        if (accepted >= quota) {
            throw new BusinessException("名额已满（上限 " + quota + " 人）");
        }
        s.setStatus(Selection.STATUS_ACCEPTED);
        s.setHandleTime(LocalDateTime.now());
        selectionMapper.updateById(s);
        log.info("导师 {} 接受学员申请 {}（已接受 {}/{}）", mentorId, selectionId, accepted + 1, quota);
    }

    @Override
    public void reject(Long mentorId, Long selectionId, String remark) {
        Selection s = selectionMapper.selectById(selectionId);
        if (s == null || !s.getMentorId().equals(mentorId)) {
            throw new BusinessException("申请不存在");
        }
        if (s.getStatus() != Selection.STATUS_PENDING) {
            throw new BusinessException("该申请已处理，请勿重复操作");
        }
        s.setStatus(Selection.STATUS_REJECTED);
        TextUtil.ensureLen("拒绝原因", remark, 200);
        s.setRemark(TextUtil.blankToNull(remark));
        s.setHandleTime(LocalDateTime.now());
        selectionMapper.updateById(s);
        log.info("导师 {} 拒绝学员申请 {}", mentorId, selectionId);
    }
}
