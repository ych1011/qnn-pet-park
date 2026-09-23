package com.qnnpet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * QNN的宠物乐园 - 启动类
 */
@SpringBootApplication
@MapperScan("com.qnnpet.mapper")
public class QnnPetParkApplication {

    public static void main(String[] args) {
        SpringApplication.run(QnnPetParkApplication.class, args);
    }
}