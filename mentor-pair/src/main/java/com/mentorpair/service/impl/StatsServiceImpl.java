package com.mentorpair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mentorpair.dto.LoginUser;
import com.mentorpair.entity.Project;
import com.mentorpair.entity.Selection;
import com.mentorpair.entity.Submission;
import com.mentorpair.entity.User;
import com.mentorpair.entity.Course;
import com.mentorpair.entity.Guidance;
import com.mentorpair.mapper.CourseMapper;
import com.mentorpair.mapper.GuidanceMapper;
import com.mentorpair.mapper.ProjectMapper;
import com.mentorpair.mapper.SelectionMapper;
import com.mentorpair.mapper.SubmissionMapper;
import com.mentorpair.mapper.UserMapper;
import com.mentorpair.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private SelectionMapper selectionMapper;
    @Autowired
    private SubmissionMapper submissionMapper;
    @Autowired
    private GuidanceMapper guidanceMapper;
    @Autowired
    private CourseMapper courseMapper;

    @Override
    public Map<String, Long> stats() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("mentors", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, LoginUser.ROLE_MENTOR).eq(User::getStatus, 1)));
        m.put("students", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, LoginUser.ROLE_STUDENT).eq(User::getStatus, 1)));
        m.put("openProjects", projectMapper.selectCount(new LambdaQueryWrapper<Project>()
                .eq(Project::getStatus, 1)));
        m.put("pendingSelections", selectionMapper.selectCount(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getStatus, Selection.STATUS_PENDING)));
        m.put("acceptedSelections", selectionMapper.selectCount(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getStatus, Selection.STATUS_ACCEPTED)));
        m.put("submissions", submissionMapper.selectCount(null));
        m.put("guidances", guidanceMapper.selectCount(null));
        m.put("courses", courseMapper.selectCount(null));
        return m;
    }
}
