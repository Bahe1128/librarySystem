package com.library.config;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String[] FIRST_NAMES = {"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴",
            "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗"};
    private static final String[] LAST_NAMES = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
            "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞", "平", "刚", "桂英", "文", "华"};

    private static final String[] TECH_PREFIX = {"深入", "精通", "实战", "高级", "现代", "企业级", "分布式", "微服务",
            "云计算", "大数据", "人工智能", "机器学习", "深度学习", "自然语言", "计算机"};
    private static final String[] TECH_SUFFIX = {"编程", "架构", "设计", "开发", "实战指南", "技术手册", "应用实践",
            "系统设计", "算法", "网络", "数据库", "操作系统", "编译原理", "软件工程"};

    private static final String[] LIT_PREFIX = {"百年", "挪威的", "追风", "活着", "围城", "平凡", "人间",
            "深夜", "时间的", "无声", "长安", "雪国", "海边", "远山", "金色"};
    private static final String[] LIT_SUFFIX = {"孤独", "森林", "的人", "世界", "故事", "告白", "年华",
            "笔记", "岁月", "往事", "之歌", "序曲", "挽歌", "寓言", "童话"};

    private static final String[] SCI_PREFIX = {"量子", "相对论", "宇宙", "基因", "进化", "时间", "数学",
            "物理", "化学", "生物", "生态", "混沌", "暗物质", "引力", "熵"};
    private static final String[] SCI_SUFFIX = {"简史", "原理", "之谜", "探秘", "导论", "基础", "概论",
            "漫谈", "前沿", "哲学", "的故事", "起源", "未来", "之美", "密码"};

    private static final String[] CATEGORIES = {"计算机", "文学", "科幻", "科学", "历史", "哲学", "经济",
            "心理学", "艺术", "教育", "医学", "法律", "政治", "社会学", "语言学"};

    private static final Random RANDOM = new Random(42);

    public DataInitializer(UserMapper userMapper, BookMapper bookMapper,
                           BorrowRecordMapper borrowRecordMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.bookMapper = bookMapper;
        this.borrowRecordMapper = borrowRecordMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initAdmin();
        initBooks();
        initTestUsersAndBorrowRecords();
    }

    private void initAdmin() {
        if (userMapper.selectCount(null) == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("系统管理员");
            admin.setRole("admin");
            userMapper.insert(admin);
            System.out.println("默认管理员创建成功 (用户名: admin, 密码: admin123)");
        } else {
            User admin = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
            if (admin != null && admin.getName() == null) {
                admin.setName("系统管理员");
                userMapper.updateById(admin);
            }
        }
    }

    private void initBooks() {
        long count = bookMapper.selectCount(null);
        if (count >= 1000) return;

        int need = (int) (1000 - count);
        // Keep existing 5 sample books if fresh, otherwise just add random ones
        if (count == 0) {
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
            need -= sampleBooks.length;
        }

        Set<String> usedIsbns = new HashSet<>();
        for (int i = 0; i < need; i++) {
            String isbn;
            do {
                isbn = generateIsbn();
            } while (usedIsbns.contains(isbn));
            usedIsbns.add(isbn);

            String title = generateTitle();
            String author = generateAuthor();
            String category = CATEGORIES[RANDOM.nextInt(CATEGORIES.length)];
            int copies = RANDOM.nextInt(8) + 1;

            Book book = createBook(title, author, isbn, category, copies);
            bookMapper.insert(book);
        }
        System.out.println("已添加 " + need + " 本随机图书，当前共 1000 本");
    }

    private void initTestUsersAndBorrowRecords() {
        // Create test users if they don't exist
        User user1 = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "user1"));
        User user2 = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "user2"));

        if (user1 == null) {
            user1 = new User();
            user1.setUsername("user1");
            user1.setPassword(passwordEncoder.encode("123456"));
            user1.setName("张三");
            user1.setIdCard("110101199001011234");
            user1.setPhone("13800001111");
            user1.setEmail("zhangsan@example.com");
            user1.setRole("user");
            userMapper.insert(user1);
            System.out.println("测试用户创建: user1 / 123456 (张三)");
        }
        if (user2 == null) {
            user2 = new User();
            user2.setUsername("user2");
            user2.setPassword(passwordEncoder.encode("123456"));
            user2.setName("李四");
            user2.setIdCard("110101199002022345");
            user2.setPhone("13800002222");
            user2.setEmail("lisi@example.com");
            user2.setRole("user");
            userMapper.insert(user2);
            System.out.println("测试用户创建: user2 / 123456 (李四)");
        }

        // Create borrow records if user1 has none yet
        Long borrowCount = borrowRecordMapper.selectCount(
                new LambdaQueryWrapper<BorrowRecord>().eq(BorrowRecord::getUserId, user1.getId()));
        if (borrowCount == 0) {
            List<Book> allBooks = bookMapper.selectList(
                    new LambdaQueryWrapper<Book>().gt(Book::getAvailableCopies, 0));
            Collections.shuffle(allBooks, new Random(100));

            List<Book> booksForUser1 = allBooks.subList(0, Math.min(20, allBooks.size()));
            createBorrowRecordsForUser(user1.getId(), booksForUser1);

            List<Book> remaining = allBooks.subList(Math.min(20, allBooks.size()), allBooks.size());
            List<Book> booksForUser2 = remaining.subList(0, Math.min(20, remaining.size()));
            createBorrowRecordsForUser(user2.getId(), booksForUser2);

            System.out.println("已为测试用户各创建 20 条借阅记录");
        }
    }

    private void createBorrowRecordsForUser(Long userId, List<Book> books) {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            BorrowRecord record = new BorrowRecord();
            record.setUserId(userId);
            record.setBookId(book.getId());
            record.setBorrowDate(now.minusDays(RANDOM.nextInt(30) + 1));
            record.setStatus("borrowed");
            borrowRecordMapper.insert(record);

            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookMapper.updateById(book);
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

    private String generateTitle() {
        int type = RANDOM.nextInt(3);
        return switch (type) {
            case 0 -> TECH_PREFIX[RANDOM.nextInt(TECH_PREFIX.length)] + TECH_SUFFIX[RANDOM.nextInt(TECH_SUFFIX.length)];
            case 1 -> LIT_PREFIX[RANDOM.nextInt(LIT_PREFIX.length)] + LIT_SUFFIX[RANDOM.nextInt(LIT_SUFFIX.length)];
            default -> SCI_PREFIX[RANDOM.nextInt(SCI_PREFIX.length)] + SCI_SUFFIX[RANDOM.nextInt(SCI_SUFFIX.length)];
        };
    }

    private String generateAuthor() {
        return FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)] + LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
    }

    private String generateIsbn() {
        StringBuilder sb = new StringBuilder("978");
        for (int i = 0; i < 10; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
