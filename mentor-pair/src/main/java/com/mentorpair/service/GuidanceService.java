package com.mentorpair.service;

import com.mentorpair.common.PageVO;
import com.mentorpair.dto.GuidanceVO;

public interface GuidanceService {

    PageVO<GuidanceVO> voPage(int page, int size, Long mentorId, Long studentId);

    void add(Long mentorId, Long submissionId, String content);

    void delete(Long id);
}
