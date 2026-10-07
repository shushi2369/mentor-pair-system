package com.mentorpair.service;

import com.mentorpair.common.PageVO;
import com.mentorpair.dto.SelectionVO;
import com.mentorpair.entity.Selection;

public interface SelectionService {

    PageVO<SelectionVO> voPage(int page, int size, Long mentorId, Long studentId, Integer status);

    Selection getById(Long id);

    void apply(Long studentId, Long mentorId, Long projectId);

    void accept(Long mentorId, Long selectionId);

    void reject(Long mentorId, Long selectionId, String remark);
}
