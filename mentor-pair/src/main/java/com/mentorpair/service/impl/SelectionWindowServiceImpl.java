package com.mentorpair.service.impl;

import com.mentorpair.common.BusinessException;
import com.mentorpair.dto.WindowVO;
import com.mentorpair.entity.SelectionWindow;
import com.mentorpair.mapper.SelectionWindowMapper;
import com.mentorpair.service.SelectionWindowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class SelectionWindowServiceImpl implements SelectionWindowService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private SelectionWindowMapper selectionWindowMapper;

    public SelectionWindow get() {
        SelectionWindow w = selectionWindowMapper.selectById(1L);
        if (w == null) {
            w = new SelectionWindow();
            w.setId(1L);
            w.setEnabled(0);
            selectionWindowMapper.insert(w);
        }
        return w;
    }

    @Override
    public WindowVO status() {
        SelectionWindow w = get();
        WindowVO vo = new WindowVO();
        vo.setEnabled(w.getEnabled());
        String reason = closedReason(LocalDateTime.now());
        vo.setOpen(reason == null);
        vo.setStartTime(w.getStartTime() == null ? null : w.getStartTime().format(FMT));
        vo.setEndTime(w.getEndTime() == null ? null : w.getEndTime().format(FMT));
        if (reason != null) {
            vo.setMessage(reason);
        } else if (w.getEnabled() == 1 && w.getEndTime() != null) {
            vo.setMessage("双选进行中，截止时间：" + vo.getEndTime());
        }
        return vo;
    }

    /** 未开放原因；null 表示开放。时间窗未启用时始终开放 */
    public String closedReason(LocalDateTime now) {
        SelectionWindow w = get();
        if (w.getEnabled() == null || w.getEnabled() != 1) {
            return null;
        }
        if (w.getStartTime() != null && now.isBefore(w.getStartTime())) {
            return "双选尚未开始，开始时间：" + w.getStartTime().format(FMT);
        }
        if (w.getEndTime() != null && now.isAfter(w.getEndTime())) {
            return "双选已截止（截止时间：" + w.getEndTime().format(FMT) + "），无法提交申请";
        }
        return null;
    }

    @Override
    @Transactional
    public void save(Integer enabled, LocalDateTime startTime, LocalDateTime endTime) {
        int on = enabled != null && enabled == 1 ? 1 : 0;
        if (on == 1) {
            if (startTime == null || endTime == null) {
                throw new BusinessException("启用时间窗时，开始时间与截止时间都必须填写");
            }
            if (!startTime.isBefore(endTime)) {
                throw new BusinessException("开始时间必须早于截止时间");
            }
        }
        SelectionWindow w = get();
        w.setEnabled(on);
        w.setStartTime(startTime);
        w.setEndTime(endTime);
        selectionWindowMapper.updateById(w);
        log.info("管理员更新双选时间窗：enabled={}, {} ~ {}", on, startTime, endTime);
    }
}
