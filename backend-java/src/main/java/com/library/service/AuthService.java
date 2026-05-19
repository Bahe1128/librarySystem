package com.library.service;

import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.entity.User;

public interface AuthService {
    LoginResponse login(String username, String password);
    User register(RegisterRequest request);
}
