package com.mentorpair.service;

import com.mentorpair.common.PageVO;
import com.mentorpair.dto.LoginUser;
import com.mentorpair.entity.User;

public interface UserService {

    LoginUser login(String username, String password);

    PageVO<User> page(int page, int size, Integer role, String kw);

    void create(String username, String password, String realName, Integer role, String phone,
                Integer maxQuota, String studentNo, String major, String className);

    void update(Long id, String realName, String phone, Integer maxQuota);

    void changeStatus(Long id, Integer status);

    void resetPassword(Long id);

    void changePassword(Long id, String oldPassword, String newPassword);

    /** 批量导入学员账号（逐行容错），返回面向用户的结果消息 */
    String batchImportStudents(java.util.List<com.mentorpair.util.ExcelUtil.StudentRow> rows);
}
