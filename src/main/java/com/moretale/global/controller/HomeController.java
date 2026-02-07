package com.moretale.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 메인 페이지: static/index.html 반환 (임시 / 2026.02.05)
    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    // 로그인 페이지: static/login.html 반환 (임시 / 2026.02.05)
    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }
}
