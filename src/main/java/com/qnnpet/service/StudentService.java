package com.qnnpet.service;

import com.qnnpet.dto.CreateStudentRequest;
import com.qnnpet.entity.Student;

import java.util.List;

public interface StudentService {

    List<Student> listStudents(Long classId, Long teacherId);

    Student createStudent(CreateStudentRequest request, Long teacherId);

    Student updateStudent(Long id, CreateStudentRequest request, Long teacherId);

    void deleteStudent(Long id, Long teacherId);
}
