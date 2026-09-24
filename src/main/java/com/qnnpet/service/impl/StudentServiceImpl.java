package com.qnnpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.dto.CreateStudentRequest;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.entity.Pet;
import com.qnnpet.entity.ScoreLog;
import com.qnnpet.entity.Student;
import com.qnnpet.mapper.ClassInfoMapper;
import com.qnnpet.mapper.PetMapper;
import com.qnnpet.mapper.ScoreLogMapper;
import com.qnnpet.mapper.StudentMapper;
import com.qnnpet.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学生管理服务实现（PRD §5.4）
 * - 删除学生级联删除 pet + score_log
 * - 数据归属校验：teacher 只能查询/操作自己班级的学生
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final PetMapper petMapper;
    private final ScoreLogMapper scoreLogMapper;

    @Override
    public List<Student> listStudents(Long classId, Long teacherId) {
        log.info("查询学生列表: classId={}, teacherId={}", classId, teacherId);
        checkOwnership(classId, teacherId);
        return studentMapper.selectList(
                new QueryWrapper<Student>()
                        .eq("class_id", classId)
                        .orderByAsc("sort_order"));
    }

    @Override
    public Student createStudent(CreateStudentRequest request, Long teacherId) {
        log.info("添加学生: teacherId={}, name={}", teacherId, request.getName());
        ClassInfo cls = classInfoMapper.selectOne(
                new QueryWrapper<ClassInfo>().eq("teacher_id", teacherId));
        if (cls == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "请先创建班级");
        }
        Student student = new Student();
        student.setClassId(cls.getId());
        student.setName(request.getName());
        student.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        student.setStatus(1);
        studentMapper.insert(student);
        log.info("学生添加成功: id={}, classId={}, name={}", student.getId(), cls.getId(), student.getName());
        return student;
    }

    @Override
    public Student updateStudent(Long id, CreateStudentRequest request, Long teacherId) {
        log.info("编辑学生: id={}, teacherId={}", id, teacherId);
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        checkOwnership(student.getClassId(), teacherId);
        student.setName(request.getName());
        if (request.getSortOrder() != null) {
            student.setSortOrder(request.getSortOrder());
        }
        studentMapper.updateById(student);
        log.info("学生更新成功: id={}", id);
        return student;
    }

    @Override
    @Transactional
    public void deleteStudent(Long id, Long teacherId) {
        log.info("逻辑删除学生: id={}, teacherId={}", id, teacherId);
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        checkOwnership(student.getClassId(), teacherId);
        // 级联逻辑删除 pet + score_log（PRD §5.4，MyBatis-Plus @TableLogic 自动转 UPDATE SET deleted=1）
        petMapper.delete(new QueryWrapper<Pet>().eq("student_id", id));
        scoreLogMapper.delete(new QueryWrapper<ScoreLog>().eq("student_id", id));
        studentMapper.deleteById(id);
        log.info("学生逻辑删除成功（含级联宠物和积分记录）: id={}", id);
    }

    private void checkOwnership(Long classId, Long teacherId) {
        ClassInfo cls = classInfoMapper.selectById(classId);
        if (cls == null || !cls.getTeacherId().equals(teacherId)) {
            log.warn("越权操作学生数据: classId={}, teacherId={}", classId, teacherId);
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作其他老师的学生");
        }
    }
}
