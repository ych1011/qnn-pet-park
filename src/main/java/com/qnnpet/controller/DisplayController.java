package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.service.DisplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "老师-课堂大屏", description = "课堂大屏聚合展示数据")
@RestController
@RequestMapping("/api/teacher/display")
@RequiredArgsConstructor
public class DisplayController {

    private final DisplayService displayService;

    @Operation(summary = "查询大屏展示数据", description = "返回全班学生的宠物信息（含等级、积分、图片URL）")
    @GetMapping("/{classId}")
    public Result<List<Map<String, Object>>> display(@PathVariable Long classId, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询课堂大屏数据: classId={}, teacherId={}", classId, teacherId);
        return Result.success(displayService.getDisplayData(classId, teacherId));
    }
}