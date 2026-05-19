package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    @Select("SELECT br.*, b.title, b.author, u.username " +
            "FROM borrow_records br " +
            "JOIN books b ON br.book_id = b.id " +
            "JOIN users u ON br.user_id = u.id " +
            "WHERE br.user_id = #{userId} " +
            "ORDER BY br.borrow_date DESC")
    List<BorrowRecord> findByUserIdWithBookInfo(@Param("userId") Long userId);

    @Select("SELECT br.*, b.title, b.author, u.username " +
            "FROM borrow_records br " +
            "JOIN books b ON br.book_id = b.id " +
            "JOIN users u ON br.user_id = u.id " +
            "ORDER BY br.borrow_date DESC")
    List<BorrowRecord> findAllWithBookInfo();
}
