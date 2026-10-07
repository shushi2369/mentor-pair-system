package com.mentorpair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentorpair.common.BusinessException;
import com.mentorpair.common.PageVO;
import com.mentorpair.dto.ProjectVO;
import com.mentorpair.entity.Project;
import com.mentorpair.entity.Selection;
import com.mentorpair.mapper.ProjectMapper;
import com.mentorpair.mapper.SelectionMapper;
import com.mentorpair.service.ProjectService;
import com.mentorpair.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private SelectionMapper selectionMapper;

    @Override
    public PageVO<ProjectVO> voPage(int page, int size, Long mentorId, Integer status, String kw) {
        IPage<ProjectVO> p = projectMapper.selectVOPage(new Page<>(page, size), mentorId, status, kw);
        return PageVO.of(p);
    }

    @Override
    public void create(Long mentorId, String title, String description) {
        Project p = checkAndBuild(mentorId, null, title, description);
        p.setStatus(1);
        projectMapper.insert(p);
        log.info("导师 {} 发布项目：{}", mentorId, p.getTitle());
    }

    @Override
    public void update(Long id, Long mentorId, String title, String description) {
        Project p = checkAndBuild(mentorId, id, title, description);
        projectMapper.updateById(p);
        log.info("导师 {} 编辑项目 {}", mentorId, id);
    }

    @Override
    public void offline(Long id, Long mentorId) {
        Project p = requireProject(id, mentorId);
        p.setStatus(0);
        projectMapper.updateById(p);
        log.info("项目 {} 下架", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Project p = requireProject(id, null);
        projectMapper.deleteById(id);
        // 历史申请不再悬挂已删除的项目
        selectionMapper.update(null, new LambdaUpdateWrapper<Selection>()
                .eq(Selection::getProjectId, id).set(Selection::getProjectId, null));
        log.info("项目 {}（{}）删除", id, p.getTitle());
    }

    private Project requireProject(Long id, Long mentorId) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        if (mentorId != null && !p.getMentorId().equals(mentorId)) {
            throw new BusinessException("项目不存在");
        }
        return p;
    }

    private Project checkAndBuild(Long mentorId, Long id, String title, String description) {
        if (TextUtil.isBlank(title)) {
            throw new BusinessException("请填写项目名称");
        }
        if (title.trim().length() > 100) {
            throw new BusinessException("项目名称不能超过 100 字");
        }
        Project p;
        if (id == null) {
            p = new Project();
            p.setMentorId(mentorId);
        } else {
            p = requireProject(id, mentorId);
        }
        p.setTitle(title.trim());
        p.setDescription(TextUtil.blankToNull(description));
        return p;
    }
}
