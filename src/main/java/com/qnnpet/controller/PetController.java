package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.AssignPetRequest;
import com.qnnpet.entity.Pet;
import com.qnnpet.entity.PetType;
import com.qnnpet.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物系统
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping("/pet-types")
    public Result<List<PetType>> listPetTypes() {
        return Result.success(petService.listPetTypes());
    }

    @GetMapping("/pet-types/{id}/levels")
    public Result<List<Object>> listPetLevels(@PathVariable Long id) {
        return Result.success(petService.listPetLevels(id));
    }

    @PostMapping("/pets")
    public Result<Pet> assignPet(@Valid @RequestBody AssignPetRequest request, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        return Result.success(petService.assignPet(request, teacherId));
    }

    @PutMapping("/pets/{id}")
    public Result<Pet> updatePet(@PathVariable Long id, @Valid @RequestBody AssignPetRequest request, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        return Result.success(petService.updatePet(id, request, teacherId));
    }

    @GetMapping("/students/{studentId}/pet")
    public Result<Pet> getStudentPet(@PathVariable Long studentId) {
        return Result.success(petService.getStudentPet(studentId));
    }
}