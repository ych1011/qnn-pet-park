package com.qnnpet.service;

import com.qnnpet.dto.AssignPetRequest;
import com.qnnpet.entity.Pet;
import com.qnnpet.entity.PetType;

import java.util.List;

public interface PetService {

    List<PetType> listPetTypes();

    List<Object> listPetLevels(Long petTypeId);

    Pet assignPet(AssignPetRequest request, Long teacherId);

    Pet updatePet(Long id, AssignPetRequest request, Long teacherId);

    Pet getStudentPet(Long studentId);
}