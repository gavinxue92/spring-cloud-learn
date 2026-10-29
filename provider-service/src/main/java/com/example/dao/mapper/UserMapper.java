package com.example.dao.mapper;

import com.example.dao.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;

import java.util.List;

@Mapper
public interface UserMapper {

    List<User> selectAllUsers();

    User selectById(@Param("id")Long id);

    void batchInsert(List<User> users);

    /**
     * 流式查询：返回 Cursor，支持百万级数据遍历而不 OOM
     * 利用 MySQL 的流式查询特性（useCursorFetch=true），不会把所有数据压在内存里。
     */
    @Options(resultSetType = org.apache.ibatis.mapping.ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    Cursor<User> scanAllUsers();
}
