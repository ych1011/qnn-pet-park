package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.RuleRequest;
import com.qnnpet.entity.ScoreRule;
import com.qnnpet.service.RuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分规则
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    public Result<List<ScoreRule>> list(@RequestParam Long classId, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询积分规则列表: classId={}, teacherId={}", classId, teacherId);
        return Result.success(ruleService.listRules(classId, teacherId));
    }

    @PostMapping
    public Result<ScoreRule> create(@Valid @RequestBody RuleRequest rule, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("新增积分规则: teacherId={}, classId={}, name={}", teacherId, rule.getClassId(), rule.getName());
        return Result.success(ruleService.createRule(rule, teacherId));
    }

    @PutMapping("/{id}")
    public Result<ScoreRule> update(@PathVariable Long id, @Valid @RequestBody RuleRequest rule,
                                     Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("编辑积分规则: id={}, teacherId={}", id, teacherId);
        return Result.success(ruleService.updateRule(id, rule, teacherId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("删除积分规则: id={}, teacherId={}", id, teacherId);
        ruleService.deleteRule(id, teacherId);
        return Result.success();
    }
}
