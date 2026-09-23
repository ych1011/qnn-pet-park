package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.AddScoreRequest;
import com.qnnpet.dto.AddScoreResponse;
import com.qnnpet.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 积分操作
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping("/scores")
    public Result<AddScoreResponse> addScore(@Valid @RequestBody AddScoreRequest request, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        return Result.success(scoreService.addScore(request, teacherId));
    }

    @PostMapping("/scores/undo")
    public Result<Void> undo(Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        scoreService.undoLatestScore(teacherId);
        return Result.success();
    }

    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> ranking(@RequestParam Long classId) {
        return Result.success(scoreService.getRanking(classId));
    }

    @GetMapping("/score-logs")
    public Result<Map<String, Object>> scoreLogs(@RequestParam Long studentId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(scoreService.getScoreLogs(studentId, page, size));
    }
}