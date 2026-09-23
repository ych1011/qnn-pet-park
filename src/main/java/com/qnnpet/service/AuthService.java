package com.qnnpet.service;

import com.qnnpet.dto.LoginRequest;
import com.qnnpet.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout(String token);

    Object getCurrentUser(Long userId);
}