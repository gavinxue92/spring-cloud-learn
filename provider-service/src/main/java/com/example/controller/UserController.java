package com.example.controller;

import com.alibaba.excel.EasyExcel;
import com.example.dao.model.User;
import com.example.listener.UserDataListener;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("excelImportThreadPool")
    private ThreadPoolExecutor excelImportThreadPool;

    @GetMapping("/user/all")
    public List<User> getAllUsers() {
        return userService.selectAllUsers();
    }

    /**
     * 导出数据库用户数据到 Excel
     */
    @GetMapping("/export")
    public void exportUserData(HttpServletResponse response) throws IOException {
        // 1. 设置响应头格式，告诉浏览器这是一个 Excel 文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 防止中文文件名乱码
        String fileName = URLEncoder.encode("用户数据报表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 2. 从数据库中获取所有用户数据
        List<User> userList = userService.selectAllUsers();

        // 3. 使用 EasyExcel 写入到输出流中
        EasyExcel.write(response.getOutputStream(), User.class)
                .sheet("用户列表") // 设置 Excel 的 Sheet 页名称
                .doWrite(userList);   // 传入数据列表并完成写入
    }

    /**
     * 百万级数据流式导出接口
     */
    @GetMapping("/export-big-data")
    public void exportBigData(HttpServletResponse response) {
        try {
            userService.exportBigDataToExcel(response);
        } catch (IOException e) {
            // 注意：如果文件已经开始往前端写了一部分报错，HTTP 状态码可能无法修改，需结合全局异常处理
            throw new RuntimeException("导出大文件异常: " + e.getMessage(), e);
        }
    }

    /**
     * 上传 Excel 文件并批量导入数据库
     */
    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), User.class,
                            new UserDataListener(userService, excelImportThreadPool))
                    .sheet()
                    .doRead();
            return "Excel 数据导入成功！";
        } catch (IOException e) {
            log.error("Excel 导入失败", e);
            return "导入失败：" + e.getMessage();
        }
    }
}
