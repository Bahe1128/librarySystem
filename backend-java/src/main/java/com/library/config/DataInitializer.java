package com.library.config;

import com.library.entity.Book;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, BookMapper bookMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.bookMapper = bookMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("admin");
            userMapper.insert(admin);
            System.out.println("默认管理员创建成功 (用户名: admin, 密码: admin123)");
        }

        if (bookMapper.selectCount(null) == 0) {
            Book[] sampleBooks = {
                createBook("JavaScript高级程序设计", "Nicholas C. Zakas", "9787115275790", "计算机", 5),
                createBook("React实战", "Alex Banks", "9787115489549", "计算机", 3),
                createBook("深入理解计算机系统", "Randal E. Bryant", "9787111447447", "计算机", 4),
                createBook("算法导论", "Thomas H. Cormen", "9787111407010", "计算机", 3),
                createBook("三体", "刘慈欣", "9787536692930", "科幻", 6)
            };
            for (Book book : sampleBooks) {
                bookMapper.insert(book);
            }
            System.out.println("已添加 " + sampleBooks.length + " 本示例图书");
        }
    }

    private Book createBook(String title, String author, String isbn, String category, int copies) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);
        return book;
    }
}
