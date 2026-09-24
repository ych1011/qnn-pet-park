package com.qnnpet.service;

import java.util.List;
import java.util.Map;

/**
 * 课堂大屏展示服务（PRD §5.9）
 */
public interface DisplayService {

    /**
     * 获取全班宠物展示数据
     *
     * @param classId  班级ID
     * @param teacherId 当前老师ID（数据归属校验）
     */
    List<Map<String, Object>> getDisplayData(Long classId, Long teacherId);
}
