package com.qnnpet.service;

import com.qnnpet.dto.RuleRequest;
import com.qnnpet.entity.ScoreRule;

import java.util.List;

public interface RuleService {

    List<ScoreRule> listRules(Long classId, Long teacherId);

    ScoreRule createRule(RuleRequest rule, Long teacherId);

    ScoreRule updateRule(Long id, RuleRequest rule, Long teacherId);

    void deleteRule(Long id, Long teacherId);
}
