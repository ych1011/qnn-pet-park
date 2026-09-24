package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.AssignPetRequest;
import com.qnnpet.entity.Pet;
import com.qnnpet.entity.PetLevelConfig;
import com.qnnpet.entity.PetType;
import com.qnnpet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物系统
 */
@Slf4j
@Tag(name = "老师-宠物系统", description = "宠物类型/等级查询、宠物分配、更换、查询")
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @Operation(summary = "查询宠物类型列表", description = "返回 6 种宠物类型")
    @GetMapping("/pet-types")
    public Result<List<PetType>> listPetTypes() {
        log.info("查询宠物类型列表");
        return Result.success(petService.listPetTypes());
    }

    @Operation(summary = "查询宠物等级配置", description = "返回指定宠物类型的 5 个等级配置")
    @GetMapping("/pet-types/{id}/levels")
    public Result<List<PetLevelConfig>> listPetLevels(@PathVariable Long id) {
        log.info("查询宠物等级配置: petTypeId={}", id);
        return Result.success(petService.listPetLevels(id));
    }

    @Operation(summary = "分配宠物", description = "给学生分配宠物；同一学生已有宠物时报错")
    @PostMapping("/pets")
    public Result<Pet> assignPet(@Valid @RequestBody AssignPetRequest request, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("分配宠物: teacherId={}, studentId={}, petTypeId={}",
                teacherId, request.getStudentId(), request.getPetTypeId());
        return Result.success(petService.assignPet(request, teacherId));
    }

    @Operation(summary = "更新宠物", description = "更换宠物类型时重置等级为1（保留积分），或修改自定义名称")
    @PutMapping("/pets/{id}")
    public Result<Pet> updatePet(@PathVariable Long id, @Valid @RequestBody AssignPetRequest request,
                                  Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("更新宠物: id={}, teacherId={}, studentId={}", id, teacherId, request.getStudentId());
        return Result.success(petService.updatePet(id, request, teacherId));
    }

    @Operation(summary = "查询学生宠物", description = "返回指定学生的宠物信息")
    @GetMapping("/students/{studentId}/pet")
    public Result<Pet> getStudentPet(@PathVariable Long studentId, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询学生宠物: studentId={}, teacherId={}", studentId, teacherId);
        return Result.success(petService.getStudentPet(studentId, teacherId));
    }
}