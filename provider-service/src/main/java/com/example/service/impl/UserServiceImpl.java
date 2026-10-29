package com.example.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.dao.mapper.UserMapper;
import com.example.dao.model.User;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    // 从配置文件中读取路径
    @Value("${file.export.path}")
    private String exportPath;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> selectAllUsers() {
        return userMapper.selectAllUsers();
    }

    @Override
    public User selectUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public void batchInser(List<User> users) {
        userMapper.batchInsert(users);
    }

    @Override
    @Transactional(readOnly = true) // 必须开启事务，Cursor 才能保持连接不关闭
    public void exportBigDataToExcel(HttpServletResponse response) throws IOException {
        // 1. 确保目标文件夹存在，如果不存在则自动创建
        File dir = new File(exportPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 2. 拼接完整的磁盘文件物理路径
        String fullFilePath = exportPath + File.separator + "百万级数据流式导出.xlsx";
        // 3. 初始化 EasyExcel 写对象
        try (ExcelWriter excelWriter = EasyExcel.write(fullFilePath, User.class).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("用户大表").build();

            List<User> batchList = new ArrayList<>(2000);
            long totalCount = 0;
            long startTime = System.currentTimeMillis();

            // 4. 通过游标流式获取数据库数据
            try (Cursor<User> cursor = userMapper.scanAllUsers()) {
                for (User user : cursor) {
                    batchList.add(user);
                    totalCount++;

                    // 每攒够 2000 条，写入一次 Excel，然后清空内存
                    if (batchList.size() >= 2000) {
                        excelWriter.write(batchList, writeSheet);
                        batchList.clear();
                    }
                }

                // 写入剩余不足 2000 条的数据
                if (!batchList.isEmpty()) {
                    excelWriter.write(batchList, writeSheet);
                }
            }

            log.info("百万级数据导出完成！总记录数：{}，耗时：{} ms", totalCount, (System.currentTimeMillis() - startTime));
        }
    }
}
