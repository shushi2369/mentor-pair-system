package com.mentorpair.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** 启动完成后自动用默认浏览器打开登录页（app.auto-open-browser=false 可关闭） */
@Slf4j
@Component
public class BrowserLauncher implements ApplicationRunner {

    @Value("${app.auto-open-browser:true}")
    private boolean autoOpen;

    @Value("${server.port:8080}")
    private int port;

    @Override
    public void run(ApplicationArguments args) {
        if (!autoOpen) {
            return;
        }
        String url = "http://localhost:" + port + "/login";
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(1500);
                String os = System.getProperty("os.name", "").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", url});
                } else if (java.awt.Desktop.isDesktopSupported()
                        && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                }
                log.info("已打开浏览器：{}", url);
            } catch (Exception e) {
                log.warn("自动打开浏览器失败：{}", e.getMessage());
            }
        }, "browser-opener");
        t.setDaemon(true);
        t.start();
    }
}
