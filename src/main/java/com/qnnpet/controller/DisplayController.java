package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.service.DisplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 课堂大屏展示（PRD §5.9）
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/display")
@RequiredArgsConstructor
public class DisplayController {

    private final DisplayService displayService;

    @GetMapping("/{classId}")
    public Result<List<Map<String, Object>>> display(@PathVariable Long classId, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询课堂大屏数据: classId={}, teacherId={}", classId, teacherId);
        return Result.success(displayService.getDisplayData(classId, teacherId));
    }
}
