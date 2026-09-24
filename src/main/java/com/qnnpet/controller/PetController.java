package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.AssignPetRequest;
import com.qnnpet.entity.Pet;
import com.qnnpet.entity.PetLevelConfig;
import com.qnnpet.entity.PetType;
import com.qnnpet.service.PetService;
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
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping("/pet-types")
    public Result<List<PetType>> listPetTypes() {
        log.info("查询宠物类型列表");
        return Result.success(petService.listPetTypes());
    }

    @GetMapping("/pet-types/{id}/levels")
    public Result<List<PetLevelConfig>> listPetLevels(@PathVariable Long id) {
        log.info("查询宠物等级配置: petTypeId={}", id);
        return Result.success(petService.listPetLevels(id));
    }

    @PostMapping("/pets")
    public Result<Pet> assignPet(@Valid @RequestBody AssignPetRequest request, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("分配宠物: teacherId={}, studentId={}, petTypeId={}",
                teacherId, request.getStudentId(), request.getPetTypeId());
        return Result.success(petService.assignPet(request, teacherId));
    }

    @PutMapping("/pets/{id}")
    public Result<Pet> updatePet(@PathVariable Long id, @Valid @RequestBody AssignPetRequest request,
                                  Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("更新宠物: id={}, teacherId={}, studentId={}", id, teacherId, request.getStudentId());
        return Result.success(petService.updatePet(id, request, teacherId));
    }

    @GetMapping("/students/{studentId}/pet")
    public Result<Pet> getStudentPet(@PathVariable Long studentId, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询学生宠物: studentId={}, teacherId={}", studentId, teacherId);
        return Result.success(petService.getStudentPet(studentId, teacherId));
    }
}
