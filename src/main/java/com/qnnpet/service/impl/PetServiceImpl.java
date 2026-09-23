package com.qnnpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.dto.AssignPetRequest;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.entity.Pet;
import com.qnnpet.entity.PetLevelConfig;
import com.qnnpet.entity.PetType;
import com.qnnpet.entity.Student;
import com.qnnpet.mapper.ClassInfoMapper;
import com.qnnpet.mapper.PetLevelConfigMapper;
import com.qnnpet.mapper.PetMapper;
import com.qnnpet.mapper.PetTypeMapper;
import com.qnnpet.mapper.StudentMapper;
import com.qnnpet.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 宠物系统服务实现（PRD §5.5）
 * - 6 种宠物类型 × 5 等级
 * - 更换宠物重置 level=1，保留 score
 */
@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final PetTypeMapper petTypeMapper;
    private final PetLevelConfigMapper petLevelConfigMapper;
    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;

    @Override
    public List<PetType> listPetTypes() {
        return petTypeMapper.selectList(
                new QueryWrapper<PetType>().orderByAsc("sort_order"));
    }

    @Override
    public List<Object> listPetLevels(Long petTypeId) {
        List<PetLevelConfig> list = petLevelConfigMapper.selectList(
                new QueryWrapper<PetLevelConfig>()
                        .eq("pet_type_id", petTypeId)
                        .orderByAsc("level"));
        return List.copyOf(list);
    }

    @Override
    public Pet assignPet(AssignPetRequest request, Long teacherId) {
        Student student = studentMapper.selectById(request.getStudentId());
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        checkOwnership(student.getClassId(), teacherId);
        if (petTypeMapper.selectById(request.getPetTypeId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "宠物类型不存在");
        }
        Pet existing = petMapper.selectOne(
                new QueryWrapper<Pet>().eq("student_id", request.getStudentId()));
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "该学生已有宠物，请使用更换功能");
        }
        Pet pet = new Pet();
        pet.setStudentId(request.getStudentId());
        pet.setPetTypeId(request.getPetTypeId());
        pet.setCustomName(request.getCustomName());
        pet.setCurrentLevel(1);
        pet.setCurrentScore(0);
        petMapper.insert(pet);
        return pet;
    }

    @Override
    public Pet updatePet(Long id, AssignPetRequest request, Long teacherId) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "宠物不存在");
        }
        Student student = studentMapper.selectById(pet.getStudentId());
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        checkOwnership(student.getClassId(), teacherId);
        if (request.getPetTypeId() != null) {
            if (petTypeMapper.selectById(request.getPetTypeId()) == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "宠物类型不存在");
            }
            pet.setPetTypeId(request.getPetTypeId());
            pet.setCurrentLevel(1); // 更换类型形态变回蛋，保留积分（PRD §5.5）
        }
        if (request.getCustomName() != null) {
            pet.setCustomName(request.getCustomName());
        }
        petMapper.updateById(pet);
        return pet;
    }

    @Override
    public Pet getStudentPet(Long studentId) {
        Pet pet = petMapper.selectOne(
                new QueryWrapper<Pet>().eq("student_id", studentId));
        if (pet == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "该学生尚未分配宠物");
        }
        return pet;
    }

    private void checkOwnership(Long classId, Long teacherId) {
        ClassInfo cls = classInfoMapper.selectById(classId);
        if (cls == null || !cls.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作其他老师的学生");
        }
    }
}