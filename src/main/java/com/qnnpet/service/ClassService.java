package com.qnnpet.service;

import com.qnnpet.entity.ClassInfo;

public interface ClassService {

    ClassInfo getCurrentClass(Long teacherId);

    ClassInfo createClass(ClassInfo classInfo, Long teacherId);

    ClassInfo updateClass(Long id, ClassInfo classInfo, Long teacherId);
}