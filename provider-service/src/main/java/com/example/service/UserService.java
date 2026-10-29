package com.example.service;

import com.example.dao.model.User;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface UserService {

    List<User> selectAllUsers();

    User selectUserById(Long id);

    void batchInser(List<User> users);

    void exportBigDataToExcel(HttpServletResponse response) throws IOException;
}
