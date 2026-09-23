package com.qnnpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.entity.ScoreRule;
import com.qnnpet.mapper.ClassInfoMapper;
import com.qnnpet.mapper.ScoreRuleMapper;
import com.qnnpet.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 班级管理服务实现（PRD §5.3）
 * - 一个老师只能有一个班级
 * - 创建班级时自动初始化 10 条默认规则（PRD §5.6）
 */
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassInfoMapper classInfoMapper;
    private final ScoreRuleMapper scoreRuleMapper;

    @Override
    public ClassInfo getCurrentClass(Long teacherId) {
        ClassInfo cls = classInfoMapper.selectOne(
                new QueryWrapper<ClassInfo>().eq("teacher_id", teacherId));
        if (cls == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "请先创建班级");
        }
        return cls;
    }

    @Override
    @Transactional
    public ClassInfo createClass(ClassInfo classInfo, Long teacherId) {
        ClassInfo existing = classInfoMapper.selectOne(
                new QueryWrapper<ClassInfo>().eq("teacher_id", teacherId));
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "每个老师只能创建一个班级");
        }
        classInfo.setTeacherId(teacherId);
        classInfoMapper.insert(classInfo);
        initDefaultRules(classInfo.getId());
        return classInfo;
    }

    @Override
    public ClassInfo updateClass(Long id, ClassInfo classInfo, Long teacherId) {
        ClassInfo existing = classInfoMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "班级不存在");
        }
        if (!existing.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作其他老师的班级");
        }
        existing.setName(classInfo.getName());
        existing.setGrade(classInfo.getGrade());
        existing.setSemester(classInfo.getSemester());
        classInfoMapper.updateById(existing);
        return existing;
    }

    /**
     * 初始化默认积分规则（PRD §5.6 预设 10 条）
     */
    private void initDefaultRules(Long classId) {
        List<ScoreRule> defaults = Arrays.asList(
                buildRule(classId, "举手回答问题", "add", 2, "study", 1),
                buildRule(classId, "回答正确", "add", 3, "study", 2),
                buildRule(classId, "作业优秀", "add", 5, "study", 3),
                buildRule(classId, "按时交作业", "add", 2, "study", 4),
                buildRule(classId, "认真听讲", "add", 2, "discipline", 5),
                buildRule(classId, "坐姿端正", "add", 1, "discipline", 6),
                buildRule(classId, "主动帮助同学", "add", 3, "morality", 7),
                buildRule(classId, "拾金不昧", "add", 5, "morality", 8),
                buildRule(classId, "上课讲话", "subtract", 1, "discipline", 9),
                buildRule(classId, "没交作业", "subtract", 3, "study", 10)
        );
        for (ScoreRule rule : defaults) {
            scoreRuleMapper.insert(rule);
        }
    }

    private ScoreRule buildRule(Long classId, String name, String type, int score, String category, int sortOrder) {
        ScoreRule rule = new ScoreRule();
        rule.setClassId(classId);
        rule.setName(name);
        rule.setType(type);
        rule.setScore(score);
        rule.setCategory(category);
        rule.setSortOrder(sortOrder);
        rule.setStatus(1);
        return rule;
    }
}