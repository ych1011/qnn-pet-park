package com.qnnpet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qnnpet.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetMapper extends BaseMapper<Pet> {
}