package com.qnnpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.dto.RuleRequest;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.entity.ScoreRule;
import com.qnnpet.mapper.ClassInfoMapper;
import com.qnnpet.mapper.ScoreRuleMapper;
import com.qnnpet.service.RuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 积分规则服务实现（PRD §5.6）
 * - 分值 1-99 正整数（已由 DTO @Min/@Max 校验）
 * - 类型 add/subtract（已由 DTO @Pattern 校验）
 * - 分类 study/discipline/habit/morality（已由 DTO @Pattern 校验）
 * - 删除规则保留历史记录（记录中有 rule_name 快照）
 * - 数据归属校验：teacher 只能操作自己班级的规则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

    private final ScoreRuleMapper scoreRuleMapper;
    private final ClassInfoMapper classInfoMapper;

    @Override
    public List<ScoreRule> listRules(Long classId, Long teacherId) {
        log.info("查询积分规则列表: classId={}, teacherId={}", classId, teacherId);
        checkOwnership(classId, teacherId);
        return scoreRuleMapper.selectList(
                new QueryWrapper<ScoreRule>()
                        .eq("class_id", classId)
                        .orderByAsc("sort_order"));
    }

    @Override
    public ScoreRule createRule(RuleRequest rule, Long teacherId) {
        log.info("新增积分规则: teacherId={}, classId={}, name={}", teacherId, rule.getClassId(), rule.getName());
        checkOwnership(rule.getClassId(), teacherId);
        ScoreRule entity = new ScoreRule();
        entity.setClassId(rule.getClassId());
        entity.setName(rule.getName());
        entity.setType(rule.getType());
        entity.setScore(rule.getScore());
        entity.setCategory(rule.getCategory());
        entity.setSortOrder(rule.getSortOrder() != null ? rule.getSortOrder() : 0);
        entity.setStatus(rule.getStatus() != null ? rule.getStatus() : 1);
        scoreRuleMapper.insert(entity);
        log.info("积分规则创建成功: id={}, classId={}", entity.getId(), rule.getClassId());
        return entity;
    }

    @Override
    public ScoreRule updateRule(Long id, RuleRequest rule, Long teacherId) {
        log.info("编辑积分规则: id={}, teacherId={}", id, teacherId);
        ScoreRule existing = scoreRuleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "规则不存在");
        }
        checkOwnership(existing.getClassId(), teacherId);
        existing.setName(rule.getName());
        existing.setType(rule.getType());
        existing.setScore(rule.getScore());
        existing.setCategory(rule.getCategory());
        existing.setSortOrder(rule.getSortOrder() != null ? rule.getSortOrder() : existing.getSortOrder());
        if (rule.getStatus() != null) {
            existing.setStatus(rule.getStatus());
        }
        scoreRuleMapper.updateById(existing);
        log.info("积分规则更新成功: id={}", id);
        return existing;
    }

    @Override
    public void deleteRule(Long id, Long teacherId) {
        log.info("删除积分规则: id={}, teacherId={}", id, teacherId);
        ScoreRule existing = scoreRuleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "规则不存在");
        }
        checkOwnership(existing.getClassId(), teacherId);
        scoreRuleMapper.deleteById(id);
        log.info("积分规则删除成功: id={}", id);
    }

    private void checkOwnership(Long classId, Long teacherId) {
        ClassInfo cls = classInfoMapper.selectById(classId);
        if (cls == null || !cls.getTeacherId().equals(teacherId)) {
            log.warn("越权操作积分规则: classId={}, teacherId={}", classId, teacherId);
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作其他老师的规则");
        }
    }
}
