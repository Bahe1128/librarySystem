package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 图书馆管理系统 - Spring Boot 应用入口类
 * <p>
 * 这是整个后端服务的启动类，Spring Boot 会自动扫描该类所在的包及其子包，
 * 加载所有的组件、控制器、服务、配置等，完成依赖注入和自动配置。
 * </p>
 */
@SpringBootApplication // 组合注解，包含 @Configuration（配置类）、@EnableAutoConfiguration（自动配置）、@ComponentScan（组件扫描）
public class LibraryApplication {
    /**
     * Java 应用主方法，Spring Boot 启动点
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 启动 Spring Boot 应用，默认内嵌 Tomcat 作为 Web 服务器
        SpringApplication.run(LibraryApplication.class, args);
    }
}
