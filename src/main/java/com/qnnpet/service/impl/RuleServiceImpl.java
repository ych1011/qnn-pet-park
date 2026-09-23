package com.qnnpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.entity.ScoreRule;
import com.qnnpet.mapper.ClassInfoMapper;
import com.qnnpet.mapper.ScoreRuleMapper;
import com.qnnpet.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 积分规则服务实现（PRD §5.6）
 * - 分值 1-99 正整数
 * - 删除规则保留历史记录（记录中有 rule_name 快照）
 */
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

    private final ScoreRuleMapper scoreRuleMapper;
    private final ClassInfoMapper classInfoMapper;

    @Override
    public List<ScoreRule> listRules(Long classId) {
        return scoreRuleMapper.selectList(
                new QueryWrapper<ScoreRule>()
                        .eq("class_id", classId)
                        .orderByAsc("sort_order"));
    }

    @Override
    public ScoreRule createRule(ScoreRule rule, Long teacherId) {
        checkOwnership(rule.getClassId(), teacherId);
        validateRule(rule);
        rule.setStatus(1);
        scoreRuleMapper.insert(rule);
        return rule;
    }

    @Override
    public ScoreRule updateRule(Long id, ScoreRule rule, Long teacherId) {
        ScoreRule existing = scoreRuleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "规则不存在");
        }
        checkOwnership(existing.getClassId(), teacherId);
        validateRule(rule);
        existing.setName(rule.getName());
        existing.setType(rule.getType());
        existing.setScore(rule.getScore());
        existing.setCategory(rule.getCategory());
        existing.setSortOrder(rule.getSortOrder());
        if (rule.getStatus() != null) {
            existing.setStatus(rule.getStatus());
        }
        scoreRuleMapper.updateById(existing);
        return existing;
    }

    @Override
    public void deleteRule(Long id, Long teacherId) {
        ScoreRule existing = scoreRuleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "规则不存在");
        }
        checkOwnership(existing.getClassId(), teacherId);
        scoreRuleMapper.deleteById(id);
    }

    private void validateRule(ScoreRule rule) {
        if (rule.getScore() == null || rule.getScore() < 1 || rule.getScore() > 99) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "分值必须为1-99的正整数");
        }
        if (!"add".equals(rule.getType()) && !"subtract".equals(rule.getType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "类型必须为 add 或 subtract");
        }
    }

    private void checkOwnership(Long classId, Long teacherId) {
        ClassInfo cls = classInfoMapper.selectById(classId);
        if (cls == null || !cls.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作其他老师的规则");
        }
    }
}