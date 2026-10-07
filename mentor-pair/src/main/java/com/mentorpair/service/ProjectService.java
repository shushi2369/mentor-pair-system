package com.mentorpair.service;

import com.mentorpair.common.PageVO;
import com.mentorpair.dto.ProjectVO;

public interface ProjectService {

    PageVO<ProjectVO> voPage(int page, int size, Long mentorId, Integer status, String kw);

    void create(Long mentorId, String title, String description);

    void update(Long id, Long mentorId, String title, String description);

    void offline(Long id, Long mentorId);

    void delete(Long id);
}
