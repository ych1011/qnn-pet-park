package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.AddScoreRequest;
import com.qnnpet.dto.AddScoreResponse;
import com.qnnpet.service.ScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 积分操作
 */
@Slf4j
@Tag(name = "老师-积分操作", description = "加扣分、撤销、排行榜、积分记录")
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    /** 分页 size 上限：防止恶意请求超大页 */
    private static final int MAX_PAGE_SIZE = 100;
    /** 分页 size 下限 */
    private static final int MIN_PAGE_SIZE = 1;
    /** 分页 page 下限 */
    private static final int MIN_PAGE_NUM = 1;

    @Operation(summary = "积分加减", description = "对学生加/扣分，自动更新宠物积分和等级；扣分为负时归0不降级；幂等键防重复")
    @PostMapping("/scores")
    public Result<AddScoreResponse> addScore(@Valid @RequestBody AddScoreRequest request, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("积分操作: teacherId={}, studentId={}, type={}, score={}, ruleName={}",
                teacherId, request.getStudentId(), request.getType(), request.getScore(), request.getRuleName());
        return Result.success(scoreService.addScore(request, teacherId));
    }

    @Operation(summary = "撤销最近一次操作", description = "撤销当前老师最近一次加/扣分，10 秒内有效")
    @PostMapping("/scores/undo")
    public Result<Void> undo(Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("撤销最近一次积分操作: teacherId={}", teacherId);
        scoreService.undoLatestScore(teacherId);
        return Result.success();
    }

    @Operation(summary = "查询积分排行榜", description = "按班级查询学生积分排行榜")
    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> ranking(@RequestParam Long classId, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询积分排行: classId={}, teacherId={}", classId, teacherId);
        return Result.success(scoreService.getRanking(classId, teacherId));
    }

    @Operation(summary = "查询积分记录", description = "分页查询指定学生的积分记录；size 上限 100")
    @GetMapping("/score-logs")
    public Result<Map<String, Object>> scoreLogs(
            @Parameter(description = "学生ID", required = true) @RequestParam Long studentId,
            @Parameter(description = "页码，从1开始", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数，1-100", example = "20") @RequestParam(defaultValue = "20") Integer size,
            Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        // 分页参数防御性校验：防止恶意请求超大页导致 OOM 或慢查询
        if (page == null || page < MIN_PAGE_NUM) {
            page = MIN_PAGE_NUM;
        }
        if (size == null || size < MIN_PAGE_SIZE) {
            size = 20;
        } else if (size > MAX_PAGE_SIZE) {
            log.warn("分页 size 超限被截断: studentId={}, 请求size={}, 截断为={}",
                    studentId, size, MAX_PAGE_SIZE);
            size = MAX_PAGE_SIZE;
        }
        log.info("查询积分记录: studentId={}, page={}, size={}, teacherId={}", studentId, page, size, teacherId);
        return Result.success(scoreService.getScoreLogs(studentId, page, size, teacherId));
    }
}