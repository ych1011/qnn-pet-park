package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.RuleRequest;
import com.qnnpet.entity.ScoreRule;
import com.qnnpet.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "老师-积分规则", description = "规则增删改查；分值1-99，分类study/discipline/habit/morality")
@RestController
@RequestMapping("/api/teacher/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @Operation(summary = "查询积分规则列表", description = "按班级查询规则；只能查询自己班级的规则")
    @GetMapping
    public Result<List<ScoreRule>> list(@RequestParam Long classId, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询积分规则列表: classId={}, teacherId={}", classId, teacherId);
        return Result.success(ruleService.listRules(classId, teacherId));
    }

    @Operation(summary = "新增积分规则", description = "新增规则；分值1-99，类型add/subtract，分类study/discipline/habit/morality")
    @PostMapping
    public Result<ScoreRule> create(@Valid @RequestBody RuleRequest rule, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("新增积分规则: teacherId={}, classId={}, name={}", teacherId, rule.getClassId(), rule.getName());
        return Result.success(ruleService.createRule(rule, teacherId));
    }

    @Operation(summary = "编辑积分规则", description = "更新规则名称/类型/分值/分类/排序/状态")
    @PutMapping("/{id}")
    public Result<ScoreRule> update(@PathVariable Long id, @Valid @RequestBody RuleRequest rule,
                                     Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("编辑积分规则: id={}, teacherId={}", id, teacherId);
        return Result.success(ruleService.updateRule(id, rule, teacherId));
    }

    @Operation(summary = "删除积分规则", description = "删除规则；历史积分记录保留 rule_name 快照不受影响")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("删除积分规则: id={}, teacherId={}", id, teacherId);
        ruleService.deleteRule(id, teacherId);
        return Result.success();
    }
}