package com.mentorpair;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@SpringBootApplication
@MapperScan("com.mentorpair.mapper")
public class MentorPairApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(MentorPairApplication.class, args);
        } catch (Exception e) {
            // exe 双击场景下控制台会随进程退出，这里拦下启动失败原因并等待用户按回车
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            System.out.println();
            System.out.println("========================================");
            System.out.println("系统启动失败：" + root.getMessage());
            System.out.println("常见原因：");
            System.out.println("  1. 端口被占用 —— 修改 application.yml 的 server.port");
            System.out.println("  2. 数据库无法连接 —— 检查 MySQL 服务与数据库账号密码");
            System.out.println("按回车键退出…");
            System.out.println("========================================");
            try {
                new BufferedReader(new InputStreamReader(System.in)).readLine();
            } catch (IOException ignored) {
            }
            System.exit(1);
        }
    }
}
