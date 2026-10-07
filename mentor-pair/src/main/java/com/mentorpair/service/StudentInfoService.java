package com.mentorpair.service;

import com.mentorpair.entity.StudentInfo;

public interface StudentInfoService {

    StudentInfo getByUserId(Long userId);
}
