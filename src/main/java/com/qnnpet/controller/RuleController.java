package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.entity.ScoreRule;
import com.qnnpet.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分规则
 */
@RestController
@RequestMapping("/api/teacher/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    public Result<List<ScoreRule>> list(@RequestParam Long classId) {
        return Result.success(ruleService.listRules(classId));
    }

    @PostMapping
    public Result<ScoreRule> create(@RequestBody ScoreRule rule, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        return Result.success(ruleService.createRule(rule, teacherId));
    }

    @PutMapping("/{id}")
    public Result<ScoreRule> update(@PathVariable Long id, @RequestBody ScoreRule rule, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        return Result.success(ruleService.updateRule(id, rule, teacherId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        ruleService.deleteRule(id, teacherId);
        return Result.success();
    }
}