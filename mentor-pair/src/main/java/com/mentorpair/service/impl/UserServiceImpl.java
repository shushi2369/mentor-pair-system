package com.mentorpair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentorpair.common.BusinessException;
import com.mentorpair.common.PageVO;
import com.mentorpair.dto.LoginUser;
import com.mentorpair.entity.MentorInfo;
import com.mentorpair.entity.Selection;
import com.mentorpair.entity.StudentInfo;
import com.mentorpair.entity.User;
import com.mentorpair.mapper.MentorInfoMapper;
import com.mentorpair.mapper.SelectionMapper;
import com.mentorpair.mapper.StudentInfoMapper;
import com.mentorpair.mapper.UserMapper;
import com.mentorpair.service.UserService;
import com.mentorpair.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_RESET_PASSWORD = "123456";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MentorInfoMapper mentorInfoMapper;
    @Autowired
    private StudentInfoMapper studentInfoMapper;
    @Autowired
    private SelectionMapper selectionMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginUser login(String username, String password) {
        if (TextUtil.isBlank(username) || TextUtil.isBlank(password)) {
            throw new BusinessException("请输入登录名和密码");
        }
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username.trim()));
        if (u == null || !passwordEncoder.matches(password.trim(), u.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (u.getStatus() == null || u.getStatus() != 1) {
            throw new BusinessException("账号已停用，请联系管理员");
        }
        LoginUser lu = new LoginUser();
        lu.setId(u.getId());
        lu.setUsername(u.getUsername());
        lu.setRealName(u.getRealName());
        lu.setRole(u.getRole());
        log.info("用户登录：{}({})", u.getUsername(), u.getId());
        return lu;
    }

    @Override
    public PageVO<User> page(int page, int size, Integer role, String kw) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (role != null) {
            w.eq(User::getRole, role);
        } else {
            w.in(User::getRole, Arrays.asList(LoginUser.ROLE_MENTOR, LoginUser.ROLE_STUDENT));
        }
        if (!TextUtil.isBlank(kw)) {
            w.and(q -> q.like(User::getUsername, kw.trim()).or().like(User::getRealName, kw.trim()));
        }
        w.orderByAsc(User::getId);
        IPage<User> p = userMapper.selectPage(new Page<>(page, size), w);

        List<Long> mentorIds = p.getRecords().stream()
                .filter(u -> u.getRole() != null && u.getRole() == LoginUser.ROLE_MENTOR)
                .map(User::getId).collect(Collectors.toList());
        if (!mentorIds.isEmpty()) {
            Map<Long, Integer> quotaMap = mentorInfoMapper.selectList(
                            new LambdaQueryWrapper<MentorInfo>().in(MentorInfo::getUserId, mentorIds))
                    .stream().collect(Collectors.toMap(MentorInfo::getUserId, MentorInfo::getMaxQuota));
            for (User u : p.getRecords()) {
                if (u.getRole() != null && u.getRole() == LoginUser.ROLE_MENTOR) {
                    u.setMaxQuota(quotaMap.get(u.getId()));
                }
            }
        }
        return PageVO.of(p);
    }

    @Override
    @Transactional
    public void create(String username, String password, String realName, Integer role, String phone,
                       Integer maxQuota, String studentNo, String major, String className) {
        if (TextUtil.isBlank(username) || TextUtil.isBlank(password) || TextUtil.isBlank(realName)) {
            throw new BusinessException("登录名、密码、姓名不能为空");
        }
        if (password.trim().length() < 6) {
            throw new BusinessException("密码长度至少 6 位");
        }
        if (role == null || (role != LoginUser.ROLE_MENTOR && role != LoginUser.ROLE_STUDENT)) {
            throw new BusinessException("仅支持创建导师或学员账号");
        }
        TextUtil.ensureLen("登录名", username, 50);
        TextUtil.ensureLen("姓名", realName, 50);
        TextUtil.ensureLen("联系电话", phone, 20);
        TextUtil.ensureLen("学号", studentNo, 50);
        TextUtil.ensureLen("专业", major, 100);
        TextUtil.ensureLen("班级", className, 100);
        String name = username.trim();
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, name)) > 0) {
            throw new BusinessException("登录名已存在：" + name);
        }
        User u = new User();
        u.setUsername(name);
        u.setPassword(passwordEncoder.encode(password.trim()));
        u.setRealName(realName.trim());
        u.setRole(role);
        u.setPhone(TextUtil.blankToNull(phone));
        u.setStatus(1);
        try {
            userMapper.insert(u);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new BusinessException("登录名已存在：" + name);
        }

        if (role == LoginUser.ROLE_MENTOR) {
            MentorInfo mi = new MentorInfo();
            mi.setUserId(u.getId());
            mi.setMaxQuota(maxQuota == null || maxQuota < 0 ? 0 : maxQuota);
            mentorInfoMapper.insert(mi);
        } else {
            String no = TextUtil.isBlank(studentNo) ? name : studentNo.trim();
            if (studentInfoMapper.selectCount(
                    new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getStudentNo, no)) > 0) {
                throw new BusinessException("学号已存在：" + no);
            }
            StudentInfo si = new StudentInfo();
            si.setUserId(u.getId());
            si.setStudentNo(no);
            si.setMajor(TextUtil.blankToNull(major));
            si.setClassName(TextUtil.blankToNull(className));
            try {
                studentInfoMapper.insert(si);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                throw new BusinessException("学号已存在：" + no);
            }
        }
        log.info("管理员创建账号：{} 角色 {}", name, role);
    }

    @Override
    @Transactional
    public void update(Long id, String realName, String phone, Integer maxQuota) {
        User u = requireManagedUser(id);
        TextUtil.ensureLen("姓名", realName, 50);
        TextUtil.ensureLen("联系电话", phone, 20);
        if (!TextUtil.isBlank(realName)) {
            u.setRealName(realName.trim());
        }
        u.setPhone(TextUtil.blankToNull(phone));
        userMapper.updateById(u);
        if (u.getRole() == LoginUser.ROLE_MENTOR && maxQuota != null) {
            MentorInfo mi = mentorInfoMapper.selectOne(
                    new LambdaQueryWrapper<MentorInfo>().eq(MentorInfo::getUserId, id));
            if (mi != null) {
                mi.setMaxQuota(Math.max(maxQuota, 0));
                mentorInfoMapper.updateById(mi);
            }
        }
        log.info("管理员编辑账号：{}", id);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        User u = requireManagedUser(id);
        boolean disabling = status == null || status != 1;
        if (disabling) {
            // 停用前校验双选关系，避免孤儿申请导致学员/导师卡死
            if (u.getRole() == LoginUser.ROLE_MENTOR) {
                long accepted = selectionMapper.selectCount(new LambdaQueryWrapper<Selection>()
                        .eq(Selection::getMentorId, id)
                        .eq(Selection::getStatus, Selection.STATUS_ACCEPTED));
                if (accepted > 0) {
                    throw new BusinessException("该导师已有 " + accepted + " 名已接收学员，请先处理其双选申请后再停用");
                }
            } else {
                long active = selectionMapper.selectCount(new LambdaQueryWrapper<Selection>()
                        .eq(Selection::getStudentId, id)
                        .in(Selection::getStatus, Arrays.asList(Selection.STATUS_PENDING, Selection.STATUS_ACCEPTED)));
                if (active > 0) {
                    throw new BusinessException("该学员存在待处理或已接受的双选申请，不能停用");
                }
            }
        }
        u.setStatus(disabling ? 0 : 1);
        userMapper.updateById(u);
        log.info("管理员设置账号 {} 状态为 {}", id, u.getStatus());
    }

    @Override
    public void resetPassword(Long id) {
        User u = requireManagedUser(id);
        u.setPassword(passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
        userMapper.updateById(u);
        log.info("管理员重置账号 {} 密码", id);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        if (TextUtil.isBlank(oldPassword) || TextUtil.isBlank(newPassword)) {
            throw new BusinessException("请填写旧密码和新密码");
        }
        if (newPassword.trim().length() < 6) {
            throw new BusinessException("新密码长度至少 6 位");
        }
        User u = userMapper.selectById(id);
        if (u == null) {
            throw new BusinessException("账号不存在");
        }
        if (!passwordEncoder.matches(oldPassword.trim(), u.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        if (passwordEncoder.matches(newPassword, u.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        u.setPassword(passwordEncoder.encode(newPassword.trim()));
        userMapper.updateById(u);
        log.info("用户 {} 自助修改密码", u.getUsername());
    }

    @Override
    public String batchImportStudents(java.util.List<com.mentorpair.util.ExcelUtil.StudentRow> rows) {
        int fail = 0;
        List<String> errors = new java.util.ArrayList<>();
        for (com.mentorpair.util.ExcelUtil.StudentRow sr : rows) {
            try {
                create(sr.studentNo, TextUtil.isBlank(sr.password) ? "123456" : sr.password,
                        sr.studentName, LoginUser.ROLE_STUDENT, null, null, null, sr.major, sr.className);
            } catch (BusinessException e) {
                fail++;
                errors.add("第" + sr.line + "行（" + sr.studentNo + "）：" + e.getMessage());
            }
        }
        int success = rows.size() - fail;
        if (success == 0) {
            throw new BusinessException("导入失败，未创建任何账号。" + String.join("；", errors));
        }
        log.info("批量导入学员：成功 {} 条，失败 {} 条", success, fail);
        StringBuilder msg = new StringBuilder();
        msg.append("导入完成：成功 ").append(success).append(" 个，失败 ").append(fail).append(" 个");
        if (!errors.isEmpty()) {
            msg.append("（").append(String.join("；", errors)).append("）");
        }
        return msg.toString();
    }

    private User requireManagedUser(Long id) {
        User u = userMapper.selectById(id);
        if (u == null || u.getRole() == null || u.getRole() == LoginUser.ROLE_ADMIN) {
            throw new BusinessException("账号不存在");
        }
        return u;
    }
}
