package com.mentorpair.service;

import com.mentorpair.common.PageVO;
import com.mentorpair.dto.SubmissionVO;
import com.mentorpair.entity.Selection;
import com.mentorpair.entity.Submission;
import org.springframework.web.multipart.MultipartFile;

public interface SubmissionService {

    PageVO<SubmissionVO> voPage(int page, int size, Long mentorId, Long studentId);

    Submission getById(Long id);

    Selection getSelectionOfSubmission(Long submissionId);

    void upload(Long studentId, Long selectionId, String title, String description, MultipartFile file);

    void delete(Long id);
}
