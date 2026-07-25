package com.cvm.service;


import com.cvm.dto.UserCreateRequest;
import com.cvm.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(String id);
}