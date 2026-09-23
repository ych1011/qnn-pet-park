package com.qnnpet.service;

import com.qnnpet.entity.ScoreRule;

import java.util.List;

public interface RuleService {

    List<ScoreRule> listRules(Long classId);

    ScoreRule createRule(ScoreRule rule, Long teacherId);

    ScoreRule updateRule(Long id, ScoreRule rule, Long teacherId);

    void deleteRule(Long id, Long teacherId);
}